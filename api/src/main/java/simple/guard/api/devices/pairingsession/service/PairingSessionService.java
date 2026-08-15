package simple.guard.api.devices.pairingsession.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.guard.api.config.properties.SimpleGuardPairingProperties;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.device.domain.DevicePairingStatus;
import simple.guard.api.devices.device.service.DeviceService;
import simple.guard.api.devices.devicekey.service.DeviceKeyService;
import simple.guard.api.devices.pairingsession.controller.request.CompleteAgentPairingRequest;
import simple.guard.api.devices.pairingsession.controller.response.CompleteAgentPairingResponse;
import simple.guard.api.devices.pairingsession.controller.response.PairingSessionResponse;
import simple.guard.api.devices.pairingsession.domain.PairingSession;
import simple.guard.api.devices.pairingsession.domain.PairingSessionExpirationReason;
import simple.guard.api.devices.pairingsession.domain.PairingSessionRepository;
import simple.guard.api.devices.pairingsession.domain.PairingSessionStatus;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.shared.audit.AuditContext;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PairingSessionService {

    private static final String SYSTEM_ACTOR = "simpleguard-system";

    private final DeviceService deviceService;
    private final PairingSessionRepository pairingSessions;
    private final PairingCodeGenerator codeGenerator;
    private final PairingCodeHasher codeHasher;
    private final DeviceKeyService deviceKeys;
    private final SimpleGuardPairingProperties properties;
    private final Clock clock;

    public PairingSessionService(
            DeviceService deviceService,
            PairingSessionRepository pairingSessions,
            PairingCodeGenerator codeGenerator,
            PairingCodeHasher codeHasher,
            DeviceKeyService deviceKeys,
            SimpleGuardPairingProperties properties,
            Clock clock
    ) {
        this.deviceService = deviceService;
        this.pairingSessions = pairingSessions;
        this.codeGenerator = codeGenerator;
        this.codeHasher = codeHasher;
        this.deviceKeys = deviceKeys;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public PairingSessionResponse generate(UUID deviceId, Account account) {
        Device device = deviceService.findByIdAndAccountId(deviceId, account.getId());

        validatePairingStatusIsPaired(device.getPairingStatus());

        OffsetDateTime now = OffsetDateTime.now(clock);

        validateOpenPairingSessionsCount(deviceId, now);
        expireElapsedSessionsForDevice(deviceId, now);

        String pairingCode = codeGenerator.generate();

        PairingSession session = new PairingSession(
                UUID.randomUUID(),
                device.getId(),
                account.getId(),
                codeHasher.hash(pairingCode),
                PairingSessionStatus.WAITING,
                null,
                now.plus(properties.sessionValidity()),
                null,
                null
        );

        return PairingSessionResponse.from(pairingSessions.save(session), pairingCode);
    }

    @Transactional
    public void expireElapsedSessions() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        List<PairingSession> elapsed = pairingSessions.findAllByStatusAndExpiresAtLessThanEqual(
                PairingSessionStatus.WAITING,
                now
        );
        AuditContext.runAs(SYSTEM_ACTOR, () -> {
            elapsed.forEach(session -> session.expire(now, PairingSessionExpirationReason.TIMEOUT));
            pairingSessions.saveAll(elapsed);
        });
    }

    private void expireElapsedSessionsForDevice(UUID deviceId, OffsetDateTime now) {
        List<PairingSession> elapsed = pairingSessions.findAllByDeviceIdAndStatusAndExpiresAtLessThanEqual(
                deviceId,
                PairingSessionStatus.WAITING,
                now
        );

        elapsed.forEach(session -> session.expire(now, PairingSessionExpirationReason.TIMEOUT));

        pairingSessions.saveAllAndFlush(elapsed);
    }

    private void validatePairingStatusIsPaired(DevicePairingStatus devicePairingStatus) {
        if (devicePairingStatus == DevicePairingStatus.PAIRED) {
            throw new SimpleGuardException(
                    HttpStatus.CONFLICT,
                    SimpleGuardErrorCode.DEVICE_ALREADY_PAIRED,
                    SimpleGuardTranslation.ERROR_DEVICE_ALREADY_PAIRED
            );
        }
    }

    private void validateOpenPairingSessionsCount(UUID deviceId, OffsetDateTime now) {
        if (pairingSessions.existsByDeviceIdAndStatusAndExpiresAtAfter(
                deviceId, PairingSessionStatus.WAITING, now
        )) {
            throw new SimpleGuardException(
                    HttpStatus.CONFLICT,
                    SimpleGuardErrorCode.MAX_OPEN_PAIRING_SESSIONS_REACHED,
                    SimpleGuardTranslation.ERROR_MAX_OPEN_PAIRING_SESSIONS_REACHED
            );
        }
    }

    public PairingSession findPairingSession(String pairingCode) {
        return pairingSessions.findByCodeHash(codeHasher.hash(pairingCode))
                .orElseThrow(() -> new SimpleGuardException(
                        HttpStatus.NOT_FOUND,
                        SimpleGuardErrorCode.PAIRING_SESSION_INVALID,
                        SimpleGuardTranslation.ERROR_PAIRING_SESSION_INVALID
                ));
    }

    @Transactional
    public CompleteAgentPairingResponse complete(CompleteAgentPairingRequest request) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        PairingSession session = findPairingSession(request.pairingCode());
        Device device = deviceService.findDeviceById(session.getDeviceId());

        validateDeviceCanPair(device, request);
        validatePairingSession(session, now);

        AuditContext.runAs(agentActor(request), () -> {
            useSession(session, now);
            deviceService.updatePairingStatus(device, DevicePairingStatus.PAIRED);
            deviceKeys.registerActiveKey(
                    device,
                    session.getId(),
                    request.agentInstanceId().trim(),
                    request.publicKey().trim()
            );
        });

        return CompleteAgentPairingResponse.from(device, now);
    }

    private void validateDeviceCanPair(Device device, CompleteAgentPairingRequest request) {
        validateDeviceIsNotPaired(device);
        validatePlatformMatches(device, request);
    }

    private void validatePairingSession(PairingSession session, OffsetDateTime now) {
        validatePairingIsUsed(session.getStatus());
        validatePairingIsValid(session.getStatus(), session.getExpiresAt(), now);
    }

    private void validatePairingIsUsed(PairingSessionStatus status) {
        if (status == PairingSessionStatus.USED) {
            throw new SimpleGuardException(
                    HttpStatus.CONFLICT,
                    SimpleGuardErrorCode.PAIRING_SESSION_ALREADY_USED,
                    SimpleGuardTranslation.ERROR_PAIRING_SESSION_ALREADY_USED
            );
        }
    }

    private void validatePairingIsValid(PairingSessionStatus status, OffsetDateTime expiresAt, OffsetDateTime now) {
        if (status != PairingSessionStatus.WAITING || !now.isBefore(expiresAt)) {
            throw new SimpleGuardException(
                    HttpStatus.GONE,
                    SimpleGuardErrorCode.PAIRING_SESSION_EXPIRED,
                    SimpleGuardTranslation.ERROR_PAIRING_SESSION_EXPIRED
            );
        }
    }

    private void validateDeviceIsNotPaired(Device device) {
        if (device.getPairingStatus() == DevicePairingStatus.PAIRED) {
            throw new SimpleGuardException(
                    HttpStatus.CONFLICT,
                    SimpleGuardErrorCode.DEVICE_ALREADY_PAIRED,
                    SimpleGuardTranslation.ERROR_DEVICE_ALREADY_PAIRED
            );
        }
    }

    private void validatePlatformMatches(Device device, CompleteAgentPairingRequest request) {
        if (device.getPlatform() != request.platform()) {
            throw new SimpleGuardException(
                    HttpStatus.CONFLICT,
                    SimpleGuardErrorCode.DEVICE_PLATFORM_MISMATCH,
                    SimpleGuardTranslation.ERROR_DEVICE_PLATFORM_MISMATCH
            );
        }
    }

    private static String agentActor(CompleteAgentPairingRequest request) {
        return "agent:" + request.agentInstanceId().trim();
    }

    private void useSession(PairingSession session, OffsetDateTime now) {
        session.use(now);
        pairingSessions.saveAndFlush(session);
    }
}


