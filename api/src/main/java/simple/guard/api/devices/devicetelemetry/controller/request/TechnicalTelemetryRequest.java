package simple.guard.api.devices.devicetelemetry.controller.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;

public record TechnicalTelemetryRequest(
    @Min(0) @Max(100) Integer batteryLevelPercentage,
    Boolean batteryCharging,
    @Pattern(regexp = "NONE|WIFI|CELLULAR|ETHERNET|VPN|OTHER") String networkType,
    @Min(-160) @Max(0) Integer signalStrengthDbm,
    @Valid TelemetryPermissionsRequest permissions,
    @NotNull OffsetDateTime collectedAt) {}
