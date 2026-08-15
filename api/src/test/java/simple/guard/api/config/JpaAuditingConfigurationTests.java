package simple.guard.api.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class JpaAuditingConfigurationTests {

    @Test
    void providesOffsetDateTimeFromConfiguredClockTests() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-09T12:00:00Z"), ZoneOffset.UTC);
        var provider = new JpaAuditingConfiguration().auditingDateTimeProvider(clock);

        assertThat(provider.getNow()).contains(OffsetDateTime.parse("2026-08-09T12:00:00Z"));
    }
}


