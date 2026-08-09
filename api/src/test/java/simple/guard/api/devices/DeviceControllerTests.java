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
import simple.guard.api.devices.domain.DeviceRepository;
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

    @BeforeEach
    void resetDataTests() {
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
