package simple.guard.api.devices.devicetelemetry.controller.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.locationtech.jts.geom.Point;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.devicelocation.domain.DeviceLocation;
import simple.guard.api.devices.devicetelemetry.domain.DeviceTelemetry;

public record LatestDeviceTelemetryResponse(
    UUID deviceId,
    String deviceName,
    OffsetDateTime lastUpdatedAt,
    Integer batteryLevelPercentage,
    Boolean batteryCharging,
    String networkType,
    Integer signalStrengthDbm,
    BigDecimal latitude,
    BigDecimal longitude,
    BigDecimal accuracyMeters) {

  public static LatestDeviceTelemetryResponse from(
      Device device, DeviceTelemetry technical, DeviceLocation location) {
    Point position = location == null ? null : location.getPosition();
    return new LatestDeviceTelemetryResponse(
        device.getId(),
        device.getName(),
        latestCollectedAt(technical, location),
        technical == null ? null : technical.getBatteryLevelPercentage(),
        technical == null ? null : technical.getBatteryCharging(),
        technical == null ? null : technical.getNetworkType(),
        technical == null ? null : technical.getSignalStrengthDbm(),
        position == null ? null : BigDecimal.valueOf(position.getY()),
        position == null ? null : BigDecimal.valueOf(position.getX()),
        location == null ? null : location.getAccuracyMeters());
  }

  private static OffsetDateTime latestCollectedAt(
      DeviceTelemetry technical, DeviceLocation location) {
    return Stream.of(
            technical == null ? null : technical.getCollectedAt(),
            location == null ? null : location.getCollectedAt())
        .filter(Objects::nonNull)
        .max(OffsetDateTime::compareTo)
        .orElse(null);
  }
}
