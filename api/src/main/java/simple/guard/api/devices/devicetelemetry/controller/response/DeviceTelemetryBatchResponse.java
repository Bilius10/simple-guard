package simple.guard.api.devices.devicetelemetry.controller.response;

import java.util.List;

public record DeviceTelemetryBatchResponse(List<DeviceTelemetryBatchItemResponse> results) {

  public DeviceTelemetryBatchResponse {
    results = List.copyOf(results);
  }
}
