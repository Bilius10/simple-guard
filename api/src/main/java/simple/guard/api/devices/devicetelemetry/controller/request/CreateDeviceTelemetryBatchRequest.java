package simple.guard.api.devices.devicetelemetry.controller.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateDeviceTelemetryBatchRequest(
    @NotEmpty @Size(max = 100) List<SignedDeviceTelemetryRequest> events) {

  public CreateDeviceTelemetryBatchRequest {
    events = List.copyOf(events);
  }
}
