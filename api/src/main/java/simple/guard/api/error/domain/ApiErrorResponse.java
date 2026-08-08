package simple.guard.api.error.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record ApiErrorResponse(
        @JsonProperty("erro_code")
        String erroCode,
        String mensagem,
        String uri,
        OffsetDateTime data
) {
}
