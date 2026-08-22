package simple.guard.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import simple.guard.api.config.properties.SimpleGuardPairingProperties;

class SimpleGuardPairingPropertiesTests {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
          .withUserConfiguration(PairingPropertiesTestConfigurationTests.class);

  @Test
  void usesDefaultSessionValidityTests() {
    contextRunner.run(
        context -> {
          assertThat(context).hasNotFailed();
          assertThat(context.getBean(SimpleGuardPairingProperties.class).sessionValidity())
              .isEqualTo(Duration.ofMinutes(5));
        });
  }

  @Test
  void bindsConfiguredSessionValidityTests() {
    contextRunner
        .withPropertyValues("simpleguard.pairing.session-validity=PT2M")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context.getBean(SimpleGuardPairingProperties.class).sessionValidity())
                  .isEqualTo(Duration.ofMinutes(2));
            });
  }

  @Test
  void rejectsNonPositiveSessionValidityTests() {
    contextRunner
        .withPropertyValues("simpleguard.pairing.session-validity=PT0S")
        .run(context -> assertThat(context).hasFailed());
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(SimpleGuardPairingProperties.class)
  static class PairingPropertiesTestConfigurationTests {}
}
