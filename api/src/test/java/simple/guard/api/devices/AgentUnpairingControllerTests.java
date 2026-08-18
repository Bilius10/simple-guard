package simple.guard.api.devices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import simple.guard.api.devices.devicekey.domain.DeviceKey;
import simple.guard.api.devices.devicekey.domain.DeviceKeyRepository;
import simple.guard.api.devices.devicekey.domain.DeviceKeyStatus;
import simple.guard.api.devices.device.domain.Device;
import simple.guard.api.devices.device.domain.DevicePairingStatus;
import simple.guard.api.devices.device.domain.DevicePlatform;
import simple.guard.api.devices.device.domain.DeviceRepository;
import simple.guard.api.devices.device.domain.DeviceType;
import simple.guard.api.devices.pairingsession.domain.PairingSessionRepository;
import simple.guard.api.devices.pairingsession.service.AgentSignatureVerifier;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequestRepository;
import simple.guard.api.devices.deviceunpairingrequest.domain.DeviceUnpairingRequestStatus;
import simple.guard.api.devices.deviceunpairingrequest.service.DeviceUnpairingRequestService;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.identity.domain.AccountRepository;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AgentUnpairingControllerTests {

    private static final UUID ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000801");
    private static final UUID DEVICE_ID = UUID.fromString("00000000-0000-0000-0000-000000000802");
    private static final String AGENT_INSTANCE_ID = "android-agent-unpairing";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private DeviceRepository devices;

    @Autowired
    private PairingSessionRepository pairingSessions;

    @Autowired
    private DeviceUnpairingRequestRepository unpairingRequests;

    @Autowired
    private DeviceKeyRepository deviceKeys;

    @Autowired
    private DeviceUnpairingRequestService unpairingRequestService;

    private KeyPair keyPair;

    @BeforeEach
    void resetDataTests() throws Exception {
        unpairingRequests.deleteAll();
        deviceKeys.deleteAll();
        pairingSessions.deleteAll();
        devices.deleteAll();
        accounts.deleteAll();
        accounts.save(accountTests());
        devices.save(deviceTests());
        keyPair = keyPairTests();
        deviceKeys.save(DeviceKey.active(
                UUID.randomUUID(),
                DEVICE_ID,
                UUID.randomUUID(),
                AGENT_INSTANCE_ID,
                DevicePlatform.ANDROID,
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())
        ));
    }

    @Test
    void requestsUnpairingWithValidProofOfPossessionTests() throws Exception {
        mockMvc.perform(delete("/api/agent/devices/{deviceId}/pairing", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", signatureTests()))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.deviceId").value(DEVICE_ID.toString()))
                .andExpect(jsonPath("$.deviceName").value("Celular operacional"))
                .andExpect(jsonPath("$.agentInstanceId").value(AGENT_INSTANCE_ID))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.requestedAt").exists());

        assertThat(devices.findById(DEVICE_ID)).get().satisfies(device -> {
            assertThat(device.getPairingStatus()).isEqualTo(DevicePairingStatus.PAIRED);
        });
        assertThat(deviceKeys.findAll()).singleElement().satisfies(key -> {
            assertThat(key.getStatus()).isEqualTo(DeviceKeyStatus.ACTIVE);
            assertThat(key.getRevokedBy()).isNull();
        });
        assertThat(unpairingRequests.findAll()).singleElement().satisfies(request -> {
            assertThat(request.getDeviceId()).isEqualTo(DEVICE_ID);
            assertThat(request.getAgentInstanceId()).isEqualTo(AGENT_INSTANCE_ID);
            assertThat(request.getStatus()).isEqualTo(DeviceUnpairingRequestStatus.PENDING);
            assertThat(request.getRequestedBy()).isEqualTo("agent:" + AGENT_INSTANCE_ID);
        });
    }

    @Test
    void acceptsRepeatedSignedUnpairingRequestsIdempotentlyTests() throws Exception {
        String signature = signatureTests();
        unpairTests(signature);

        mockMvc.perform(delete("/api/agent/devices/{deviceId}/pairing", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", signature))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("pending"));

        assertThat(unpairingRequests.findAll()).hasSize(1);
        assertThat(deviceKeys.findAll()).singleElement()
                .satisfies(key -> assertThat(key.getStatus()).isEqualTo(DeviceKeyStatus.ACTIVE));
    }

    @Test
    void reportsPendingAndApprovedUnpairingToAgentTests() throws Exception {
        String signature = signatureTests();
        unpairTests(signature);

        mockMvc.perform(get("/api/agent/devices/{deviceId}/pairing", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", signature))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.pairingStatus").value("paired"))
                .andExpect(jsonPath("$.unpairingStatus").value("pending"));

        var request = unpairingRequests.findAll().getFirst();
        unpairingRequestService.decide(
                request.getId(),
                accounts.findById(ACCOUNT_ID).orElseThrow(),
                DeviceUnpairingRequestStatus.APPROVED
        );

        mockMvc.perform(get("/api/agent/devices/{deviceId}/pairing", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", signature))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pairingStatus").value("unpaired"))
                .andExpect(jsonPath("$.unpairingStatus").value("approved"));
    }

    @Test
    void reportsPairedStatusWithoutUnpairingRequestTests() throws Exception {
        mockMvc.perform(get("/api/agent/devices/{deviceId}/pairing", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", signatureTests()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pairingStatus").value("paired"))
                .andExpect(jsonPath("$.unpairingStatus").isEmpty());
    }

    @Test
    void rejectsPairingStatusWithInvalidSignatureTests() throws Exception {
        mockMvc.perform(get("/api/agent/devices/{deviceId}/pairing", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", Base64.getEncoder().encodeToString("invalid".getBytes())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_CREDENTIAL_INVALID"));
    }

    @Test
    void rejectsPairingStatusForUnknownAgentCredentialTests() throws Exception {
        mockMvc.perform(get("/api/agent/devices/{deviceId}/pairing", DEVICE_ID)
                        .header("X-Agent-Instance-Id", "unknown-agent")
                        .header("X-Agent-Signature", signatureTests()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_CREDENTIAL_INVALID"));
    }

    @Test
    void rejectsUnpairingWithInvalidSignatureTests() throws Exception {
        mockMvc.perform(delete("/api/agent/devices/{deviceId}/pairing", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", Base64.getEncoder().encodeToString("invalid".getBytes())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_CREDENTIAL_INVALID"));

        assertThat(deviceKeys.findAll()).singleElement()
                .satisfies(key -> assertThat(key.getStatus()).isEqualTo(DeviceKeyStatus.ACTIVE));
    }

    @Test
    void rejectsUnpairingForUnknownAgentCredentialTests() throws Exception {
        mockMvc.perform(delete("/api/agent/devices/{deviceId}/pairing", DEVICE_ID)
                        .header("X-Agent-Instance-Id", "unknown-agent")
                        .header("X-Agent-Signature", signatureTests()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_CREDENTIAL_INVALID"));
    }

    private void unpairTests(String signature) throws Exception {
        mockMvc.perform(delete("/api/agent/devices/{deviceId}/pairing", DEVICE_ID)
                        .header("X-Agent-Instance-Id", AGENT_INSTANCE_ID)
                        .header("X-Agent-Signature", signature))
                .andExpect(status().isAccepted());
    }

    private String signatureTests() throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(keyPair.getPrivate());
        signature.update(AgentSignatureVerifier.unpairingPayload(DEVICE_ID, AGENT_INSTANCE_ID));
        return Base64.getEncoder().encodeToString(signature.sign());
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


