package simple.guard.api.devices.controller;

import simple.guard.api.devices.domain.Device;
import simple.guard.api.devices.domain.DevicePlatform;
import simple.guard.api.devices.domain.DeviceType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeviceResponse(
        UUID deviceId,
        String name,
        DeviceType type,
        DevicePlatform platform,
        String pairingStatus,
        OffsetDateTime createdAt
) {

    public static DeviceResponse from(Device device) {
        return new DeviceResponse(
                device.getId(),
                device.getName(),
                device.getType(),
                device.getPlatform(),
                device.getPairingStatus().apiValue(),
                device.getCreatedAt()
        );
    }
}
