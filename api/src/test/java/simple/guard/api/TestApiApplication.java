package simple.guard.api;

import org.springframework.boot.SpringApplication;

public final class TestApiApplication {

  private TestApiApplication() {}

  public static void main(String[] args) {
    SpringApplication.from(ApiApplication::main).with(TestcontainersConfiguration.class).run(args);
  }
}
