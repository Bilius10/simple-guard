package simple.guard.api.devices.device.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.guard.api.devices.device.controller.request.CreateDeviceRequest;
import simple.guard.api.devices.device.controller.response.DeviceResponse;
import simple.guard.api.devices.device.controller.response.UnpairDeviceResponse;
import simple.guard.api.devices.device.service.DeviceService;
import simple.guard.api.identity.domain.Account;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping
    ResponseEntity<DeviceResponse> create(
            @Valid @RequestBody CreateDeviceRequest request,
            Authentication authentication
    ) {
        DeviceResponse device = deviceService.create(request, account(authentication));
        return ResponseEntity.created(URI.create("/api/devices/" + device.deviceId())).body(device);
    }

    @GetMapping
    List<DeviceResponse> list(Authentication authentication) {
        return deviceService.list(account(authentication));
    }

    @DeleteMapping("/{deviceId}/unpairing")
    UnpairDeviceResponse unpair(
            @PathVariable UUID deviceId,
            Authentication authentication
    ) {
        return deviceService.unpairByAdministrator(
                deviceId,
                account(authentication)
        );
    }

    private Account account(Authentication authentication) {
        return (Account) authentication.getDetails();
    }
}
