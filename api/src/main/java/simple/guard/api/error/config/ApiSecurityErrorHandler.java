package simple.guard.api.error.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import simple.guard.api.error.domain.ApiErrorResponse;
import simple.guard.api.error.domain.ApiErrorResponseFactory;
import simple.guard.api.error.domain.SimpleGuardErrorCode;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ApiErrorResponseFactory errorResponseFactory;
    private final ObjectMapper objectMapper;

    public ApiSecurityErrorHandler(ApiErrorResponseFactory errorResponseFactory, ObjectMapper objectMapper) {
        this.errorResponseFactory = errorResponseFactory;
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        ApiErrorResponse error = errorResponseFactory.create(
                SimpleGuardErrorCode.INVALID_TOKEN,
                SimpleGuardTranslation.ERROR_INVALID_TOKEN,
                request.getRequestURI()
        );
        write(response, HttpStatus.UNAUTHORIZED, error);
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        ApiErrorResponse error = errorResponseFactory.create(
                SimpleGuardErrorCode.ACCESS_DENIED,
                SimpleGuardTranslation.ERROR_ACCESS_DENIED,
                request.getRequestURI()
        );
        write(response, HttpStatus.FORBIDDEN, error);
    }

    private void write(HttpServletResponse response, HttpStatus status, ApiErrorResponse error) throws IOException {
        response.setStatus(status.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), error);
    }
}
