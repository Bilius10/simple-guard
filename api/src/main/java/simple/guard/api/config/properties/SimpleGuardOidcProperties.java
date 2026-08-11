package simple.guard.api.config.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "simpleguard.oidc")
public record SimpleGuardOidcProperties(
        @NotBlank(message = "{simple_guard_oidc_issuer_uri_required}")
        String issuerUri,
        @NotBlank(message = "{simple_guard_oidc_jwk_set_uri_required}")
        String jwkSetUri
) {
}
