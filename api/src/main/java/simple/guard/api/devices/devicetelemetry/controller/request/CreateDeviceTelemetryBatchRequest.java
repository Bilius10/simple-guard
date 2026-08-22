package simple.guard.api.devices.devicetelemetry.controller.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record CreateDeviceTelemetryBatchRequest(
    @NotEmpty @Size(max = 100) List<SignedDeviceTelemetryRequest> events) {

  public CreateDeviceTelemetryBatchRequest(List<SignedDeviceTelemetryRequest> events) {
    this.events = events == null ? null : Collections.unmodifiableList(new ArrayList<>(events));
  }
}
