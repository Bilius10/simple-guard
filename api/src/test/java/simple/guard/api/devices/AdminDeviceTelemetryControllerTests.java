package simple.guard.api.devices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.device.domain.DevicePairingStatus;
import simple.guard.api.devices.device.domain.DevicePlatform;
import simple.guard.api.devices.device.domain.DeviceRepository;
import simple.guard.api.devices.device.domain.DeviceType;
import simple.guard.api.devices.devicekey.domain.DeviceKeyRepository;
import simple.guard.api.devices.devicelocation.domain.DeviceLocation;
import simple.guard.api.devices.devicelocation.domain.DeviceLocationRepository;
import simple.guard.api.devices.devicetelemetry.controller.request.TechnicalTelemetryRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryLocationRequest;
import simple.guard.api.devices.devicetelemetry.controller.request.TelemetryPermissionsRequest;
import simple.guard.api.devices.devicetelemetry.domain.DeviceTelemetry;
import simple.guard.api.devices.devicetelemetry.domain.DeviceTelemetryRepository;
import simple.guard.api.devices.pairingsession.domain.PairingSessionRepository;
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
class AdminDeviceTelemetryControllerTests {

  private static final String ADMINISTRATOR_SUBJECT = "00000000-0000-0000-0000-000000003001";
  private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000003002");
  private static final UUID OTHER_ACCOUNT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000003003");
  private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000003004");
  private static final UUID OTHER_DEVICE_ID =
      UUID.fromString("00000000-0000-0000-0000-000000003005");
  private static final UUID SECOND_PAIRED_DEVICE_ID =
      UUID.fromString("00000000-0000-0000-0000-000000003006");
  private static final UUID UNPAIRED_DEVICE_ID =
      UUID.fromString("00000000-0000-0000-0000-000000003007");
  private static final OffsetDateTime COLLECTED_AT =
      OffsetDateTime.parse("2026-08-20T10:15:00-03:00");

  @Autowired private MockMvc mockMvc;
  @Autowired private AccountRepository accounts;
  @Autowired private DeviceRepository devices;
  @Autowired private PairingSessionRepository pairingSessions;
  @Autowired private DeviceKeyRepository deviceKeys;
  @Autowired private DeviceLocationRepository locations;
  @Autowired private DeviceTelemetryRepository technicalTelemetry;

  @BeforeEach
  void resetDataTests() {
    technicalTelemetry.deleteAll();
    locations.deleteAll();
    deviceKeys.deleteAll();
    pairingSessions.deleteAll();
    devices.deleteAll();
    accounts.deleteAll();
    accounts.save(accountTests(ACCOUNT_ID, ADMINISTRATOR_SUBJECT, "admin@simpleguard.local"));
    accounts.save(accountTests(OTHER_ACCOUNT_ID, "other-admin", "other@simpleguard.local"));
    devices.save(deviceTests(DEVICE_ID, ACCOUNT_ID, "Celular operacional"));
    devices.save(deviceTests(OTHER_DEVICE_ID, OTHER_ACCOUNT_ID, "Outro dispositivo"));
  }

