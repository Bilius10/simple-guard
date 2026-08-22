package simple.guard.api.devices.devicetelemetry.controller.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TelemetryLocationRequest(
    @NotNull @DecimalMin("-90") @DecimalMax("90") @Digits(integer = 2, fraction = 8)
        BigDecimal latitude,
    @NotNull @DecimalMin("-180") @DecimalMax("180") @Digits(integer = 3, fraction = 8)
        BigDecimal longitude,
    @DecimalMin("0") @Digits(integer = 7, fraction = 3) BigDecimal accuracyMeters,
    @Digits(integer = 9, fraction = 3) BigDecimal altitudeMeters,
    @DecimalMin("0") @Digits(integer = 7, fraction = 3) BigDecimal speedMetersPerSecond,
    @NotBlank @Size(max = 32) @Pattern(regexp = "GPS|NETWORK|PASSIVE|FUSED") String provider,
    @NotNull OffsetDateTime collectedAt) {}
