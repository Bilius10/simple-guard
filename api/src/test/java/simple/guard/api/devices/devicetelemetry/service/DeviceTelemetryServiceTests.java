package simple.guard.api.devices.devicetelemetry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import simple.guard.api.devices.device.domain.DevicePlatform;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.devicekey.service.DeviceKeyService;
import simple.guard.api.devices.devicelocation.domain.DeviceLocation;
import simple.guard.api.devices.devicelocation.domain.DeviceLocationResolution;
import simple.guard.api.devices.devicelocation.service.DeviceLocationService;
import simple.guard.api.devices.devicetelemetry.controller.request.CreateDeviceTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TechnicalTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryLocationRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryPermissionsRequest;
import simple.guard.api.devices.devicetelemetry.controller.response.DeviceTelemetryResponse;
import simple.guard.api.devices.devicetelemetry.domain.DeviceTelemetry;
import simple.guard.api.devices.devicetelemetry.domain.DeviceTelemetryRepository;
import simple.guard.api.devices.pairingsession.service.AgentSignatureVerifier;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

@ExtendWith(MockitoExtension.class)
class DeviceTelemetryServiceTests {

  private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000002001");
  private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000002002");
  private static final String AGENT_INSTANCE_ID = "android-agent-test";
  private static final OffsetDateTime COLLECTED_AT =
      OffsetDateTime.parse("2026-08-19T09:00:00-03:00");
  private static final OffsetDateTime RECEIVED_AT = OffsetDateTime.parse("2026-08-19T12:00:10Z");

  @Mock private DeviceLocationService locations;

  @Mock private DeviceTelemetryRepository technicalTelemetry;

  @Mock private DeviceKeyService deviceKeys;

  @Mock private AgentSignatureVerifier signatureVerifier;

  private DeviceTelemetryService service;

  @BeforeEach
  void setUp() {
    service =
        new DeviceTelemetryService(
            locations,
            technicalTelemetry,
            deviceKeys,
            signatureVerifier,
            Clock.fixed(Instant.parse("2026-08-19T12:00:10Z"), ZoneOffset.UTC));
  }

  @Test
  void ingestsLocationAndTechnicalTelemetryTests() {
    CreateDeviceTelemetryRequest request = completeRequestTests();
    DeviceKey deviceKey = deviceKeyTests();
    DeviceLocation location = locationTests();
    DeviceTelemetry technical = technicalTests();

    when(deviceKeys.requireByDeviceIdAndAgentInstanceIdAndStatus(
            DEVICE_ID,
            AGENT_INSTANCE_ID,
            DeviceKeyStatus.ACTIVE,
            SimpleGuardErrorCode.DEVICE_CREDENTIAL_REVOKED,
            SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_REVOKED))
        .thenReturn(deviceKey);
    when(signatureVerifier.verifyTelemetry(
            deviceKey.getPublicKey(), DEVICE_ID, AGENT_INSTANCE_ID, request, "signature"))
        .thenReturn(true);
    when(locations.resolveLocation(DEVICE_ID, EVENT_ID, request.location(), RECEIVED_AT))
        .thenReturn(new DeviceLocationResolution(location, true));
    when(technicalTelemetry.findById(EVENT_ID)).thenReturn(Optional.empty());
    when(technicalTelemetry.saveAndFlush(any(DeviceTelemetry.class))).thenReturn(technical);

    DeviceTelemetryResponse response =
        service.ingest(DEVICE_ID, AGENT_INSTANCE_ID, "signature", request);

    assertThat(response.eventId()).isEqualTo(EVENT_ID);
    assertThat(response.deviceId()).isEqualTo(DEVICE_ID);
    assertThat(response.locationId()).isEqualTo(EVENT_ID);
    assertThat(response.technicalTelemetryId()).isEqualTo(EVENT_ID);
    assertThat(response.duplicate()).isFalse();
    verify(locations).resolveLocation(DEVICE_ID, EVENT_ID, request.location(), RECEIVED_AT);
    verify(technicalTelemetry).saveAndFlush(any(DeviceTelemetry.class));
  }

