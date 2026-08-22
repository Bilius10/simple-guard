package simple.guard.api;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class ApiApplicationMainTests {

  @Test
  void delegatesStartupToSpringApplicationTests() {
    String[] args = {"--server.port=0"};

    try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
      ApiApplication.main(args);

      springApplication.verify(() -> SpringApplication.run(ApiApplication.class, args));
    }
  }
}
