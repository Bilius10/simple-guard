package simple.guard.api.devices.devicetelemetry.controller.request;

import jakarta.validation.constraints.Pattern;

public record TelemetryPermissionsRequest(
        @Pattern(regexp = "GRANTED|DENIED")
        String fineLocation,
        @Pattern(regexp = "GRANTED|DENIED")
        String coarseLocation
) {
}
