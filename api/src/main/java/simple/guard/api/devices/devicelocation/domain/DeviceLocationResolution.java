package simple.guard.api.devices.devicelocation.domain;

import org.locationtech.jts.geom.Point;

public record DeviceLocationResolution(DeviceLocation location, boolean created) {

  public DeviceLocationResolution {
    location = copy(location);
  }

  @Override
  public DeviceLocation location() {
    return copy(location);
  }

  private static DeviceLocation copy(DeviceLocation source) {
    if (source == null) {
      return null;
    }

    Point position = source.getPosition() == null ? null : (Point) source.getPosition().copy();
    return new DeviceLocation(
        source.getId(),
        source.getDeviceId(),
        position,
        source.getAccuracyMeters(),
        source.getAltitudeMeters(),
        source.getSpeedMetersPerSecond(),
        source.getProvider(),
        source.getCollectedAt(),
        source.getReceivedAt());
  }
}
