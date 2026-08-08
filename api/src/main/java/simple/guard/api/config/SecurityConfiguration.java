package simple.guard.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import simple.guard.api.error.config.ApiSecurityErrorHandler;
import simple.guard.api.identity.config.AccountJwtAuthenticationConverter;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AccountJwtAuthenticationConverter accountJwtAuthenticationConverter,
            ApiSecurityErrorHandler apiSecurityErrorHandler
    ) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(
                        "/actuator/health",
                        "/actuator/health/**",
                        "/livez",
                        "/readyz"
                ).permitAll()
                .anyRequest()
                .authenticated()
        );
        http.oauth2ResourceServer(oauth2 -> oauth2
                .authenticationEntryPoint(apiSecurityErrorHandler)
                .accessDeniedHandler(apiSecurityErrorHandler)
                .jwt(jwt -> jwt.jwtAuthenticationConverter(accountJwtAuthenticationConverter))
        );
        http.exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(apiSecurityErrorHandler)
                .accessDeniedHandler(apiSecurityErrorHandler)
        );

        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(SimpleGuardOidcProperties oidcProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(oidcProperties.jwkSetUri()).build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(oidcProperties.issuerUri()));
        return jwtDecoder;
    }
}
