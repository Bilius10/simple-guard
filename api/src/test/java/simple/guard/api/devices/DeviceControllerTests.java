package simple.guard.api.devices;

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
import simple.guard.api.devices.management.domain.DevicePairingStatus;
import simple.guard.api.devices.management.domain.DeviceRepository;
import simple.guard.api.devices.pairing.domain.PairingSessionExpirationReason;
import simple.guard.api.devices.pairing.domain.PairingSessionRepository;
import simple.guard.api.devices.pairing.domain.PairingSessionStatus;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.identity.domain.AccountRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class DeviceControllerTests {

    private static final String ADMINISTRATOR_SUBJECT = "00000000-0000-0000-0000-000000000001";
    private static final UUID ADMINISTRATOR_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private DeviceRepository devices;

    @Autowired
    private PairingSessionRepository pairingSessions;

    @BeforeEach
    void resetDataTests() {
        pairingSessions.deleteAll();
        devices.deleteAll();
        accounts.deleteAll();
        accounts.save(new Account(
                ADMINISTRATOR_ID,
                ADMINISTRATOR_SUBJECT,
                "admin@simpleguard.local",
                "SimpleGuard Admin",
                "ADMIN",
                true,
                "test",
                OffsetDateTime.now(),
                "test",
                OffsetDateTime.now()
        ));
    }

    @Test
    void createsValidUnpairedDeviceTests() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Celular operacional",
                                  "type": "MOBILE",
                                  "platform": "ANDROID"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/devices/.+")))
                .andExpect(jsonPath("$.deviceId").exists())
                .andExpect(jsonPath("$.name").value("Celular operacional"))
                .andExpect(jsonPath("$.type").value("MOBILE"))
                .andExpect(jsonPath("$.platform").value("ANDROID"))
                .andExpect(jsonPath("$.pairingStatus").value("unpaired"))
                .andExpect(jsonPath("$.createdAt").exists());

        assertThat(devices.findAll()).singleElement().satisfies(device -> {
            assertThat(device.getAccountId()).isEqualTo(ADMINISTRATOR_ID);
            assertThat(device.getCreatedBy()).isEqualTo(ADMINISTRATOR_SUBJECT);
            assertThat(device.getUpdatedBy()).isEqualTo(ADMINISTRATOR_SUBJECT);
            assertThat(device.getCreatedAt()).isNotNull();
            assertThat(device.getUpdatedAt()).isNotNull();
        });
    }

    @Test
    void rejectsMissingRequiredFieldsTests() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .header("Authorization", "Bearer valid-token")
                        .header("Accept-Language", "pt-BR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.mensagem").value("Dados invalidos na requisicao."))
                .andExpect(jsonPath("$.uri").value("/api/devices"));

        assertThat(devices.count()).isZero();
    }

    @Test
    void rejectsInvalidPlatformTests() throws Exception {
        mockMvc.perform(post("/api/devices")
                        .header("Authorization", "Bearer valid-token")
                        .header("Accept-Language", "pt-BR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Dispositivo invalido",
                                  "type": "OTHER",
                                  "platform": "SYMBIAN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro_code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.mensagem").value("Dados invalidos na requisicao."))
                .andExpect(jsonPath("$.uri").value("/api/devices"));

        assertThat(devices.count()).isZero();
    }

    @Test
    void listsRegisteredDevicesTests() throws Exception {
        createDeviceTests("Notebook de campo", "NOTEBOOK", "LINUX");
        createDeviceTests("Desktop do escritorio", "DESKTOP", "WINDOWS");

        mockMvc.perform(get("/api/devices")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].pairingStatus").value("unpaired"))
                .andExpect(jsonPath("$[1].pairingStatus").value("unpaired"));
    }

    @Test
    void generatesAuditableShortPairingSessionTests() throws Exception {
        createDeviceTests("Celular operacional", "MOBILE", "ANDROID");
        var device = devices.findAll().getFirst();

        mockMvc.perform(post("/api/devices/{deviceId}/pairing-sessions", device.getId())
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        org.hamcrest.Matchers.matchesPattern("/api/devices/.+/pairing-sessions/.+")
                ))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.pairingSessionId").exists())
                .andExpect(jsonPath("$.deviceId").value(device.getId().toString()))
                .andExpect(jsonPath("$.pairingCode").value(org.hamcrest.Matchers.matchesPattern("[A-Z2-9]{4}-[A-Z2-9]{4}")))
                .andExpect(jsonPath("$.status").value("waiting"))
                .andExpect(jsonPath("$.expiresAt").exists())
                .andExpect(jsonPath("$.createdAt").exists());

        assertThat(pairingSessions.findAll()).singleElement().satisfies(session -> {
            assertThat(session.getCodeHash()).hasSize(64).doesNotContain("-");
            assertThat(session.getStatus()).isEqualTo(PairingSessionStatus.WAITING);
            assertThat(session.getCreatedBy()).isEqualTo(ADMINISTRATOR_SUBJECT);
            assertThat(session.getUpdatedBy()).isEqualTo(ADMINISTRATOR_SUBJECT);
            assertThat(session.getExpiresAt()).isAfter(session.getCreatedAt());
        });
    }

    @Test
    void rejectsNewPairingSessionWhenValidWaitingSessionExistsTests() throws Exception {
        createDeviceTests("Notebook de campo", "NOTEBOOK", "LINUX");
        var device = devices.findAll().getFirst();

        generatePairingSessionTests(device.getId());

        mockMvc.perform(post("/api/devices/{deviceId}/pairing-sessions", device.getId())
                        .header("Authorization", "Bearer valid-token")
                        .header("Accept-Language", "pt-BR"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro_code").value("MAX_OPEN_PAIRING_SESSIONS_REACHED"))
                .andExpect(jsonPath("$.uri").value(
                        "/api/devices/" + device.getId() + "/pairing-sessions"
                ));

        assertThat(pairingSessions.findAll()).singleElement()
                .satisfies(session -> assertThat(session.getStatus()).isEqualTo(PairingSessionStatus.WAITING));
    }

    @Test
    void expiresElapsedWaitingSessionBeforeGeneratingNewPairingSessionTests() throws Exception {
        createDeviceTests("Notebook de campo", "NOTEBOOK", "LINUX");
        var device = devices.findAll().getFirst();

        generatePairingSessionTests(device.getId());
        var elapsedSession = pairingSessions.findAll().getFirst();
        elapsedSession.setExpiresAt(OffsetDateTime.now().minusSeconds(1));
        pairingSessions.save(elapsedSession);

        generatePairingSessionTests(device.getId());

        assertThat(pairingSessions.findAll())
                .hasSize(2)
                .anySatisfy(session -> {
                    assertThat(session.getStatus()).isEqualTo(PairingSessionStatus.EXPIRED);
                    assertThat(session.getExpirationReason()).isEqualTo(PairingSessionExpirationReason.TIMEOUT);
                    assertThat(session.getExpiredAt()).isNotNull();
                })
                .anySatisfy(session -> assertThat(session.getStatus()).isEqualTo(PairingSessionStatus.WAITING));
    }

    @Test
    void rejectsPairingSessionForAlreadyPairedDeviceTests() throws Exception {
        createDeviceTests("Desktop operacional", "DESKTOP", "WINDOWS");
        var device = devices.findAll().getFirst();
        device.setPairingStatus(DevicePairingStatus.PAIRED);
        devices.save(device);

        mockMvc.perform(post("/api/devices/{deviceId}/pairing-sessions", device.getId())
                        .header("Authorization", "Bearer valid-token")
                        .header("Accept-Language", "pt-BR"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_ALREADY_PAIRED"))
                .andExpect(jsonPath("$.mensagem").value("O dispositivo ja esta pareado."))
                .andExpect(jsonPath("$.uri").value(
                        "/api/devices/" + device.getId() + "/pairing-sessions"
                ));

        assertThat(pairingSessions.count()).isZero();
    }

    @Test
    void rejectsPairingSessionForUnknownDeviceTests() throws Exception {
        UUID unknownDeviceId = UUID.randomUUID();

        mockMvc.perform(post("/api/devices/{deviceId}/pairing-sessions", unknownDeviceId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro_code").value("DEVICE_NOT_FOUND"));
    }

    private void createDeviceTests(String name, String type, String platform) throws Exception {
        mockMvc.perform(post("/api/devices")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "%s",
                                  "platform": "%s"
                                }
                                """.formatted(name, type, platform)))
                .andExpect(status().isCreated());
    }

    private void generatePairingSessionTests(UUID deviceId) throws Exception {
        mockMvc.perform(post("/api/devices/{deviceId}/pairing-sessions", deviceId)
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isCreated());
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
                            "iss", "https://idp.localhost/realms/simpleguard",
                            "sub", ADMINISTRATOR_SUBJECT,
                            "email", "admin@simpleguard.local"
                    )
            );
        }
    }
}
