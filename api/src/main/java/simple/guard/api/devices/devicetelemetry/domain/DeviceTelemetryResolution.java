package simple.guard.api.devices.devicetelemetry.domain;

public record DeviceTelemetryResolution(DeviceTelemetry technical, boolean created) {

  public DeviceTelemetryResolution {
    technical = copy(technical);
  }

  @Override
  public DeviceTelemetry technical() {
    return copy(technical);
  }

  private static DeviceTelemetry copy(DeviceTelemetry source) {
    if (source == null) {
      return null;
    }

    return new DeviceTelemetry(
        source.getId(),
        source.getDeviceId(),
        source.getBatteryLevelPercentage(),
        source.getBatteryCharging(),
        source.getNetworkType(),
        source.getSignalStrengthDbm(),
        source.getFineLocationPermission(),
        source.getCoarseLocationPermission(),
        source.getCollectedAt(),
        source.getReceivedAt());
  }
}
