package simple.guard.api.error.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import simple.guard.api.error.domain.ApiErrorResponse;
import simple.guard.api.error.domain.ApiErrorResponseFactory;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

class ApiExceptionHandlerTests {

  private ApiExceptionHandler handler;

  @BeforeEach
  void setUpTests() {
    StaticMessageSource messageSource = new StaticMessageSource();
    messageSource.addMessage(
        "simple_guard_error_validation", Locale.getDefault(), "Dados invalidos na requisicao.");
    messageSource.addMessage(
        "simple_guard_error_system", Locale.getDefault(), "Erro interno do sistema.");
    messageSource.addMessage(
        "simple_guard_error_critical_action_confirmation_required",
        Locale.getDefault(),
        "Acao critica exige confirmacao explicita.");
    handler = new ApiExceptionHandler(new ApiErrorResponseFactory(messageSource));
  }

  @Test
  void handleSimpleGuardExceptionReturnsStandardErrorResponseTests() {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/commands");

    var response =
        handler.handleException(
            new SimpleGuardException(
                HttpStatus.CONFLICT,
                SimpleGuardErrorCode.CRITICAL_ACTION_CONFIRMATION_REQUIRED,
                SimpleGuardTranslation.ERROR_CRITICAL_ACTION_CONFIRMATION_REQUIRED),
            request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    ApiErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.erroCode()).isEqualTo("CRITICAL_ACTION_CONFIRMATION_REQUIRED");
    assertThat(body.mensagem()).isEqualTo("Acao critica exige confirmacao explicita.");
    assertThat(body.uri()).isEqualTo("/api/commands");
    assertThat(body.data()).isNotNull();
  }

  @Test
  void handleValidationExceptionReturnsStandardErrorResponseTests() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/accounts");
    Method method = SampleControllerTests.class.getDeclaredMethod("sample", String.class);
    MethodParameter methodParameter = new MethodParameter(method, 0);
    MethodArgumentNotValidException exception =
        new MethodArgumentNotValidException(
            methodParameter, new BeanPropertyBindingResult(new Object(), "sample"));

    var response = handler.handleException(exception, request);

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

    var response = handler.handleException(new RuntimeException("boom"), request);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    ApiErrorResponse body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.erroCode()).isEqualTo("INTERNAL_ERROR");
    assertThat(body.mensagem()).isEqualTo("Erro interno do sistema.");
    assertThat(body.uri()).isEqualTo("/api/session/me");
    assertThat(body.data()).isNotNull();
  }

  static class SampleControllerTests {
    @SuppressWarnings("unused")
    void sample(String value) {}
  }
}
