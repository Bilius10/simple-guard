package simple.guard.api.devices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.device.domain.DevicePairingStatus;
import simple.guard.api.devices.device.domain.DevicePlatform;
import simple.guard.api.devices.device.domain.DeviceRepository;
import simple.guard.api.devices.device.domain.DeviceType;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.domain.DeviceKeyRepository;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.devicelocation.domain.DeviceLocationRepository;
import simple.guard.api.devices.devicetelemetry.controller.request.CreateDeviceTelemetryBatchRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.CreateDeviceTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.SignedDeviceTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TechnicalTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryLocationRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryPermissionsRequest;
import simple.guard.api.devices.devicetelemetry.domain.DeviceTelemetryRepository;
import simple.guard.api.devices.pairingsession.domain.PairingSessionRepository;
import simple.guard.api.devices.pairingsession.service.AgentSignatureVerifier;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.identity.domain.AccountRepository;

@AutoConfigureMockMvc
@SpringBootTest(
    properties = {
      "simpleguard.instance-id=test-instance",
      "simpleguard.public-url=http://localhost",
      "simpleguard.oidc.issuer-uri=https://idp.localhost/realms/simpleguard",
      "simpleguard.oidc.jwk-set-uri=http://keycloak:8080/realms/simpleguard/protocol/openid-connect/certs",
      "spring.flyway.enabled=false",
      "spring.jpa.hibernate.ddl-auto=create-drop"
    })
class AgentDeviceTelemetryControllerTests {

  private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000901");
  private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000902");
  private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000903");
  private static final String AGENT_INSTANCE_ID = "android-agent-telemetry";
  private static final OffsetDateTime COLLECTED_AT =
      OffsetDateTime.parse("2026-08-17T09:00:00-03:00");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private AccountRepository accounts;
  @Autowired private DeviceRepository devices;
  @Autowired private PairingSessionRepository pairingSessions;
  @Autowired private DeviceKeyRepository deviceKeys;
  @Autowired private DeviceLocationRepository locations;
  @Autowired private DeviceTelemetryRepository technicalTelemetry;

  private KeyPair keyPair;

  @BeforeEach
  void resetDataTests() throws Exception {
    technicalTelemetry.deleteAll();
    locations.deleteAll();
    deviceKeys.deleteAll();
    pairingSessions.deleteAll();
    devices.deleteAll();
    accounts.deleteAll();
    accounts.save(accountTests());
    devices.save(deviceTests());
    keyPair = keyPairTests();
    deviceKeys.save(activeKeyTests());
  }

