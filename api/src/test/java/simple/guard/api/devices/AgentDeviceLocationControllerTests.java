package simple.guard.api.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import simple.guard.api.devices.devicelocation.domain.DeviceLocationRepository;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.device.domain.DevicePairingStatus;
import simple.guard.api.devices.device.domain.DevicePlatform;
import simple.guard.api.devices.device.domain.DeviceRepository;
import simple.guard.api.devices.device.domain.DeviceType;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.domain.DeviceKeyRepository;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.pairingsession.domain.PairingSessionRepository;
import simple.guard.api.devices.pairingsession.service.AgentSignatureVerifier;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.identity.domain.AccountRepository;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "simpleguard.instance-id=test-instance",
        "simpleguard.public-url=http://localhost",
        "simpleguard.oidc.issuer-uri=https://idp.localhost/realms/simpleguard",
        "simpleguard.oidc.jwk-set-uri=http://keycloak:8080/realms/simpleguard/protocol/openid-connect/certs",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AgentDeviceLocationControllerTests {

    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000901");
    private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000902");
    private static final String AGENT_INSTANCE_ID = "android-agent-location";
    private static final OffsetDateTime COLLECTED_AT = OffsetDateTime.parse("2026-08-17T09:00:00-03:00");
    private static final BigDecimal LATITUDE = new BigDecimal("-23.55052000");
    private static final BigDecimal LONGITUDE = new BigDecimal("-46.63330800");
    private static final BigDecimal ACCURACY = new BigDecimal("4.500");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private DeviceRepository devices;

    @Autowired
    private PairingSessionRepository pairingSessions;

    @Autowired
    private DeviceKeyRepository deviceKeys;

    @Autowired
    private DeviceLocationRepository deviceLocations;

    private KeyPair keyPair;

    @BeforeEach
    void resetDataTests() throws Exception {
        deviceLocations.deleteAll();
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
    void ingestsValidSignedLocationTests() throws Exception {
        OffsetDateTime beforeRequest = OffsetDateTime.now().minusSeconds(1);

        mockMvc.perform(post("/api/agent/devices/{deviceId}/locations", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", validSignatureTests())
                        .contentType("application/json")
                        .content(validPayloadTests()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.locationId").exists())
                .andExpect(jsonPath("$.deviceId").value(DEVICE_ID.toString()))
                .andExpect(jsonPath("$.collectedAt").value("2026-08-17T12:00:00Z"))
                .andExpect(jsonPath("$.receivedAt").exists());

        assertThat(deviceLocations.findAll()).singleElement().satisfies(location -> {
            assertThat(location.getDeviceId()).isEqualTo(DEVICE_ID);
            assertThat(location.getPosition().getY()).isEqualTo(LATITUDE.doubleValue());
            assertThat(location.getPosition().getX()).isEqualTo(LONGITUDE.doubleValue());
            assertThat(location.getPosition().getSRID()).isEqualTo(4326);
            assertThat(location.getAccuracyMeters()).isEqualByComparingTo(ACCURACY);
            assertThat(location.getAltitudeMeters()).isNull();
            assertThat(location.getSpeedMetersPerSecond()).isNull();
            assertThat(location.getProvider()).isEqualTo("GPS");
            assertThat(location.getCollectedAt()).isEqualTo(COLLECTED_AT);
            assertThat(location.getReceivedAt()).isAfter(beforeRequest);
            assertThat(location.getReceivedAt()).isNotEqualTo(location.getCollectedAt());
        });
    }

    @Test
    void rejectsInvalidLocationSignatureTests() throws Exception {
        mockMvc.perform(post("/api/agent/devices/{deviceId}/locations", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", Base64.getEncoder().encodeToString("invalid".getBytes()))
                        .contentType("application/json")
                        .content(validPayloadTests()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_CREDENTIAL_INVALID"));

        assertThat(deviceLocations.count()).isZero();
    }

    @Test
    void rejectsLocationFromRevokedDeviceTests() throws Exception {
        DeviceKey key = deviceKeys.findAll().getFirst();
        key.setStatus(DeviceKeyStatus.REVOKED);
        key.setRevokedBy("administrator-subject");
        key.setRevokedAt(OffsetDateTime.now());
        deviceKeys.saveAndFlush(key);

        mockMvc.perform(post("/api/agent/devices/{deviceId}/locations", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", validSignatureTests())
                        .contentType("application/json")
                        .content(validPayloadTests()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_CREDENTIAL_REVOKED"));

        assertThat(deviceLocations.count()).isZero();
    }

    @Test
    void rejectsInvalidCoordinatesTests() throws Exception {
        mockMvc.perform(post("/api/agent/devices/{deviceId}/locations", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", validSignatureTests())
                        .contentType("application/json")
                        .content(validPayloadTests().replace("-23.55052000", "91.00000000")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro_code").value("VALIDATION_ERROR"));

        assertThat(deviceLocations.count()).isZero();
    }

    private DeviceKey activeKeyTests() {
        return DeviceKey.active(
                UUID.randomUUID(),
                DEVICE_ID,
                UUID.randomUUID(),
                AGENT_INSTANCE_ID,
                DevicePlatform.ANDROID,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
        );
    }

    private String validSignatureTests() throws Exception {
        byte[] payload = AgentSignatureVerifier.locationPayload(
                DEVICE_ID,
                AGENT_INSTANCE_ID,
                COLLECTED_AT,
                LATITUDE,
                LONGITUDE,
                ACCURACY,
                null,
                null,
                "GPS"
        );
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(payload);
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    private static String validPayloadTests() {
        return """
                {
                  "latitude": -23.55052000,
                  "longitude": -46.63330800,
                  "accuracyMeters": 4.500,
                  "altitudeMeters": null,
                  "speedMetersPerSecond": null,
                  "provider": "GPS",
                  "collectedAt": "2026-08-17T09:00:00-03:00"
                }
                """;
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
                now
        );
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
                now
        );
    }
}
