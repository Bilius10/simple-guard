package simple.guard.api.devices.devicetelemetry.controller.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateDeviceTelemetryRequest(
    @NotNull UUID eventId,
    @Valid TelemetryLocationRequest location,
    @Valid TechnicalTelemetryRequest technical) {

  @AssertTrue
  @JsonIgnore
  public boolean hasPayload() {
    return location != null || technical != null;
  }
}