  @Test
  void ingestsLocationAndTechnicalTelemetryInOneRequestTests() throws Exception {
    CreateDeviceTelemetryRequest request = completeRequestTests();
    OffsetDateTime beforeRequest = OffsetDateTime.now().minusSeconds(1);

    performTests(request, validSignatureTests(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.eventId").value(EVENT_ID.toString()))
        .andExpect(jsonPath("$.deviceId").value(DEVICE_ID.toString()))
        .andExpect(jsonPath("$.locationId").value(EVENT_ID.toString()))
        .andExpect(jsonPath("$.technicalTelemetryId").value(EVENT_ID.toString()))
        .andExpect(jsonPath("$.duplicate").value(false));

    assertThat(locations.findAll())
        .singleElement()
        .satisfies(
            location -> {
              assertThat(location.getDeviceId()).isEqualTo(DEVICE_ID);
              assertThat(location.getPosition().getY()).isEqualTo(-23.55052);
              assertThat(location.getPosition().getX()).isEqualTo(-46.633308);
              assertThat(location.getPosition().getSRID()).isEqualTo(4326);
              assertThat(location.getAccuracyMeters()).isEqualByComparingTo("4.500");
              assertThat(location.getAltitudeMeters()).isNull();
              assertThat(location.getSpeedMetersPerSecond()).isEqualByComparingTo("0.000");
              assertThat(location.getProvider()).isEqualTo("GPS");
              assertThat(location.getCollectedAt()).isEqualTo(COLLECTED_AT);
              assertThat(location.getReceivedAt()).isAfter(beforeRequest);
            });
    assertThat(technicalTelemetry.findAll())
        .singleElement()
        .satisfies(
            telemetry -> {
              assertThat(telemetry.getDeviceId()).isEqualTo(DEVICE_ID);
              assertThat(telemetry.getBatteryLevelPercentage()).isEqualTo(12);
              assertThat(telemetry.getBatteryCharging()).isFalse();
              assertThat(telemetry.getNetworkType()).isEqualTo("CELLULAR");
              assertThat(telemetry.getSignalStrengthDbm()).isEqualTo(-101);
              assertThat(telemetry.getFineLocationPermission()).isEqualTo("GRANTED");
              assertThat(telemetry.getCoarseLocationPermission()).isEqualTo("GRANTED");
              assertThat(telemetry.getCollectedAt()).isEqualTo(COLLECTED_AT.plusSeconds(2));
              assertThat(telemetry.getReceivedAt()).isAfter(beforeRequest);
            });
  }

  @Test
  void persistsUnavailableTechnicalValuesAsNullTests() throws Exception {
    CreateDeviceTelemetryRequest request =
        new CreateDeviceTelemetryRequest(
            EVENT_ID,
            null,
            new TechnicalTelemetryRequest(null, null, null, null, null, COLLECTED_AT));

    performTests(request, validSignatureTests(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.locationId").doesNotExist())
        .andExpect(jsonPath("$.technicalTelemetryId").value(EVENT_ID.toString()));

    assertThat(locations.count()).isZero();
    assertThat(technicalTelemetry.findAll())
        .singleElement()
        .satisfies(
            telemetry -> {
              assertThat(telemetry.getBatteryLevelPercentage()).isNull();
              assertThat(telemetry.getBatteryCharging()).isNull();
              assertThat(telemetry.getNetworkType()).isNull();
              assertThat(telemetry.getSignalStrengthDbm()).isNull();
              assertThat(telemetry.getFineLocationPermission()).isNull();
              assertThat(telemetry.getCoarseLocationPermission()).isNull();
            });
  }

  @Test
  void acceptsLocationOnlyTelemetryTests() throws Exception {
    CreateDeviceTelemetryRequest request =
        new CreateDeviceTelemetryRequest(EVENT_ID, locationTests(), null);

    performTests(request, validSignatureTests(request))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.locationId").value(EVENT_ID.toString()))
        .andExpect(jsonPath("$.technicalTelemetryId").doesNotExist());

    assertThat(locations.count()).isOne();
    assertThat(technicalTelemetry.count()).isZero();
  }

  @Test
  void ignoresDuplicateTelemetryEventTests() throws Exception {
    CreateDeviceTelemetryRequest request = completeRequestTests();
    String signature = validSignatureTests(request);

    performTests(request, signature).andExpect(status().isCreated());
    performTests(request, signature)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.duplicate").value(true));

    assertThat(locations.count()).isOne();
    assertThat(technicalTelemetry.count()).isOne();
  }

  @Test
  void ingestsOutOfOrderDuplicateAndPartiallyInvalidBatchTests() throws Exception {
    UUID laterEventId = UUID.fromString("00000000-0000-0000-0000-000000000904");
    UUID invalidEventId = UUID.fromString("00000000-0000-0000-0000-000000000905");
    UUID earlierEventId = UUID.fromString("00000000-0000-0000-0000-000000000906");
    CreateDeviceTelemetryRequest later = requestTests(laterEventId, COLLECTED_AT.plusMinutes(5));
    CreateDeviceTelemetryRequest invalid =
        new CreateDeviceTelemetryRequest(invalidEventId, null, null);
    CreateDeviceTelemetryRequest earlier =
        requestTests(earlierEventId, COLLECTED_AT.minusMinutes(5));

    CreateDeviceTelemetryBatchRequest batch =
        new CreateDeviceTelemetryBatchRequest(
            List.of(
                new SignedDeviceTelemetryRequest(validSignatureTests(later), later),
                new SignedDeviceTelemetryRequest(validSignatureTests(later), later),
                new SignedDeviceTelemetryRequest("ignored", invalid),
                new SignedDeviceTelemetryRequest(validSignatureTests(earlier), earlier)));

    mockMvc
        .perform(
            post("/api/agent/devices/{deviceId}/telemetry/batch", DEVICE_ID)
                .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                .header("Accept-Language", "pt-BR")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(batch)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[0].eventId").value(laterEventId.toString()))
        .andExpect(jsonPath("$.results[0].status").value("ACCEPTED"))
        .andExpect(jsonPath("$.results[1].status").value("DUPLICATE"))
        .andExpect(jsonPath("$.results[2].eventId").value(invalidEventId.toString()))
        .andExpect(jsonPath("$.results[2].status").value("INVALID"))
        .andExpect(
            jsonPath("$.results[2].error")
                .value("O evento de telemetria do lote possui dados invalidos."))
        .andExpect(jsonPath("$.results[3].eventId").value(earlierEventId.toString()))
        .andExpect(jsonPath("$.results[3].status").value("ACCEPTED"));

    assertThat(locations.findAll())
        .extracting(location -> location.getCollectedAt().toInstant())
        .containsExactlyInAnyOrder(
            COLLECTED_AT.plusMinutes(5).toInstant(), COLLECTED_AT.minusMinutes(5).toInstant());
    assertThat(technicalTelemetry.count()).isEqualTo(2);
  }

  @Test
  void reportsNullAndUnauthorizedBatchItemsWithoutRejectingWholeBatchTests() throws Exception {
    CreateDeviceTelemetryRequest request = completeRequestTests();
    CreateDeviceTelemetryBatchRequest batch =
        new CreateDeviceTelemetryBatchRequest(
            Arrays.asList(
                null,
                new SignedDeviceTelemetryRequest("ignored", null),
                new SignedDeviceTelemetryRequest(null, request),
                new SignedDeviceTelemetryRequest(" ", request),
                new SignedDeviceTelemetryRequest("invalid", request)));

    mockMvc
        .perform(
            post("/api/agent/devices/{deviceId}/telemetry/batch", DEVICE_ID)
                .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                .header("Accept-Language", "pt-BR")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(batch)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.results[0].eventId").doesNotExist())
        .andExpect(jsonPath("$.results[0].status").value("INVALID"))
        .andExpect(
            jsonPath("$.results[0].error").value("O evento de telemetria do lote e obrigatorio."))
        .andExpect(jsonPath("$.results[1].status").value("INVALID"))
        .andExpect(
            jsonPath("$.results[1].error").value("O evento de telemetria do lote e obrigatorio."))
        .andExpect(jsonPath("$.results[2].status").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.results[2].error").value("A credencial do dispositivo e invalida."))
        .andExpect(jsonPath("$.results[3].status").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.results[3].error").value("A credencial do dispositivo e invalida."))
        .andExpect(jsonPath("$.results[4].eventId").value(EVENT_ID.toString()))
        .andExpect(jsonPath("$.results[4].status").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.results[4].error").value("A credencial do dispositivo e invalida."));

    assertThat(locations.count()).isZero();
    assertThat(technicalTelemetry.count()).isZero();
  }

  @Test
  void rejectsOutOfRangeAndEmptyTelemetryTests() throws Exception {
    CreateDeviceTelemetryRequest invalidRange =
        new CreateDeviceTelemetryRequest(
            EVENT_ID,
            null,
            new TechnicalTelemetryRequest(101, false, "WIFI", -161, null, COLLECTED_AT));
    performTests(invalidRange, "invalid")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro_code").value("VALIDATION_ERROR"));

    CreateDeviceTelemetryRequest empty = new CreateDeviceTelemetryRequest(EVENT_ID, null, null);
    performTests(empty, "invalid")
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro_code").value("VALIDATION_ERROR"));

    assertThat(locations.count()).isZero();
    assertThat(technicalTelemetry.count()).isZero();
  }

  @Test
  void rejectsInvalidOrRevokedCredentialTests() throws Exception {
    CreateDeviceTelemetryRequest request = completeRequestTests();
    performTests(request, Base64.getEncoder().encodeToString("invalid".getBytes()))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.erro_code").value("DEVICE_CREDENTIAL_INVALID"));

    DeviceKey key = deviceKeys.findAll().getFirst();
    key.setStatus(DeviceKeyStatus.REVOKED);
    key.setRevokedBy("administrator-subject");
    key.setRevokedAt(OffsetDateTime.now());
    deviceKeys.saveAndFlush(key);
    performTests(request, validSignatureTests(request))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.erro_code").value("DEVICE_CREDENTIAL_REVOKED"));
  }

  private org.springframework.test.web.servlet.ResultActions performTests(
      CreateDeviceTelemetryRequest request, String signature) throws Exception {
    return mockMvc.perform(
        post("/api/agent/devices/{deviceId}/telemetry", DEVICE_ID)
            .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
            .header("X-Agent-Signature", signature)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(request)));
  }

  private static CreateDeviceTelemetryRequest requestTests(
      UUID eventId, OffsetDateTime collectedAt) {
    return new CreateDeviceTelemetryRequest(
        eventId,
        new TelemetryLocationRequest(
            new BigDecimal("-23.55052000"),
            new BigDecimal("-46.63330800"),
            new BigDecimal("4.500"),
            null,
            new BigDecimal("0.000"),
            "GPS",
            collectedAt),
        new TechnicalTelemetryRequest(
            12,
            false,
            "CELLULAR",
            -101,
            new TelemetryPermissionsRequest("GRANTED", "GRANTED"),
            collectedAt.plusSeconds(2)));
  }

  private String validSignatureTests(CreateDeviceTelemetryRequest request) throws Exception {
    Signature signature = Signature.getInstance("SHA256withECDSA");
    signature.initSign(keyPair.getPrivate());
    signature.update(
        AgentSignatureVerifier.telemetryPayload(DEVICE_ID, AGENT_INSTANCE_ID, request));
    return Base64.getEncoder().encodeToString(signature.sign());
  }

  private static CreateDeviceTelemetryRequest completeRequestTests() {
    return new CreateDeviceTelemetryRequest(
        EVENT_ID,
        locationTests(),
        new TechnicalTelemetryRequest(
            12,
            false,
            "CELLULAR",
            -101,
            new TelemetryPermissionsRequest("GRANTED", "GRANTED"),
            COLLECTED_AT.plusSeconds(2)));
  }

  private static TelemetryLocationRequest locationTests() {
    return new TelemetryLocationRequest(
        new BigDecimal("-23.55052000"),
        new BigDecimal("-46.63330800"),
        new BigDecimal("4.500"),
        null,
        new BigDecimal("0.000"),
        "GPS",
        COLLECTED_AT);
  }

  private DeviceKey activeKeyTests() {
    return DeviceKey.active(
        UUID.randomUUID(),
        DEVICE_ID,
        UUID.randomUUID(),
        AGENT_INSTANCE_ID,
        DevicePlatform.ANDROID,
        Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
  }

  private static KeyPair keyPairTests() throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
    generator.initialize(new ECGenParameterSpec("secp256r1"));
    return generator.generateKeyPair();
  }

  private static Account accountTests() {
    OffsetDateTime now = OffsetDateTime.now();
    return new Account(
        ACCOUNT_ID,
        "administrator-subject",
        "admin@simpleguard.local",
        "SimpleGuard Admin",
        "ADMIN",
        true,
        "test",
        now,
        "test",
        now);
  }

  private static Device deviceTests() {
    OffsetDateTime now = OffsetDateTime.now();
    return new Device(
        DEVICE_ID,
        ACCOUNT_ID,
        "Celular operacional",
        DeviceType.MOBILE,
        DevicePlatform.ANDROID,
        DevicePairingStatus.PAIRED,
        "administrator-subject",
        now,
        "administrator-subject",
        now);
  }
}
