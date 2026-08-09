package simple.guard.api.devices.pairing.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.guard.api.config.SimpleGuardPairingProperties;
import simple.guard.api.devices.management.domain.Device;
import simple.guard.api.devices.management.domain.DevicePairingStatus;
import simple.guard.api.devices.management.service.DeviceService;
import simple.guard.api.devices.pairing.controller.PairingSessionResponse;
import simple.guard.api.devices.pairing.domain.PairingSession;
import simple.guard.api.devices.pairing.domain.PairingSessionExpirationReason;
import simple.guard.api.devices.pairing.domain.PairingSessionRepository;
import simple.guard.api.devices.pairing.domain.PairingSessionStatus;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.identity.domain.Account;
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
    private final SimpleGuardPairingProperties properties;
    private final Clock clock;

    public PairingSessionService(
            DeviceService deviceService,
            PairingSessionRepository pairingSessions,
            PairingCodeGenerator codeGenerator,
            PairingCodeHasher codeHasher,
            SimpleGuardPairingProperties properties,
            Clock clock
    ) {
        this.deviceService = deviceService;
        this.pairingSessions = pairingSessions;
        this.codeGenerator = codeGenerator;
        this.codeHasher = codeHasher;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public PairingSessionResponse generate(UUID deviceId, Account account) {
        Device device = deviceService.findByIdAndAccountId(deviceId, account.getId());

        validatePairingStatusIsPaired(device.getPairingStatus());

        OffsetDateTime now = OffsetDateTime.now(clock);
        validateOpenPairingSessionsCount(deviceId, now);

        expireElapsedSessionsForDevice(deviceId, account.getSubject(), now);

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
                null,
                account.getSubject(),
                now,
                account.getSubject(),
                now,
                0L
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
        elapsed.forEach(session -> session.expire(now, SYSTEM_ACTOR, PairingSessionExpirationReason.TIMEOUT));
        pairingSessions.saveAll(elapsed);
    }

    private void expireElapsedSessionsForDevice(UUID deviceId, String actor, OffsetDateTime now) {
        List<PairingSession> elapsed = pairingSessions.findAllByDeviceIdAndStatusAndExpiresAtLessThanEqual(
                deviceId,
                PairingSessionStatus.WAITING,
                now
        );

        elapsed.forEach(session -> session.expire(now, actor, PairingSessionExpirationReason.TIMEOUT));

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
}
