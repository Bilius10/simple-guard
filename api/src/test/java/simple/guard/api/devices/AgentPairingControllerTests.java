package simple.guard.api.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import simple.guard.api.devices.keys.domain.DeviceKeyRepository;
import simple.guard.api.devices.keys.domain.DeviceKeyStatus;
import simple.guard.api.devices.management.domain.Device;
import simple.guard.api.devices.management.domain.DevicePairingStatus;
import simple.guard.api.devices.management.domain.DevicePlatform;
import simple.guard.api.devices.management.domain.DeviceRepository;
import simple.guard.api.devices.management.domain.DeviceType;
import simple.guard.api.devices.pairing.domain.PairingSession;
import simple.guard.api.devices.pairing.domain.PairingSessionExpirationReason;
import simple.guard.api.devices.pairing.domain.PairingSessionRepository;
import simple.guard.api.devices.pairing.domain.PairingSessionStatus;
import simple.guard.api.devices.pairing.service.PairingCodeHasher;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.identity.domain.AccountRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
class AgentPairingControllerTests {

    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000401");
    private static final String PAIRING_CODE = "ABCD-2345";
    private static final String AGENT_INSTANCE_ID = "android-agent-001";
    private static final OffsetDateTime NOW = OffsetDateTime.of(
            2026, 8, 9, 12, 0, 0, 0, ZoneOffset.UTC
    );

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
    private PairingCodeHasher codeHasher;

    @BeforeEach
    void resetDataTests() {
        deviceKeys.deleteAll();
        pairingSessions.deleteAll();
        devices.deleteAll();
        accounts.deleteAll();
        accounts.save(accountTests());
        devices.save(deviceTests(DevicePairingStatus.UNPAIRED));
    }

