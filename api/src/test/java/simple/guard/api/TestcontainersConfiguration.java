package simple.guard.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    DockerImageName image =
        DockerImageName.parse("postgis/postgis:18-3.6-alpine")
            .asCompatibleSubstituteFor("postgres");
    return new PostgreSQLContainer<>(image);
  }
}
