package simple.guard.api.devices.pairing.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.guard.api.devices.keys.domain.DeviceKey;
import simple.guard.api.devices.keys.domain.DeviceKeyRepository;
import simple.guard.api.devices.management.domain.Device;
import simple.guard.api.devices.management.domain.DevicePairingStatus;
import simple.guard.api.devices.management.domain.DeviceRepository;
import simple.guard.api.devices.management.service.DeviceService;
import simple.guard.api.devices.pairing.controller.request.CompleteAgentPairingRequest;
import simple.guard.api.devices.pairing.controller.response.CompleteAgentPairingResponse;
import simple.guard.api.devices.pairing.domain.PairingSession;
import simple.guard.api.devices.pairing.domain.PairingSessionRepository;
import simple.guard.api.devices.pairing.domain.PairingSessionStatus;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.shared.audit.AuditContext;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AgentPairingService {

    private final PairingSessionRepository pairingSessions;
    private final DeviceRepository devices;
    private final DeviceKeyRepository deviceKeys;
    private final Clock clock;
    private final DeviceService deviceService;
    private final PairingSessionService pairingSessionService;

    public AgentPairingService(
            PairingSessionRepository pairingSessions,
            DeviceRepository devices,
            DeviceKeyRepository deviceKeys,
            Clock clock,
            DeviceService deviceService,
            PairingSessionService pairingSessionService
    ) {
        this.pairingSessions = pairingSessions;
        this.devices = devices;
        this.deviceKeys = deviceKeys;
        this.clock = clock;
        this.deviceService = deviceService;
        this.pairingSessionService = pairingSessionService;
    }

    @Transactional
    public CompleteAgentPairingResponse complete(CompleteAgentPairingRequest request) {
        OffsetDateTime now = OffsetDateTime.now(clock);
        PairingSession session = pairingSessionService.findPairingSession(request.pairingCode());
        Device device = deviceService.findDeviceById(session.getDeviceId());

        validateDeviceCanPair(device, request);
        validatePairingSession(session, now);

        AuditContext.runAs(agentActor(request), () -> {
            useSession(session, now);
            pairDevice(device);
            registerDeviceKey(device, session, request);
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

    private void pairDevice(Device device) {
        device.setPairingStatus(DevicePairingStatus.PAIRED);
        devices.saveAndFlush(device);
    }

    private void registerDeviceKey(
            Device device,
            PairingSession session,
            CompleteAgentPairingRequest request
    ) {
        deviceKeys.saveAndFlush(DeviceKey.active(
                UUID.randomUUID(),
                device.getId(),
                session.getId(),
                request.agentInstanceId().trim(),
                device.getPlatform(),
                request.publicKey().trim()
        ));
    }
}
