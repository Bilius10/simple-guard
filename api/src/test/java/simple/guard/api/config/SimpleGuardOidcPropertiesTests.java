package simple.guard.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import simple.guard.api.config.properties.SimpleGuardOidcProperties;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleGuardOidcPropertiesTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class))
            .withUserConfiguration(OidcPropertiesTestConfiguration.class);

    @Test
    void bindsRequiredOidcConfigurationTests() {
        contextRunner
                .withPropertyValues(
                        "simpleguard.oidc.issuer-uri=https://idp.localhost/realms/simpleguard",
                        "simpleguard.oidc.jwk-set-uri=http://keycloak:8080/realms/simpleguard/protocol/openid-connect/certs"
                )
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(SimpleGuardOidcProperties.class))
                            .isEqualTo(new SimpleGuardOidcProperties(
                                    "https://idp.localhost/realms/simpleguard",
                                    "http://keycloak:8080/realms/simpleguard/protocol/openid-connect/certs"
                            ));
                });
    }

    @Test
    void failsClearlyWhenIssuerUriIsMissingTests() {
        contextRunner
                .withPropertyValues("simpleguard.oidc.jwk-set-uri=http://keycloak:8080/certs")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("SIMPLEGUARD_OIDC_ISSUER_URI");
                });
    }

    @Test
    void failsClearlyWhenJwkSetUriIsMissingTests() {
        contextRunner
                .withPropertyValues("simpleguard.oidc.issuer-uri=https://idp.localhost/realms/simpleguard")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("SIMPLEGUARD_OIDC_JWK_SET_URI");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(SimpleGuardOidcProperties.class)
    static class OidcPropertiesTestConfiguration {
    }
}
