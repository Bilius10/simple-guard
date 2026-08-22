package simple.guard.api.devices.devicetelemetry.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.device.service.DeviceService;
import simple.guard.api.devices.devicelocation.domain.DeviceLocation;
import simple.guard.api.devices.devicelocation.service.DeviceLocationService;
import simple.guard.api.devices.devicetelemetry.controller.response.LatestDeviceTelemetryResponse;
import simple.guard.api.devices.devicetelemetry.domain.DeviceTelemetry;
import simple.guard.api.devices.devicetelemetry.domain.LatestTelemetryByDevice;
import simple.guard.api.identity.domain.Account;

@Service
public class LatestDeviceTelemetryService {

  private final DeviceService devices;
  private final DeviceTelemetryService technicalTelemetry;
  private final DeviceLocationService locations;

  public LatestDeviceTelemetryService(
      DeviceService devices,
      DeviceTelemetryService technicalTelemetry,
      DeviceLocationService locations) {
    this.devices = devices;
    this.technicalTelemetry = technicalTelemetry;
    this.locations = locations;
  }

  @Transactional(readOnly = true)
  public LatestDeviceTelemetryResponse findLatest(UUID deviceId, Account account) {
    Device device = devices.findByIdAndAccountId(deviceId, account.getId());
    DeviceTelemetry technical = findLatestTechnicalTelemetry(deviceId);
    DeviceLocation location = findLatestLocation(deviceId);
    return LatestDeviceTelemetryResponse.from(device, technical, location);
  }

  @Transactional(readOnly = true)
  public List<LatestDeviceTelemetryResponse> findLatestForPairedDevices(Account account) {
    List<Device> pairedDevices = devices.listPairedDevices(account);

    if (pairedDevices.isEmpty()) {
      return List.of();
    }

    LatestTelemetryByDevice latestTelemetry = latestTelemetryByDevice(deviceIds(pairedDevices));
    return responsesForDevices(pairedDevices, latestTelemetry);
  }

  private List<UUID> deviceIds(List<Device> devices) {
    return devices.stream().map(Device::getId).toList();
  }

  private LatestTelemetryByDevice latestTelemetryByDevice(List<UUID> deviceIds) {
    return new LatestTelemetryByDevice(
        technicalTelemetry.findLatestByDeviceIds(deviceIds).stream()
            .collect(Collectors.toMap(DeviceTelemetry::getDeviceId, telemetry -> telemetry)),
        locations.findLatestByDeviceIds(deviceIds).stream()
            .collect(Collectors.toMap(DeviceLocation::getDeviceId, location -> location)));
  }

  private List<LatestDeviceTelemetryResponse> responsesForDevices(
      List<Device> devices, LatestTelemetryByDevice latestTelemetry) {
    return devices.stream()
        .map(
            device ->
                LatestDeviceTelemetryResponse.from(
                    device,
                    latestTelemetry.technicalTelemetry().get(device.getId()),
                    latestTelemetry.locations().get(device.getId())))
        .toList();
  }

  private DeviceTelemetry findLatestTechnicalTelemetry(UUID deviceId) {
    return technicalTelemetry.findFirstByDeviceIdOrderByCollectedAtDesc(deviceId).orElse(null);
  }

  private DeviceLocation findLatestLocation(UUID deviceId) {
    return locations.findFirstByDeviceIdOrderByCollectedAtDesc(deviceId).orElse(null);
  }
}
