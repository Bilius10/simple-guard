package simple.guard.api.config.properties;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "simpleguard.pairing")
public record SimpleGuardPairingProperties(
        @NotNull
        @DurationMin(seconds = 1, message = "{simple_guard_pairing_session_validity_minimum}")
        @DefaultValue("PT5M")
        Duration sessionValidity
) {
}
