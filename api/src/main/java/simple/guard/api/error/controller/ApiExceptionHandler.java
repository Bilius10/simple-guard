package simple.guard.api.error.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import simple.guard.api.error.domain.ApiErrorResponse;
import simple.guard.api.error.domain.ApiErrorResponseFactory;
import simple.guard.api.error.domain.SimpleGuardException;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

@RestControllerAdvice
public class ApiExceptionHandler {

    private final ApiErrorResponseFactory errorResponseFactory;

    public ApiExceptionHandler(ApiErrorResponseFactory errorResponseFactory) {
        this.errorResponseFactory = errorResponseFactory;
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        if (exception instanceof SimpleGuardException simpleGuardException) {
            return ResponseEntity.status(simpleGuardException.status()).body(errorResponseFactory.create(
                    simpleGuardException.errorCode(),
                    simpleGuardException.translation(),
                    request.getRequestURI()
            ));
        }

        if (exception instanceof MethodArgumentNotValidException
                || exception instanceof HttpMessageNotReadableException) {
            return ResponseEntity.badRequest().body(errorResponseFactory.create(
                    SimpleGuardErrorCode.VALIDATION_ERROR,
                    SimpleGuardTranslation.ERROR_VALIDATION,
                    request.getRequestURI()
            ));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponseFactory.create(
                SimpleGuardErrorCode.INTERNAL_ERROR,
                SimpleGuardTranslation.ERROR_SYSTEM,
                request.getRequestURI()
        ));
    }
}
