package simple.guard.api.devices.devicelocation.controller.response;

import simple.guard.api.devices.devicelocation.domain.DeviceLocation;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DeviceLocationResponse(
        UUID locationId,
        UUID deviceId,
        OffsetDateTime collectedAt,
        OffsetDateTime receivedAt
) {

    public static DeviceLocationResponse from(DeviceLocation location) {
        return new DeviceLocationResponse(
                location.getId(),
                location.getDeviceId(),
                location.getCollectedAt(),
                location.getReceivedAt()
        );
    }
}
