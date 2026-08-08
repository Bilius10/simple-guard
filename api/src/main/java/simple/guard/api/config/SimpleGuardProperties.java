package simple.guard.api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "simpleguard")
public record SimpleGuardProperties(
        @NotBlank(message = "{simple_guard_instance_id_required}")
        String instanceId,
        @NotBlank(message = "{simple_guard_public_url_required}")
        @Pattern(
                regexp = "^https?://.+$",
                message = "{simple_guard_public_url_absolute_required}"
        )
        String publicUrl
) {
}