  @Test
  void returnsLatestDeviceTelemetryForAuthenticatedOwnerTests() throws Exception {
    technicalTelemetry.save(
        technicalTests(
            UUID.fromString("00000000-0000-0000-0000-000000003006"),
            COLLECTED_AT.minusMinutes(5),
            80,
            "WIFI",
            -65));
    technicalTelemetry.save(
        technicalTests(
            UUID.fromString("00000000-0000-0000-0000-000000003007"),
            COLLECTED_AT.plusMinutes(2),
            12,
            "CELLULAR",
            -101));
    locations.save(
        locationTests(UUID.fromString("00000000-0000-0000-0000-000000003008"), COLLECTED_AT));

    mockMvc
        .perform(
            get("/api/devices/{deviceId}/telemetry/latest", DEVICE_ID)
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deviceId").value(DEVICE_ID.toString()))
        .andExpect(jsonPath("$.deviceName").value("Celular operacional"))
        .andExpect(jsonPath("$.lastUpdatedAt").value("2026-08-20T10:17:00-03:00"))
        .andExpect(jsonPath("$.batteryLevelPercentage").value(12))
        .andExpect(jsonPath("$.batteryCharging").value(false))
        .andExpect(jsonPath("$.networkType").value("CELLULAR"))
        .andExpect(jsonPath("$.signalStrengthDbm").value(-101))
        .andExpect(jsonPath("$.latitude").value(-23.55052))
        .andExpect(jsonPath("$.longitude").value(-46.633308))
        .andExpect(jsonPath("$.accuracyMeters").value(4.5));
  }

  @Test
  void returnsUnavailableFieldsAsNullWhenDeviceHasNoTelemetryTests() throws Exception {
    mockMvc
        .perform(
            get("/api/devices/{deviceId}/telemetry/latest", DEVICE_ID)
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deviceId").value(DEVICE_ID.toString()))
        .andExpect(jsonPath("$.deviceName").value("Celular operacional"))
        .andExpect(jsonPath("$.lastUpdatedAt").doesNotExist())
        .andExpect(jsonPath("$.batteryLevelPercentage").doesNotExist())
        .andExpect(jsonPath("$.networkType").doesNotExist())
        .andExpect(jsonPath("$.signalStrengthDbm").doesNotExist())
        .andExpect(jsonPath("$.latitude").doesNotExist())
        .andExpect(jsonPath("$.longitude").doesNotExist())
        .andExpect(jsonPath("$.accuracyMeters").doesNotExist());
  }

  @Test
  void returnsLatestTelemetryListOnlyForAuthenticatedPairedDevicesTests() throws Exception {
    devices.save(deviceTests(SECOND_PAIRED_DEVICE_ID, ACCOUNT_ID, "Notebook operacional"));
    devices.save(
        deviceTests(
            UNPAIRED_DEVICE_ID, ACCOUNT_ID, "Celular nao pareado", DevicePairingStatus.UNPAIRED));

    technicalTelemetry.save(
        technicalTests(
            DEVICE_ID,
            UUID.fromString("00000000-0000-0000-0000-000000003009"),
            COLLECTED_AT,
            80,
            "WIFI",
            -65));
    technicalTelemetry.save(
        technicalTests(
            SECOND_PAIRED_DEVICE_ID,
            UUID.fromString("00000000-0000-0000-0000-000000003010"),
            COLLECTED_AT.plusMinutes(1),
            44,
            "CELLULAR",
            -86));
    technicalTelemetry.save(
        technicalTests(
            UNPAIRED_DEVICE_ID,
            UUID.fromString("00000000-0000-0000-0000-000000003011"),
            COLLECTED_AT.plusMinutes(2),
            10,
            "WIFI",
            -55));
    technicalTelemetry.save(
        technicalTests(
            OTHER_DEVICE_ID,
            UUID.fromString("00000000-0000-0000-0000-000000003012"),
            COLLECTED_AT.plusMinutes(3),
            99,
            "WIFI",
            -40));
    locations.save(
        locationTests(
            DEVICE_ID, UUID.fromString("00000000-0000-0000-0000-000000003013"), COLLECTED_AT));
    locations.save(
        locationTests(
            SECOND_PAIRED_DEVICE_ID,
            UUID.fromString("00000000-0000-0000-0000-000000003014"),
            COLLECTED_AT.plusMinutes(1)));

    mockMvc
        .perform(get("/api/devices/telemetry/latest").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(
            jsonPath(
                "$[*].deviceId",
                containsInAnyOrder(DEVICE_ID.toString(), SECOND_PAIRED_DEVICE_ID.toString())))
        .andExpect(
            jsonPath("$[*].deviceId")
                .value(
                    org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem(UNPAIRED_DEVICE_ID.toString()))))
        .andExpect(
            jsonPath("$[*].deviceId")
                .value(
                    org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem(OTHER_DEVICE_ID.toString()))));
  }

  @Test
  void returnsEmptyLatestTelemetryListWhenAccountHasNoPairedDevicesTests() throws Exception {
    devices.deleteAll();
    devices.save(
        deviceTests(
            UNPAIRED_DEVICE_ID, ACCOUNT_ID, "Celular nao pareado", DevicePairingStatus.UNPAIRED));

    mockMvc
        .perform(get("/api/devices/telemetry/latest").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void rejectsLatestTelemetryForDeviceOutsideAuthenticatedAccountTests() throws Exception {
    mockMvc
        .perform(
            get("/api/devices/{deviceId}/telemetry/latest", OTHER_DEVICE_ID)
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro_code").value("DEVICE_NOT_FOUND"));

    assertThat(technicalTelemetry.count()).isZero();
  }

  private static DeviceTelemetry technicalTests(
      UUID eventId,
      OffsetDateTime collectedAt,
      Integer batteryLevelPercentage,
      String networkType,
      Integer signalStrengthDbm) {
    return technicalTests(
        DEVICE_ID, eventId, collectedAt, batteryLevelPercentage, networkType, signalStrengthDbm);
  }

  private static DeviceTelemetry technicalTests(
      UUID deviceId,
      UUID eventId,
      OffsetDateTime collectedAt,
      Integer batteryLevelPercentage,
      String networkType,
      Integer signalStrengthDbm) {
    return new DeviceTelemetry(
        deviceId,
        eventId,
        new TechnicalTelemetryRequest(
            batteryLevelPercentage,
            false,
            networkType,
            signalStrengthDbm,
            new TelemetryPermissionsRequest("GRANTED", "GRANTED"),
            collectedAt),
        collectedAt.plusSeconds(10));
  }

  private static DeviceLocation locationTests(UUID eventId, OffsetDateTime collectedAt) {
    return locationTests(DEVICE_ID, eventId, collectedAt);
  }

  private static DeviceLocation locationTests(
      UUID deviceId, UUID eventId, OffsetDateTime collectedAt) {
    return DeviceLocation.collected(
        deviceId,
        eventId,
        new TelemetryLocationRequest(
            new BigDecimal("-23.55052000"),
            new BigDecimal("-46.63330800"),
            new BigDecimal("4.500"),
            null,
            new BigDecimal("0.000"),
            "GPS",
            collectedAt),
        collectedAt.plusSeconds(8));
  }

  private static Account accountTests(UUID accountId, String subject, String email) {
    OffsetDateTime now = OffsetDateTime.now();
    return new Account(
        accountId, subject, email, "SimpleGuard Admin", "ADMIN", true, "test", now, "test", now);
  }

  private static Device deviceTests(UUID deviceId, UUID accountId, String name) {
    return deviceTests(deviceId, accountId, name, DevicePairingStatus.PAIRED);
  }

  private static Device deviceTests(
      UUID deviceId, UUID accountId, String name, DevicePairingStatus pairingStatus) {
    OffsetDateTime now = OffsetDateTime.now();
    return new Device(
        deviceId,
        accountId,
        name,
        DeviceType.MOBILE,
        DevicePlatform.ANDROID,
        pairingStatus,
        ADMINISTRATOR_SUBJECT,
        now,
        ADMINISTRATOR_SUBJECT,
        now);
  }

  @TestConfiguration(proxyBeanMethods = false)
  static class JwtDecoderTestConfigurationTests {

    @Bean
    @Primary
    JwtDecoder jwtDecoderTests() {
      return token -> jwtTests(token);
    }

    private static Jwt jwtTests(String token) {
      return new Jwt(
          token,
          Instant.now().minusSeconds(60),
          Instant.now().plusSeconds(300),
          Map.of("alg", "RS256"),
          Map.of(
              "iss",
              "https://idp.localhost/realms/simpleguard",
              "sub",
              ADMINISTRATOR_SUBJECT,
              "email",
              "admin@simpleguard.local"));
    }
  }
}
