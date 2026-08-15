package simple.guard.api.identity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import simple.guard.api.identity.domain.Account;
import simple.guard.api.identity.domain.AccountRepository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
class AdministratorOidcSecurityTests {

    private static final String ADMINISTRATOR_SUBJECT = "00000000-0000-0000-0000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accounts;

    @BeforeEach
    void resetAccountsTests() {
        accounts.deleteAll();
        accounts.save(new Account(
                UUID.fromString("00000000-0000-0000-0000-000000000101"),
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
    void acceptsValidTokenForExistingAdministratorTests() throws Exception {
        mockMvc.perform(get("/api/session/me")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value(ADMINISTRATOR_SUBJECT))
                .andExpect(jsonPath("$.email").value("admin@simpleguard.local"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void rejectsExpiredTokenTests() throws Exception {
        mockMvc.perform(get("/api/session/me")
                        .header("Accept-Language", "pt-BR")
                        .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro_code").value("INVALID_TOKEN"))
                .andExpect(jsonPath("$.mensagem").value("Token invalido ou sessao expirada."))
                .andExpect(jsonPath("$.uri").value("/api/session/me"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void rejectsInvalidTokenTests() throws Exception {
        mockMvc.perform(get("/api/session/me")
                        .header("Accept-Language", "pt-BR")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro_code").value("INVALID_TOKEN"))
                .andExpect(jsonPath("$.mensagem").value("Token invalido ou sessao expirada."))
                .andExpect(jsonPath("$.uri").value("/api/session/me"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void rejectsTokenForUnknownAdministratorTests() throws Exception {
        mockMvc.perform(get("/api/session/me")
                        .header("Accept-Language", "pt-BR")
                        .header("Authorization", "Bearer unknown-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro_code").value("INVALID_TOKEN"))
                .andExpect(jsonPath("$.mensagem").value("Token invalido ou sessao expirada."))
                .andExpect(jsonPath("$.uri").value("/api/session/me"))
                .andExpect(jsonPath("$.data").exists());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class JwtDecoderTestConfiguration {

        @Bean
        @Primary
        JwtDecoder jwtDecoderTests() {
            return token -> switch (token) {
                case "valid-token" -> jwtTests(token, ADMINISTRATOR_SUBJECT);
                case "unknown-user-token" -> jwtTests(token, "00000000-0000-0000-0000-000000000999");
                case "expired-token" -> throw new BadJwtException("Token expired");
                default -> throw new BadJwtException("Token invalid");
            };
        }

        private static Jwt jwtTests(String token, String subject) {
            Instant issuedAt = Instant.now().minusSeconds(60);
            Instant expiresAt = Instant.now().plusSeconds(300);
            return new Jwt(
                    token,
                    issuedAt,
                    expiresAt,
                    Map.of("alg", "RS256"),
                    Map.of(
                            "iss", "https://idp.localhost/realms/simpleguard",
                            "sub", subject,
                            "email", "admin@simpleguard.local"
                    )
            );
        }
    }
}


