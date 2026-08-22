package simple.guard.api.devices.devicelocation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import simple.guard.api.devices.devicelocation.domain.DeviceLocation;
import simple.guard.api.devices.devicelocation.domain.DeviceLocationRepository;
import simple.guard.api.devices.devicelocation.domain.DeviceLocationResolution;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryLocationRequest;

@ExtendWith(MockitoExtension.class)
class DeviceLocationServiceTests {

  private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000001001");
  private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000001002");
  private static final OffsetDateTime COLLECTED_AT =
      OffsetDateTime.parse("2026-08-19T09:00:00-03:00");
  private static final OffsetDateTime RECEIVED_AT =
      OffsetDateTime.parse("2026-08-19T09:00:05-03:00");

  @Mock private DeviceLocationRepository locations;

  private DeviceLocationService service;

  @BeforeEach
  void setUp() {
    service = new DeviceLocationService(locations);
  }

  @Test
  void returnsExistingLocationWithoutPersistingAgainTests() {
    TelemetryLocationRequest request = requestTests();
    DeviceLocation existing = locationTests();
    when(locations.findById(EVENT_ID)).thenReturn(Optional.of(existing));

    DeviceLocationResolution resolution =
        service.resolveLocation(DEVICE_ID, EVENT_ID, request, RECEIVED_AT);

    assertThat(resolution.location()).isSameAs(existing);
    assertThat(resolution.created()).isFalse();
    verify(locations).findById(EVENT_ID);
  }

  @Test
  void persistsLocationWhenMissingTests() {
    TelemetryLocationRequest request = requestTests();
    when(locations.findById(EVENT_ID)).thenReturn(Optional.empty());
    when(locations.saveAndFlush(any(DeviceLocation.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    DeviceLocationResolution resolution =
        service.resolveLocation(DEVICE_ID, EVENT_ID, request, RECEIVED_AT);

    assertThat(resolution.created()).isTrue();
    assertThat(resolution.location()).isNotNull();
    assertThat(resolution.location().getId()).isEqualTo(EVENT_ID);
    assertThat(resolution.location().getDeviceId()).isEqualTo(DEVICE_ID);
    assertThat(resolution.location().getProvider()).isEqualTo("GPS");
    assertThat(resolution.location().getCollectedAt()).isEqualTo(COLLECTED_AT);
    assertThat(resolution.location().getReceivedAt()).isEqualTo(RECEIVED_AT);
    verify(locations).saveAndFlush(any(DeviceLocation.class));
  }

  @Test
  void returnsNullLocationWhenPayloadIsNullTests() {
    DeviceLocationResolution resolution =
        service.resolveLocation(DEVICE_ID, EVENT_ID, null, RECEIVED_AT);

    assertThat(resolution.location()).isNull();
    assertThat(resolution.created()).isFalse();
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

  private static DeviceLocation locationTests() {
    return DeviceLocation.collected(DEVICE_ID, EVENT_ID, requestTests(), RECEIVED_AT);
  }
}
