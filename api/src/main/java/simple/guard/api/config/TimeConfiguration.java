package simple.guard.api.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class TimeConfiguration {

  @Bean
  Clock systemClock() {
    return Clock.systemUTC();
  }
}
