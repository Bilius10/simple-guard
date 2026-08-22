package simple.guard.api.error.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import simple.guard.api.error.domain.ApiErrorResponseFactory;

class ApiSecurityErrorHandlerTests {

  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
  private ApiSecurityErrorHandler handler;

  @BeforeEach
  void setUpTests() {
    StaticMessageSource messageSource = new StaticMessageSource();
    messageSource.addMessage(
        "simple_guard_error_invalid_token",
        Locale.getDefault(),
        "Token invalido ou sessao expirada.");
    messageSource.addMessage(
        "simple_guard_error_access_denied",
        Locale.getDefault(),
        "Acesso negado para este recurso.");
    handler = new ApiSecurityErrorHandler(new ApiErrorResponseFactory(messageSource), objectMapper);
  }

  @Test
  void commenceReturnsUnauthorizedStandardErrorResponseTests() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/session/me");
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.commence(request, response, new BadCredentialsException("invalid"));

    JsonNode body = objectMapper.readTree(response.getContentAsString());
    assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    assertThat(
            MediaType.parseMediaType(response.getContentType())
                .isCompatibleWith(MediaType.APPLICATION_JSON))
        .isTrue();
    assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
    assertThat(body.get("erro_code").asText()).isEqualTo("INVALID_TOKEN");
    assertThat(body.get("mensagem").asText()).isEqualTo("Token invalido ou sessao expirada.");
    assertThat(body.get("uri").asText()).isEqualTo("/api/session/me");
    assertThat(body.get("data").asText()).isNotBlank();
  }

  @Test
  void handleReturnsForbiddenStandardErrorResponseTests() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("DELETE", "/api/admin");
    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.handle(request, response, new AccessDeniedException("denied"));

    JsonNode body = objectMapper.readTree(response.getContentAsString());
    assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    assertThat(
            MediaType.parseMediaType(response.getContentType())
                .isCompatibleWith(MediaType.APPLICATION_JSON))
        .isTrue();
    assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
    assertThat(body.get("erro_code").asText()).isEqualTo("ACCESS_DENIED");
    assertThat(body.get("mensagem").asText()).isEqualTo("Acesso negado para este recurso.");
    assertThat(body.get("uri").asText()).isEqualTo("/api/admin");
    assertThat(body.get("data").asText()).isNotBlank();
  }
}
