package simple.guard.api.devices.devicetelemetry.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import simple.guard.api.devices.devicetelemetry.controller.request.CreateDeviceTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.response.DeviceTelemetryResponse;
import simple.guard.api.devices.devicetelemetry.service.DeviceTelemetryService;

import java.util.UUID;

@RestController
@RequestMapping("/api/agent/devices")
public class AgentDeviceTelemetryController {

    private final DeviceTelemetryService telemetry;

    public AgentDeviceTelemetryController(DeviceTelemetryService telemetry) {
        this.telemetry = telemetry;
    }

    @PostMapping("/{deviceId}/telemetry")
    ResponseEntity<DeviceTelemetryResponse> ingest(
            @PathVariable UUID deviceId,
            @RequestHeader("X-Agent-Instance-Id") String agentInstanceId,
            @RequestHeader("X-Agent-Signature") String signature,
            @Valid @RequestBody CreateDeviceTelemetryRequest request
    ) {
        DeviceTelemetryResponse response = telemetry.ingest(deviceId, agentInstanceId, signature, request);
        HttpStatus status = response.duplicate() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }
}
