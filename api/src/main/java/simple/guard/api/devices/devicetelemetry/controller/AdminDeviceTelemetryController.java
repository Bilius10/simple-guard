package simple.guard.api.devices.devicetelemetry.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.guard.api.devices.devicetelemetry.controller.response.LatestDeviceTelemetryResponse;
import simple.guard.api.devices.devicetelemetry.service.LatestDeviceTelemetryService;
import simple.guard.api.identity.domain.Account;

@RestController
@RequestMapping("/api/devices")
public class AdminDeviceTelemetryController {

  private final LatestDeviceTelemetryService telemetry;

  public AdminDeviceTelemetryController(LatestDeviceTelemetryService telemetry) {
    this.telemetry = telemetry;
  }

  @GetMapping("/telemetry/latest")
  List<LatestDeviceTelemetryResponse> latestForPairedDevices(Authentication authentication) {
    return telemetry.findLatestForPairedDevices(account(authentication));
  }

  @GetMapping("/{deviceId}/telemetry/latest")
  LatestDeviceTelemetryResponse latest(@PathVariable UUID deviceId, Authentication authentication) {
    return telemetry.findLatest(deviceId, account(authentication));
  }

  private Account account(Authentication authentication) {
    return (Account) authentication.getDetails();
  }
}
