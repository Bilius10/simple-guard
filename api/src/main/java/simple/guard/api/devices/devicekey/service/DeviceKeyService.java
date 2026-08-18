package simple.guard.api.devices.devicekey.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.domain.DeviceKeyRepository;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceKeyService {

    private final DeviceKeyRepository deviceKeys;

    public DeviceKeyService(DeviceKeyRepository deviceKeys) {
        this.deviceKeys = deviceKeys;
    }

    public DeviceKey requireActiveForTelemetry(UUID deviceId, String agentInstanceId) {
        return deviceKeys.findByDeviceIdAndAgentInstanceIdAndStatus(
                        deviceId,
                        agentInstanceId,
                        DeviceKeyStatus.ACTIVE
                )
                .orElseThrow(() -> new SimpleGuardException(
                        HttpStatus.UNAUTHORIZED,
                        SimpleGuardErrorCode.DEVICE_CREDENTIAL_REVOKED,
                        SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_REVOKED
                ));
    }

    public DeviceKey requireActiveForAgentUnpairing(UUID deviceId, String agentInstanceId) {
        return deviceKeys.findByDeviceIdAndAgentInstanceIdAndStatus(
                        deviceId,
                        agentInstanceId,
                        DeviceKeyStatus.ACTIVE
                )
                .orElseThrow(() -> new SimpleGuardException(
                        HttpStatus.UNAUTHORIZED,
                        SimpleGuardErrorCode.DEVICE_CREDENTIAL_INVALID,
                        SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_INVALID
                ));
    }

    public DeviceKey requireForAgentPairingStatus(UUID deviceId, String agentInstanceId) {
        return deviceKeys.findFirstByDeviceIdAndAgentInstanceIdOrderByCreatedAtDesc(
                        deviceId,
                        agentInstanceId
                )
                .orElseThrow(() -> new SimpleGuardException(
                        HttpStatus.UNAUTHORIZED,
                        SimpleGuardErrorCode.DEVICE_CREDENTIAL_INVALID,
                        SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_INVALID
                ));
    }

    public void registerActiveKey(
            Device device,
            UUID pairingSessionId,
            String agentInstanceId,
            String publicKey
    ) {
        deviceKeys.saveAndFlush(DeviceKey.active(
                UUID.randomUUID(),
                device.getId(),
                pairingSessionId,
                agentInstanceId,
                device.getPlatform(),
                publicKey
        ));
    }

    public int revokeActiveKeysForDevice(UUID deviceId, String actor, OffsetDateTime revokedAt) {
        List<DeviceKey> activeKeys = deviceKeys.findAllByDeviceIdAndStatus(deviceId, DeviceKeyStatus.ACTIVE);
        int revokedKeyCount = (int) activeKeys.stream()
                .filter(key -> key.revoke(actor, revokedAt))
                .count();

        if (!activeKeys.isEmpty()) {
            deviceKeys.saveAllAndFlush(activeKeys);
        }

        return revokedKeyCount;
    }
}