  @Test
  void marksDuplicateWhenBothReadingsAlreadyExistTests() {
    CreateDeviceTelemetryRequest request = completeRequestTests();
    DeviceKey deviceKey = deviceKeyTests();
    DeviceLocation existingLocation = locationTests();
    DeviceTelemetry existingTechnical = technicalTests();

    when(deviceKeys.requireByDeviceIdAndAgentInstanceIdAndStatus(
            DEVICE_ID,
            AGENT_INSTANCE_ID,
            DeviceKeyStatus.ACTIVE,
            SimpleGuardErrorCode.DEVICE_CREDENTIAL_REVOKED,
            SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_REVOKED))
        .thenReturn(deviceKey);
    when(signatureVerifier.verifyTelemetry(
            deviceKey.getPublicKey(), DEVICE_ID, AGENT_INSTANCE_ID, request, "signature"))
        .thenReturn(true);
    when(locations.resolveLocation(DEVICE_ID, EVENT_ID, request.location(), RECEIVED_AT))
        .thenReturn(new DeviceLocationResolution(existingLocation, false));
    when(technicalTelemetry.findById(EVENT_ID)).thenReturn(Optional.of(existingTechnical));

    DeviceTelemetryResponse response =
        service.ingest(DEVICE_ID, AGENT_INSTANCE_ID, "signature", request);

    assertThat(response.locationId()).isEqualTo(EVENT_ID);
    assertThat(response.technicalTelemetryId()).isEqualTo(EVENT_ID);
    assertThat(response.duplicate()).isTrue();
  }

  @Test
  void rejectsInvalidSignatureTests() {
    CreateDeviceTelemetryRequest request = completeRequestTests();
    DeviceKey deviceKey = deviceKeyTests();

    when(deviceKeys.requireByDeviceIdAndAgentInstanceIdAndStatus(
            DEVICE_ID,
            AGENT_INSTANCE_ID,
            DeviceKeyStatus.ACTIVE,
            SimpleGuardErrorCode.DEVICE_CREDENTIAL_REVOKED,
            SimpleGuardTranslation.ERROR_DEVICE_CREDENTIAL_REVOKED))
        .thenReturn(deviceKey);
    when(signatureVerifier.verifyTelemetry(
            deviceKey.getPublicKey(), DEVICE_ID, AGENT_INSTANCE_ID, request, "signature"))
        .thenReturn(false);

    assertThatThrownBy(() -> service.ingest(DEVICE_ID, AGENT_INSTANCE_ID, "signature", request))
        .isInstanceOf(SimpleGuardException.class)
        .satisfies(
            error -> {
              SimpleGuardException exception = (SimpleGuardException) error;
              assertThat(exception.status()).isEqualTo(HttpStatus.UNAUTHORIZED);
            });
  }

  private static CreateDeviceTelemetryRequest completeRequestTests() {
    return new CreateDeviceTelemetryRequest(
        EVENT_ID,
        locationTestsRequest(),
        new TechnicalTelemetryRequest(
            12,
            false,
            "CELLULAR",
            -101,
            new TelemetryPermissionsRequest("GRANTED", "GRANTED"),
            COLLECTED_AT.plusSeconds(2)));
  }

  private static TelemetryLocationRequest locationTestsRequest() {
    return new TelemetryLocationRequest(
        new BigDecimal("-23.55052000"),
        new BigDecimal("-46.63330800"),
        new BigDecimal("4.500"),
        null,
        new BigDecimal("0.000"),
        "GPS",
        COLLECTED_AT);
  }

  private static DeviceLocation locationTests() {
    return DeviceLocation.collected(DEVICE_ID, EVENT_ID, locationTestsRequest(), RECEIVED_AT);
  }

  private static DeviceTelemetry technicalTests() {
    return new DeviceTelemetry(
        EVENT_ID,
        DEVICE_ID,
        12,
        false,
        "CELLULAR",
        -101,
        "GRANTED",
        "GRANTED",
        COLLECTED_AT.plusSeconds(2),
        RECEIVED_AT);
  }

  private static DeviceKey deviceKeyTests() {
    return DeviceKey.active(
        UUID.fromString("00000000-0000-0000-0000-000000002003"),
        DEVICE_ID,
        UUID.fromString("00000000-0000-0000-0000-000000002004"),
        AGENT_INSTANCE_ID,
        DevicePlatform.ANDROID,
        "public-key");
  }
}
