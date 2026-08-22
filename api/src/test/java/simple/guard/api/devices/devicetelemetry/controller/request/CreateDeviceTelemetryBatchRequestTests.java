package simple.guard.api.devices.devicetelemetry.controller.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CreateDeviceTelemetryBatchRequestTests {

  @Test
  void allowsNullCollectionForValidationPhaseTests() {
    CreateDeviceTelemetryBatchRequest request = new CreateDeviceTelemetryBatchRequest(null);

    assertThat(request.events()).isNull();
  }

  @Test
  void preservesNullItemsAndDefensivelyCopiesEventsTests() {
    SignedDeviceTelemetryRequest event =
        new SignedDeviceTelemetryRequest(
            "signature",
            new CreateDeviceTelemetryRequest(
                UUID.fromString("00000000-0000-0000-0000-000000001021"),
                null,
                new TechnicalTelemetryRequest(
                    null,
                    null,
                    null,
                    null,
                    null,
                    OffsetDateTime.parse("2026-08-19T09:00:00-03:00"))));
    List<SignedDeviceTelemetryRequest> source = new ArrayList<>();
    source.add(null);
    source.add(event);

    CreateDeviceTelemetryBatchRequest request = new CreateDeviceTelemetryBatchRequest(source);
    source.clear();

    assertThat(request.events()).hasSize(2);
    assertThat(request.events().getFirst()).isNull();
    assertThat(request.events().get(1)).isEqualTo(event);
    assertThatThrownBy(() -> request.events().add(event))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
