package simple.guard.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "simpleguard.instance-id=test-instance",
        "simpleguard.public-url=http://localhost",
        "simpleguard.oidc.issuer-uri=https://idp.localhost/realms/simpleguard",
        "simpleguard.oidc.jwk-set-uri=http://keycloak:8080/realms/simpleguard/protocol/openid-connect/certs",
        "spring.flyway.enabled=false"
})
class ApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoadsTests() {
    }

    @Test
    void healthEndpointIsPubliclyAvailableTests() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void nonHealthActuatorEndpointIsDeniedTests() throws Exception {
        mockMvc.perform(get("/actuator/info")
                        .header("Accept-Language", "pt-BR"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro_code").value("INVALID_TOKEN"))
                .andExpect(jsonPath("$.mensagem").value("Token invalido ou sessao expirada."))
                .andExpect(jsonPath("$.uri").value("/actuator/info"))
                .andExpect(jsonPath("$.data").exists());
    }
}
