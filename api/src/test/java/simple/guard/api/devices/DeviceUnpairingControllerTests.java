package simple.guard.api.devices;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.device.domain.DevicePairingStatus;
import simple.guard.api.devices.device.domain.DevicePlatform;
import simple.guard.api.devices.device.domain.DeviceRepository;
import simple.guard.api.devices.device.domain.DeviceType;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.domain.DeviceKeyRepository;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequest;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequestRepository;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequestStatus;
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
class DeviceUnpairingControllerTests {

  private static final String ADMINISTRATOR_SUBJECT = "00000000-0000-0000-0000-000000000001";
  private static final String NON_ADMINISTRATOR_SUBJECT = "00000000-0000-0000-0000-000000000002";
  private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000701");
  private static final UUID NON_ADMIN_ACCOUNT_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000703");
  private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000702");

  @Autowired private MockMvc mockMvc;

  @Autowired private AccountRepository accounts;

  @Autowired private DeviceRepository devices;

  @Autowired private PairingSessionRepository pairingSessions;

  @Autowired private DeviceUnpairingRequestRepository unpairingRequests;

  @Autowired private DeviceKeyRepository deviceKeys;

  @BeforeEach
  void resetDataTests() {
    unpairingRequests.deleteAll();
    deviceKeys.deleteAll();
    pairingSessions.deleteAll();
    devices.deleteAll();
    accounts.deleteAll();
    accounts.save(accountTests(ACCOUNT_ID, ADMINISTRATOR_SUBJECT, "ADMIN"));
    accounts.save(accountTests(NON_ADMIN_ACCOUNT_ID, NON_ADMINISTRATOR_SUBJECT, "USER"));
    devices.save(deviceTests());
    deviceKeys.save(
        DeviceKey.active(
            UUID.randomUUID(),
            DEVICE_ID,
            UUID.randomUUID(),
            "android-agent-001",
            DevicePlatform.ANDROID,
            "public-key-" + "A".repeat(80)));
  }

  @Test
  void revokesActiveKeysAndUpdatesDeviceStateTests() throws Exception {
    mockMvc
        .perform(
            delete("/api/devices/{deviceId}/unpairing", DEVICE_ID)
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.deviceId").value(DEVICE_ID.toString()))
        .andExpect(jsonPath("$.pairingStatus").value("unpaired"))
        .andExpect(jsonPath("$.revokedKeyCount").value(1))
        .andExpect(jsonPath("$.unpairedAt").exists());

    assertThat(devices.findById(DEVICE_ID))
        .get()
        .satisfies(
            device -> {
              assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.UNPAIRED);
              assertThat(device.getUpdatedBy()).isEqualTo(ADMINISTRATOR_SUBJECT);
            });
    assertThat(deviceKeys.findAll())
        .singleElement()
        .satisfies(
            key -> {
              assertThat(key.getStatus()).isEqualTo(DeviceKeyStatus.REVOKED);
              assertThat(key.getRevokedBy()).isEqualTo(ADMINISTRATOR_SUBJECT);
              assertThat(key.getRevokedAt()).isNotNull();
            });
  }

  @Test
  void repeatsUnpairingIdempotentlyTests() throws Exception {
    unpairTests();

    mockMvc
        .perform(
            delete("/api/devices/{deviceId}/unpairing", DEVICE_ID)
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pairingStatus").value("unpaired"))
        .andExpect(jsonPath("$.revokedKeyCount").value(0));

    assertThat(deviceKeys.findAll())
        .singleElement()
        .satisfies(key -> assertThat(key.getStatus()).isEqualTo(DeviceKeyStatus.REVOKED));
  }

  @Test
  void listsPendingUnpairingRequestsTests() throws Exception {
    DeviceUnpairingRequest request = unpairingRequests.save(pendingRequestTests());

    mockMvc
        .perform(
            get("/api/devices/unpairing-requests").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].requestId").value(request.getId().toString()))
        .andExpect(jsonPath("$[0].deviceId").value(DEVICE_ID.toString()))
        .andExpect(jsonPath("$[0].deviceName").value("Celular operacional"))
        .andExpect(jsonPath("$[0].agentInstanceId").value("android-agent-001"))
        .andExpect(jsonPath("$[0].status").value("pending"));
  }

  @Test
  void approvesPendingUnpairingRequestTests() throws Exception {
    DeviceUnpairingRequest request = unpairingRequests.save(pendingRequestTests());

    mockMvc
        .perform(
            post("/api/devices/unpairing-requests/{requestId}/decision", request.getId())
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "status": "approved"
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.request.status").value("approved"))
        .andExpect(jsonPath("$.unpairing.deviceId").value(DEVICE_ID.toString()))
        .andExpect(jsonPath("$.unpairing.pairingStatus").value("unpaired"))
        .andExpect(jsonPath("$.unpairing.revokedKeyCount").value(1));

    assertThat(unpairingRequests.findById(request.getId()))
        .get()
        .satisfies(
            currentRequest -> {
              assertThat(currentRequest.getStatus())
                  .isEqualTo(DeviceUnpairingRequestStatus.APPROVED);
              assertThat(currentRequest.getDecidedBy()).isEqualTo(ADMINISTRATOR_SUBJECT);
              assertThat(currentRequest.getDecidedAt()).isNotNull();
            });
    assertThat(devices.findById(DEVICE_ID))
        .get()
        .satisfies(
            device ->
                assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.UNPAIRED));
    assertThat(deviceKeys.findAll())
        .singleElement()
        .satisfies(
            key -> {
              assertThat(key.getStatus()).isEqualTo(DeviceKeyStatus.REVOKED);
              assertThat(key.getRevokedBy()).isEqualTo(ADMINISTRATOR_SUBJECT);
            });
  }

  @Test
  void rejectsPendingUnpairingRequestTests() throws Exception {
    DeviceUnpairingRequest request = unpairingRequests.save(pendingRequestTests());

    mockMvc
        .perform(
            post("/api/devices/unpairing-requests/{requestId}/decision", request.getId())
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "status": "rejected"
                                }
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.request.status").value("rejected"))
        .andExpect(jsonPath("$.request.decidedAt").exists());

    assertThat(unpairingRequests.findById(request.getId()))
        .get()
        .satisfies(
            currentRequest -> {
              assertThat(currentRequest.getStatus())
                  .isEqualTo(DeviceUnpairingRequestStatus.REJECTED);
              assertThat(currentRequest.getDecidedBy()).isEqualTo(ADMINISTRATOR_SUBJECT);
            });
    assertThat(devices.findById(DEVICE_ID))
        .get()
        .satisfies(
            device -> assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.PAIRED));
    assertThat(deviceKeys.findAll())
        .singleElement()
        .satisfies(key -> assertThat(key.getStatus()).isEqualTo(DeviceKeyStatus.ACTIVE));
  }