    @Test
    void completesAgentPairingWithValidCodeAndPublicKeyTests() throws Exception {
        PairingSession session = pairingSessions.save(waitingSessionTests(validExpiresAtTests()));

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pairingRequestTests(PAIRING_CODE, AGENT_INSTANCE_ID, publicKeyTests())))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.deviceId").value(DEVICE_ID.toString()))
                .andExpect(jsonPath("$.deviceName").value("Celular operacional"))
                .andExpect(jsonPath("$.platform").value("ANDROID"))
                .andExpect(jsonPath("$.pairingStatus").value("paired"))
                .andExpect(jsonPath("$.pairedAt").exists());

        assertThat(devices.findById(DEVICE_ID)).get().satisfies(device -> {
            assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.PAIRED);
            assertThat(device.getUpdatedBy()).isEqualTo("agent:" + AGENT_INSTANCE_ID);
            assertThat(device.getUpdatedAt()).isNotNull();
        });
        assertThat(pairingSessions.findById(session.getId())).get().satisfies(usedSession -> {
            assertThat(usedSession.getStatus()).isEqualTo(PairingSessionStatus.USED);
            assertThat(usedSession.getUsedAt()).isNotNull();
            assertThat(usedSession.getUpdatedBy()).isEqualTo("agent:" + AGENT_INSTANCE_ID);
        });
        assertThat(deviceKeys.findAll()).singleElement().satisfies(deviceKey -> {
            assertThat(deviceKey.getDeviceId()).isEqualTo(DEVICE_ID);
            assertThat(deviceKey.getPairingSessionId()).isEqualTo(session.getId());
            assertThat(deviceKey.getAgentInstanceId()).isEqualTo(AGENT_INSTANCE_ID);
            assertThat(deviceKey.getPlatform()).isEqualTo(DevicePlatform.ANDROID);
            assertThat(deviceKey.getStatus()).isEqualTo(DeviceKeyStatus.ACTIVE);
            assertThat(deviceKey.getPublicKey()).isEqualTo(publicKeyTests());
            assertThat(deviceKey.getCreatedBy()).isEqualTo("agent:" + AGENT_INSTANCE_ID);
            assertThat(deviceKey.getCreatedAt()).isNotNull();
        });
    }

    @Test
    void completesDesktopAgentPairingWithSecureAgentContractTests() throws Exception {
        devices.deleteAll();
        devices.save(deviceTests(
                DevicePairingStatus.UNPAIRED,
                DeviceType.DESKTOP,
                DevicePlatform.WINDOWS,
                "Desktop operacional"
        ));
        PairingSession session = pairingSessions.save(waitingSessionTests(validExpiresAtTests()));
        String desktopAgentInstanceId = "desktop-windows-agent-001";

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pairingRequestTests(
                                PAIRING_CODE,
                                desktopAgentInstanceId,
                                DevicePlatform.WINDOWS,
                                publicKeyTests()
                        )))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.deviceId").value(DEVICE_ID.toString()))
                .andExpect(jsonPath("$.deviceName").value("Desktop operacional"))
                .andExpect(jsonPath("$.platform").value("WINDOWS"))
                .andExpect(jsonPath("$.pairingStatus").value("paired"))
                .andExpect(jsonPath("$.pairedAt").exists());

        assertThat(devices.findById(DEVICE_ID)).get().satisfies(device -> {
            assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.PAIRED);
            assertThat(device.getUpdatedBy()).isEqualTo("agent:" + desktopAgentInstanceId);
        });
        assertThat(pairingSessions.findById(session.getId())).get()
                .satisfies(usedSession -> assertThat(usedSession.getStatus()).isEqualTo(PairingSessionStatus.USED));
        assertThat(deviceKeys.findAll()).singleElement().satisfies(deviceKey -> {
            assertThat(deviceKey.getDeviceId()).isEqualTo(DEVICE_ID);
            assertThat(deviceKey.getPairingSessionId()).isEqualTo(session.getId());
            assertThat(deviceKey.getAgentInstanceId()).isEqualTo(desktopAgentInstanceId);
            assertThat(deviceKey.getPlatform()).isEqualTo(DevicePlatform.WINDOWS);
            assertThat(deviceKey.getStatus()).isEqualTo(DeviceKeyStatus.ACTIVE);
            assertThat(deviceKey.getPublicKey()).isEqualTo(publicKeyTests());
        });
    }

    @Test
    void rejectsAgentPairingWithInvalidCodeTests() throws Exception {
        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pairingRequestTests("ZZZZ-9999", AGENT_INSTANCE_ID, publicKeyTests())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro_code").value("PAIRING_SESSION_INVALID"))
                .andExpect(jsonPath("$.uri").value("/api/agent/pairing/complete"));

        assertThat(deviceKeys.count()).isZero();
        assertThat(devices.findById(DEVICE_ID)).get()
                .satisfies(device -> assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.UNPAIRED));
    }

    @Test
    void rejectsAgentPairingWithExpiredCodeTests() throws Exception {
        PairingSession session = pairingSessions.save(waitingSessionTests(OffsetDateTime.now().minusSeconds(1)));

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pairingRequestTests(PAIRING_CODE, AGENT_INSTANCE_ID, publicKeyTests())))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro_code").value("PAIRING_SESSION_EXPIRED"))
                .andExpect(jsonPath("$.uri").value("/api/agent/pairing/complete"));

        assertThat(deviceKeys.count()).isZero();
        assertThat(devices.findById(DEVICE_ID)).get()
                .satisfies(device -> assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.UNPAIRED));
        assertThat(pairingSessions.findById(session.getId())).get().satisfies(currentSession -> {
            assertThat(currentSession.getStatus()).isEqualTo(PairingSessionStatus.WAITING);
            assertThat(currentSession.getExpiredAt()).isNull();
        });
    }

    @Test
    void rejectsAgentPairingWithAlreadyUsedCodeTests() throws Exception {
        PairingSession usedSession = waitingSessionTests(validExpiresAtTests());
        usedSession.setStatus(PairingSessionStatus.USED);
        usedSession.setUsedAt(OffsetDateTime.now().minusMinutes(1));
        pairingSessions.save(usedSession);

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pairingRequestTests(PAIRING_CODE, AGENT_INSTANCE_ID, publicKeyTests())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro_code").value("PAIRING_SESSION_ALREADY_USED"))
                .andExpect(jsonPath("$.uri").value("/api/agent/pairing/complete"));

        assertThat(deviceKeys.count()).isZero();
        assertThat(devices.findById(DEVICE_ID)).get()
                .satisfies(device -> assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.UNPAIRED));
    }

    @Test
    void rejectsAgentPairingWithExpiredSessionStatusTests() throws Exception {
        PairingSession expiredSession = waitingSessionTests(validExpiresAtTests());
        expiredSession.setStatus(PairingSessionStatus.EXPIRED);
        expiredSession.setExpirationReason(PairingSessionExpirationReason.TIMEOUT);
        expiredSession.setExpiredAt(OffsetDateTime.now().minusMinutes(1));
        pairingSessions.save(expiredSession);

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pairingRequestTests(PAIRING_CODE, AGENT_INSTANCE_ID, publicKeyTests())))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.erro_code").value("PAIRING_SESSION_EXPIRED"))
                .andExpect(jsonPath("$.uri").value("/api/agent/pairing/complete"));

        assertThat(deviceKeys.count()).isZero();
        assertThat(devices.findById(DEVICE_ID)).get()
                .satisfies(device -> assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.UNPAIRED));
    }

    @Test
    void rejectsAgentPairingWhenSessionDeviceDoesNotExistTests() throws Exception {
        pairingSessions.save(waitingSessionTests(validExpiresAtTests()));
        devices.deleteAll();

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pairingRequestTests(PAIRING_CODE, AGENT_INSTANCE_ID, publicKeyTests())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_NOT_FOUND"))
                .andExpect(jsonPath("$.uri").value("/api/agent/pairing/complete"));

        assertThat(deviceKeys.count()).isZero();
    }

    @Test
    void rejectsAgentPairingWhenPublicKeyIsMissingTests() throws Exception {
        pairingSessions.save(waitingSessionTests(validExpiresAtTests()));

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pairingCode": "ABCD-2345",
                                  "agentInstanceId": "android-agent-001",
                                  "platform": "ANDROID"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.uri").value("/api/agent/pairing/complete"));

        assertThat(deviceKeys.count()).isZero();
    }

    @Test
    void rejectsDesktopAgentPairingWhenPublicKeyIsMissingTests() throws Exception {
        devices.deleteAll();
        devices.save(deviceTests(
                DevicePairingStatus.UNPAIRED,
                DeviceType.DESKTOP,
                DevicePlatform.WINDOWS,
                "Desktop operacional"
        ));
        pairingSessions.save(waitingSessionTests(validExpiresAtTests()));

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pairingCode": "ABCD-2345",
                                  "agentInstanceId": "desktop-windows-agent-001",
                                  "platform": "WINDOWS"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.uri").value("/api/agent/pairing/complete"));

        assertThat(deviceKeys.count()).isZero();
        assertThat(devices.findById(DEVICE_ID)).get()
                .satisfies(device -> assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.UNPAIRED));
    }

    @Test
    void rejectsAgentPairingWhenDeviceIsAlreadyPairedTests() throws Exception {
        Device device = devices.findById(DEVICE_ID).orElseThrow();
        device.setPairingStatus(DevicePairingStatus.PAIRED);
        devices.save(device);
        pairingSessions.save(waitingSessionTests(validExpiresAtTests()));

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pairingRequestTests(PAIRING_CODE, AGENT_INSTANCE_ID, publicKeyTests())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_ALREADY_PAIRED"))
                .andExpect(jsonPath("$.uri").value("/api/agent/pairing/complete"));

        assertThat(deviceKeys.count()).isZero();
    }

    @Test
    void rejectsAgentPairingWhenPlatformDoesNotMatchRegisteredDeviceTests() throws Exception {
        Device device = devices.findById(DEVICE_ID).orElseThrow();
        device.setPlatform(DevicePlatform.WINDOWS);
        devices.save(device);
        PairingSession session = pairingSessions.save(waitingSessionTests(validExpiresAtTests()));

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pairingRequestTests(PAIRING_CODE, AGENT_INSTANCE_ID, publicKeyTests())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_PLATFORM_MISMATCH"))
                .andExpect(jsonPath("$.uri").value("/api/agent/pairing/complete"));

        assertThat(deviceKeys.count()).isZero();
        assertThat(pairingSessions.findById(session.getId())).get()
                .satisfies(currentSession -> assertThat(currentSession.getStatus()).isEqualTo(PairingSessionStatus.WAITING));
    }

    @Test
    void rejectsDesktopAgentPairingWhenPlatformDoesNotMatchRegisteredDeviceTests() throws Exception {
        devices.deleteAll();
        devices.save(deviceTests(
                DevicePairingStatus.UNPAIRED,
                DeviceType.DESKTOP,
                DevicePlatform.WINDOWS,
                "Desktop operacional"
        ));
        PairingSession session = pairingSessions.save(waitingSessionTests(validExpiresAtTests()));

        mockMvc.perform(post("/api/agent/pairing/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pairingRequestTests(
                                PAIRING_CODE,
                                "desktop-linux-agent-001",
                                DevicePlatform.LINUX,
                                publicKeyTests()
                        )))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_PLATFORM_MISMATCH"))
                .andExpect(jsonPath("$.uri").value("/api/agent/pairing/complete"));

        assertThat(deviceKeys.count()).isZero();
        assertThat(devices.findById(DEVICE_ID)).get()
                .satisfies(device -> assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.UNPAIRED));
        assertThat(pairingSessions.findById(session.getId())).get()
                .satisfies(currentSession -> assertThat(currentSession.getStatus()).isEqualTo(PairingSessionStatus.WAITING));
    }

    private static Account accountTests() {
        return new Account(
                ACCOUNT_ID,
                "administrator-subject",
                "admin@simpleguard.local",
                "SimpleGuard Admin",
                "ADMIN",
                true,
                "test",
                NOW,
                "test",
                NOW
        );
    }

    private static Device deviceTests(DevicePairingStatus status) {
        return deviceTests(status, DeviceType.MOBILE, DevicePlatform.ANDROID, "Celular operacional");
    }

    private static Device deviceTests(
            DevicePairingStatus status,
            DeviceType type,
            DevicePlatform platform,
            String name
    ) {
        return new Device(
                DEVICE_ID,
                ACCOUNT_ID,
                name,
                type,
                platform,
                status,
                "administrator-subject",
                NOW,
                "administrator-subject",
                NOW
        );
    }

    private PairingSession waitingSessionTests(OffsetDateTime expiresAt) {
        return new PairingSession(
                UUID.randomUUID(),
                DEVICE_ID,
                ACCOUNT_ID,
                codeHasher.hash(PAIRING_CODE),
                PairingSessionStatus.WAITING,
                null,
                expiresAt,
                null,
                null,
                "administrator-subject",
                NOW,
                "administrator-subject",
                NOW,
                0L
        );
    }

    private static OffsetDateTime validExpiresAtTests() {
        return OffsetDateTime.now().plusMinutes(5);
    }

    private static String pairingRequestTests(String pairingCode, String agentInstanceId, String publicKey) {
        return pairingRequestTests(pairingCode, agentInstanceId, DevicePlatform.ANDROID, publicKey);
    }

    private static String pairingRequestTests(
            String pairingCode,
            String agentInstanceId,
            DevicePlatform platform,
            String publicKey
    ) {
        return """
                {
                  "pairingCode": "%s",
                  "agentInstanceId": "%s",
                  "platform": "%s",
                  "publicKey": "%s"
                }
                """.formatted(pairingCode, agentInstanceId, platform.name(), publicKey);
    }

    private static String publicKeyTests() {
        return "public-key-" + "A".repeat(80);
    }
}
