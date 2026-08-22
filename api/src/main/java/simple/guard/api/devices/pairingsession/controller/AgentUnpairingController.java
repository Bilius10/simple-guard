package simple.guard.api.devices.pairingsession.controller;

import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.guard.api.devices.deviceunpairingrequest.controller.response.DeviceUnpairingRequestResponse;
import simple.guard.api.devices.deviceunpairingrequest.service.DeviceUnpairingRequestService;
import simple.guard.api.devices.pairingsession.controller.response.AgentPairingStatusResponse;

@RestController
@RequestMapping("/api/agent/devices")
public class AgentUnpairingController {

  private final DeviceUnpairingRequestService deviceUnpairingRequests;

  public AgentUnpairingController(DeviceUnpairingRequestService deviceUnpairingRequests) {
    this.deviceUnpairingRequests = deviceUnpairingRequests;
  }

  @DeleteMapping("/{deviceId}/pairing")
  ResponseEntity<DeviceUnpairingRequestResponse> unpair(
      @PathVariable UUID deviceId,
      @RequestHeader("X-Agent-Instance-Id") String agentInstanceId,
      @RequestHeader("X-Agent-Signature") String signature) {
    return ResponseEntity.accepted()
        .cacheControl(CacheControl.noStore())
        .body(deviceUnpairingRequests.requestByAgent(deviceId, agentInstanceId, signature));
  }

  @GetMapping("/{deviceId}/pairing")
  ResponseEntity<AgentPairingStatusResponse> pairingStatus(
      @PathVariable UUID deviceId,
      @RequestHeader("X-Agent-Instance-Id") String agentInstanceId,
      @RequestHeader("X-Agent-Signature") String signature) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(deviceUnpairingRequests.statusForAgent(deviceId, agentInstanceId, signature));
  }
}