  @Test
  void rejectsPendingStatusAsUnpairingRequestDecisionTests() throws Exception {
    DeviceUnpairingRequest request = unpairingRequests.save(pendingRequestTests());

    mockMvc
        .perform(
            post("/api/devices/unpairing-requests/{requestId}/decision", request.getId())
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "status": "pending"
                                }
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro_code").value("VALIDATION_ERROR"));

    assertThat(unpairingRequests.findById(request.getId()))
        .get()
        .satisfies(
            currentRequest ->
                assertThat(currentRequest.getStatus())
                    .isEqualTo(DeviceUnpairingRequestStatus.PENDING));
    assertThat(deviceKeys.findAll())
        .singleElement()
        .satisfies(key -> assertThat(key.getStatus()).isEqualTo(DeviceKeyStatus.ACTIVE));
  }

  @Test
  void rejectsUnknownPendingUnpairingRequestDecisionTests() throws Exception {
    mockMvc
        .perform(
            post("/api/devices/unpairing-requests/{requestId}/decision", UUID.randomUUID())
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "status": "rejected"
                                }
                                """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.erro_code").value("DEVICE_UNPAIRING_REQUEST_NOT_FOUND"));
  }

  @Test
  void rejectsNonAdministratorWhenDecidingUnpairingRequestTests() throws Exception {
    DeviceUnpairingRequest request = unpairingRequests.save(pendingRequestTests());

    mockMvc
        .perform(
            post("/api/devices/unpairing-requests/{requestId}/decision", request.getId())
                .header("Authorization", "Bearer non-admin-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {
                                  "status": "approved"
                                }
                                """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.erro_code").value("ACCESS_DENIED"));

    assertThat(unpairingRequests.findById(request.getId()))
        .get()
        .satisfies(
            currentRequest ->
                assertThat(currentRequest.getStatus())
                    .isEqualTo(DeviceUnpairingRequestStatus.PENDING));
  }

  @Test
  void rejectsDecisionWithoutDecisionValueTests() throws Exception {
    DeviceUnpairingRequest request = unpairingRequests.save(pendingRequestTests());

    mockMvc
        .perform(
            post("/api/devices/unpairing-requests/{requestId}/decision", request.getId())
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.erro_code").value("VALIDATION_ERROR"));
  }

  private void unpairTests() throws Exception {
    mockMvc
        .perform(
            delete("/api/devices/{deviceId}/unpairing", DEVICE_ID)
                .header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk());
  }

  private static DeviceUnpairingRequest pendingRequestTests() {
    OffsetDateTime now = OffsetDateTime.now();
    return DeviceUnpairingRequest.pending(
        UUID.randomUUID(), deviceTests(), "android-agent-001", now);
  }

  private static Account accountTests(UUID accountId, String subject, String role) {
    OffsetDateTime now = OffsetDateTime.now();
    return new Account(
        accountId,
        subject,
        subject + "@simpleguard.local",
        "SimpleGuard " + role,
        role,
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
      return token -> {
        String subject =
            "non-admin-token".equals(token) ? NON_ADMINISTRATOR_SUBJECT : ADMINISTRATOR_SUBJECT;

        return new Jwt(
            token,
            Instant.now().minusSeconds(60),
            Instant.now().plusSeconds(300),
            Map.of("alg", "RS256"),
            Map.of(
                "iss",
                "https://idp.localhost/realms/simpleguard",
                "sub",
                subject,
                "email",
                subject + "@simpleguard.local"));
      };
    }
  }
}
