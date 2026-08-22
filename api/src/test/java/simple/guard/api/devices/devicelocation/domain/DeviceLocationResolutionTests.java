package simple.guard.api.devices.devicelocation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryLocationRequest;

class DeviceLocationResolutionTests {

  private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000001011");
  private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000001012");
  private static final OffsetDateTime COLLECTED_AT =
      OffsetDateTime.parse("2026-08-19T09:00:00-03:00");
  private static final OffsetDateTime RECEIVED_AT =
      OffsetDateTime.parse("2026-08-19T09:00:05-03:00");

  @Test
  void returnsNullLocationWhenResolutionCreatedWithoutLocationTests() {
    DeviceLocationResolution resolution = new DeviceLocationResolution(null, false);

    assertThat(resolution.location()).isNull();
    assertThat(resolution.created()).isFalse();
  }

  @Test
  void keepsNullPositionWhenSourceLocationHasNoGeometryTests() {
    DeviceLocation source =
        new DeviceLocation(
            EVENT_ID,
            DEVICE_ID,
            null,
            new BigDecimal("4.500"),
            null,
            BigDecimal.ZERO,
            "GPS",
            COLLECTED_AT,
            RECEIVED_AT);

    DeviceLocationResolution resolution = new DeviceLocationResolution(source, true);

    assertThat(resolution.location().getPosition()).isNull();
    assertThat(resolution.location()).isEqualTo(source);
    assertThat(resolution.location()).isNotSameAs(source);
  }

  @Test
  void copiesLocationDefensivelyOnConstructionAndReadTests() {
    DeviceLocation source =
        DeviceLocation.collected(DEVICE_ID, EVENT_ID, requestTests(), RECEIVED_AT);

    DeviceLocationResolution resolution = new DeviceLocationResolution(source, true);
    source.setProvider("NETWORK");
    source.setPosition(
        DeviceLocation.collected(DEVICE_ID, EVENT_ID, alternateRequestTests(), RECEIVED_AT)
            .getPosition());

    DeviceLocation firstRead = resolution.location();
    DeviceLocation secondRead = resolution.location();

    assertThat(firstRead).isEqualTo(locationTests());
    assertThat(firstRead).isNotSameAs(source);
    assertThat(firstRead.getPosition()).isNotSameAs(source.getPosition());
    assertThat(secondRead).isEqualTo(firstRead);
    assertThat(secondRead).isNotSameAs(firstRead);
  }

  private static TelemetryLocationRequest requestTests() {
    return new TelemetryLocationRequest(
        new BigDecimal("-23.55052000"),
        new BigDecimal("-46.63330800"),
        new BigDecimal("4.500"),
        null,
        BigDecimal.ZERO,
        "GPS",
        COLLECTED_AT);
  }

  private static TelemetryLocationRequest alternateRequestTests() {
    return new TelemetryLocationRequest(
        new BigDecimal("-22.90000000"),
        new BigDecimal("-43.20000000"),
        new BigDecimal("8.000"),
        null,
        BigDecimal.ZERO,
        "NETWORK",
        COLLECTED_AT);
  }

  private static DeviceLocation locationTests() {
    return DeviceLocation.collected(DEVICE_ID, EVENT_ID, requestTests(), RECEIVED_AT);
  }
}
