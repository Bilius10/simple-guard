package simple.guard.api.devices.pairingsession.controller.response;

import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.device.domain.DevicePlatform;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CompleteAgentPairingResponse(
        UUID deviceId,
        String deviceName,
        DevicePlatform platform,
        String pairingStatus,
        OffsetDateTime pairedAt
) {

    public static CompleteAgentPairingResponse from(
            Device device,
            OffsetDateTime pairedAt
    ) {
        return new CompleteAgentPairingResponse(
                device.getId(),
                device.getName(),
                device.getPlatform(),
                device.getPairingStatus().apiValue(),
                pairedAt
        );
    }
}
