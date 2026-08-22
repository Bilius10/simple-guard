package simple.guard.api.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import simple.guard.api.config.properties.SimpleGuardProperties;

class SimpleGuardPropertiesTests {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner()
          .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
          .withUserConfiguration(PropertiesTestConfiguration.class);

  @Test
  void bindsRequiredConfigurationTests() {
    contextRunner
        .withPropertyValues(
            "simpleguard.instance-id=local-test",
            "simpleguard.public-url=https://simpleguard.localhost")
        .run(
            context -> {
              assertThat(context).hasNotFailed();
              assertThat(context.getBean(SimpleGuardProperties.class))
                  .isEqualTo(
                      new SimpleGuardProperties("local-test", "https://simpleguard.localhost"));
            });
  }

  @Test
  void failsClearlyWhenInstanceIdIsMissingTests() {
    contextRunner
        .withPropertyValues("simpleguard.public-url=https://simpleguard.localhost")
        .run(
            context -> {
              assertThat(context).hasFailed();
              assertThat(context.getStartupFailure())
                  .hasStackTraceContaining("SIMPLEGUARD_INSTANCE_ID");
            });
  }

  @Test
  void failsClearlyWhenPublicUrlIsMissingTests() {
    contextRunner
        .withPropertyValues("simpleguard.instance-id=local-test")
        .run(
            context -> {
              assertThat(context).hasFailed();
              assertThat(context.getStartupFailure())
                  .hasStackTraceContaining("SIMPLEGUARD_PUBLIC_URL");
            });
  }

  @Test
  void failsClearlyWhenPublicUrlIsNotAbsoluteTests() {
    contextRunner
        .withPropertyValues(
            "simpleguard.instance-id=local-test", "simpleguard.public-url=localhost")
        .run(
            context -> {
              assertThat(context).hasFailed();
              assertThat(context.getStartupFailure())
                  .hasStackTraceContaining("SIMPLEGUARD_PUBLIC_URL");
            });
  }

  @Configuration(proxyBeanMethods = false)
  @EnableConfigurationProperties(SimpleGuardProperties.class)
  static class PropertiesTestConfiguration {}
}
