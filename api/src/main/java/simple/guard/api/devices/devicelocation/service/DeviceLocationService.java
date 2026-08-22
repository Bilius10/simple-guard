package simple.guard.api.devices.devicelocation.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import simple.guard.api.devices.devicelocation.domain.DeviceLocation;
import simple.guard.api.devices.devicelocation.domain.DeviceLocationRepository;
import simple.guard.api.devices.devicelocation.domain.DeviceLocationResolution;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryLocationRequest;

@Service
public class DeviceLocationService {

  private final DeviceLocationRepository locations;

  public DeviceLocationService(DeviceLocationRepository locations) {
    this.locations = locations;
  }

  public DeviceLocationResolution resolveLocation(
      UUID deviceId, UUID eventId, TelemetryLocationRequest request, OffsetDateTime receivedAt) {
    if (request == null) {
      return new DeviceLocationResolution(null, false);
    }

    DeviceLocation existing = locations.findById(eventId).orElse(null);
    if (existing != null) {
      return new DeviceLocationResolution(existing, false);
    }

    return new DeviceLocationResolution(
        locations.saveAndFlush(DeviceLocation.collected(deviceId, eventId, request, receivedAt)),
        true);
  }
}
