package simple.guard.api.devices.deviceunpairingrequest.controller.response;

import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequest;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeviceUnpairingRequestResponse(
        UUID requestId,
        UUID deviceId,
        String deviceName,
        String agentInstanceId,
        String status,
        OffsetDateTime requestedAt,
        OffsetDateTime decidedAt
) {

    public static DeviceUnpairingRequestResponse from(DeviceUnpairingRequest request, Device device) {
        return new DeviceUnpairingRequestResponse(
                request.getId(),
                request.getDeviceId(),
                device.getName(),
                request.getAgentInstanceId(),
                request.getStatus().apiValue(),
                request.getRequestedAt(),
                request.getDecidedAt()
        );
    }
}
