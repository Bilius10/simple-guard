package simple.guard.api.error.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import simple.guard.api.error.domain.ApiErrorResponse;
import simple.guard.api.error.domain.ApiErrorResponseFactory;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final ApiErrorResponseFactory errorResponseFactory;

    public ApiExceptionHandler(ApiErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest().body(errorResponseFactory.create(
                SimpleGuardErrorCode.VALIDATION_ERROR,
                SimpleGuardTranslation.ERROR_VALIDATION,
                request.getRequestURI()
        ));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseFactory.create(
                SimpleGuardErrorCode.INTERNAL_ERROR,
                SimpleGuardTranslation.ERROR_SYSTEM,
                request.getRequestURI()
        ));
    }
}
