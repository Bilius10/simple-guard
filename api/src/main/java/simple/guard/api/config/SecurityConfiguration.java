package simple.guard.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(
                        "/actuator/health",
                        "/actuator/health/**",
                        "/livez",
                        "/readyz"
                )
                .permitAll()
                .anyRequest()
                .denyAll()
        );

        return http.build();
    }
}
