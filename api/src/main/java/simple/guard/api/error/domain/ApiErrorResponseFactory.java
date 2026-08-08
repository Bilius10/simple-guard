package simple.guard.api.error.domain;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import simple.guard.api.shared.i18n.SimpleGuardTranslation;

import java.time.Clock;
import java.time.OffsetDateTime;

@Component
public class ApiErrorResponseFactory {

    private final MessageSource messageSource;
    private final Clock clock;

    public ApiErrorResponseFactory(MessageSource messageSource) {
        this.messageSource = messageSource;
        this.clock = Clock.systemUTC();
    }

    public ApiErrorResponse create(SimpleGuardErrorCode errorCode, SimpleGuardTranslation translation, String uri) {
        String message = messageSource.getMessage(
                translation.key(),
                null,
                translation.key(),
                LocaleContextHolder.getLocale()
        );

        return new ApiErrorResponse(
                errorCode.name(),
                message,
                uri,
                OffsetDateTime.now(clock)
        );
    }
}
