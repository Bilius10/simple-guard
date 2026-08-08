package simple.guard.api.error.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import simple.guard.api.error.domain.ApiErrorResponse;
import simple.guard.api.error.domain.ApiErrorResponseFactory;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTests {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUpTests() {
        StaticMessageSource messageSource = new StaticMessageSource();
        messageSource.addMessage("simple_guard_error_validation", Locale.getDefault(), "Dados invalidos na requisicao.");
        messageSource.addMessage("simple_guard_error_system", Locale.getDefault(), "Erro interno do sistema.");
        handler = new ApiExceptionHandler(new ApiErrorResponseFactory(messageSource));
    }

    @Test
    void handleValidationExceptionReturnsStandardErrorResponseTests() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/accounts");

        var response = handler.handleValidationException(null, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ApiErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.erroCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(body.mensagem()).isEqualTo("Dados invalidos na requisicao.");
        assertThat(body.uri()).isEqualTo("/api/accounts");
        assertThat(body.data()).isNotNull();
    }

    @Test
    void handleUnexpectedExceptionReturnsStandardErrorResponseTests() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/session/me");

        var response = handler.handleUnexpectedException(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ApiErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.erroCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(body.mensagem()).isEqualTo("Erro interno do sistema.");
        assertThat(body.uri()).isEqualTo("/api/session/me");
        assertThat(body.data()).isNotNull();
    }
}
