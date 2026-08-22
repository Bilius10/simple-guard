package simple.guard.api.devices.deviceunpairingrequest.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.guard.api.devices.deviceunpairingrequest.controller.request.DecideDeviceUnpairingRequest;
import simple.guard.api.devices.deviceunpairingrequest.controller.response.DeviceUnpairingDecisionResponse;
import simple.guard.api.devices.deviceunpairingrequest.controller.response.DeviceUnpairingRequestResponse;
import simple.guard.api.devices.deviceunpairingrequest.service.DeviceUnpairingRequestService;
import simple.guard.api.identity.domain.Account;

@RestController
@RequestMapping("/api/devices/unpairing-requests")
public class DeviceUnpairingRequestController {

  private final DeviceUnpairingRequestService unpairingRequests;

  public DeviceUnpairingRequestController(DeviceUnpairingRequestService unpairingRequests) {
    this.unpairingRequests = unpairingRequests;
  }

  @GetMapping
  List<DeviceUnpairingRequestResponse> listPending(Authentication authentication) {
    return unpairingRequests.listPending(account(authentication));
  }

  @PostMapping("/{requestId}/decision")
  ResponseEntity<DeviceUnpairingDecisionResponse> decide(
      @PathVariable UUID requestId,
      @Valid @RequestBody DecideDeviceUnpairingRequest request,
      Authentication authentication) {
    return ResponseEntity.ok(
        unpairingRequests.decide(requestId, account(authentication), request.status()));
  }

  private Account account(Authentication authentication) {
    return (Account) authentication.getDetails();
  }
}
