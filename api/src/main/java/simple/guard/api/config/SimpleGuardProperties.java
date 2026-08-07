package simple.guard.api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "simpleguard")
public record SimpleGuardProperties(
        @NotBlank(message = "SIMPLEGUARD_INSTANCE_ID is required")
        String instanceId,
        @NotBlank(message = "SIMPLEGUARD_PUBLIC_URL is required")
        @Pattern(
                regexp = "^https?://.+$",
                message = "SIMPLEGUARD_PUBLIC_URL must be an absolute HTTP or HTTPS URL"
        )
        String publicUrl
) {
}
