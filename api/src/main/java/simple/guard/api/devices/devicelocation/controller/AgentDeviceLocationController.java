package simple.guard.api.devices.devicelocation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.guard.api.devices.devicelocation.controller.request.CreateDeviceLocationRequest;
import simple.guard.api.devices.devicelocation.controller.response.DeviceLocationResponse;
import simple.guard.api.devices.devicelocation.service.DeviceLocationService;

import java.util.UUID;

@RestController
@RequestMapping("/api/agent/devices")
public class AgentDeviceLocationController {

    private final DeviceLocationService deviceLocations;

    public AgentDeviceLocationController(DeviceLocationService deviceLocations) {
        this.deviceLocations = deviceLocations;
    }

    @PostMapping("/{deviceId}/locations")
    ResponseEntity<DeviceLocationResponse> ingest(
            @PathVariable UUID deviceId,
            @RequestHeader("X-Agent-Instance-Id") String agentInstanceId,
            @RequestHeader("X-Agent-Signature") String signature,
            @Valid @RequestBody CreateDeviceLocationRequest request
    ) {
        DeviceLocationResponse response = deviceLocations.ingest(deviceId, agentInstanceId, signature, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
