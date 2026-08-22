package simple.guard.api.devices.devicetelemetry.domain;

import java.util.Map;
import java.util.UUID;
import simple.guard.api.devices.devicelocation.domain.DeviceLocation;

public record LatestTelemetryByDevice(
    Map<UUID, DeviceTelemetry> technicalTelemetry, Map<UUID, DeviceLocation> locations) {

  public LatestTelemetryByDevice {
    technicalTelemetry = Map.copyOf(technicalTelemetry);
    locations = Map.copyOf(locations);
  }

  @Override
  public Map<UUID, DeviceTelemetry> technicalTelemetry() {
    return Map.copyOf(technicalTelemetry);
  }

  @Override
  public Map<UUID, DeviceLocation> locations() {
    return Map.copyOf(locations);
  }
}
