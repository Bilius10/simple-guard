package simple.guard.api.shared.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import simple.guard.api.identity.domain.Account;

class SimpleGuardAuditorAwareTests {

  private final SimpleGuardAuditorAware auditorAware = new SimpleGuardAuditorAware();

  @AfterEach
  void clearContextTests() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void prioritizesExplicitAuditContextTests() {
    String auditor =
        AuditContext.runAs(
            "agent:android-001", () -> auditorAware.getCurrentAuditor().orElseThrow());

    assertThat(auditor).isEqualTo("agent:android-001");
  }

  @Test
  void resolvesAccountSubjectFromAuthenticatedDetailsTests() {
    TestingAuthenticationToken authentication = new TestingAuthenticationToken("jwt", "n/a");
    authentication.setAuthenticated(true);
    authentication.setDetails(accountTests());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    assertThat(auditorAware.getCurrentAuditor()).contains("administrator-subject");
  }

  @Test
  void fallsBackToAuthenticationNameTests() {
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken("service-user", "n/a");
    authentication.setAuthenticated(true);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    assertThat(auditorAware.getCurrentAuditor()).contains("service-user");
  }

  @Test
  void fallsBackToSystemWhenUnauthenticatedTests() {
    assertThat(auditorAware.getCurrentAuditor()).contains("simpleguard-system");
  }

  @Test
  void fallsBackToSystemWhenAuthenticationIsNotAuthenticatedTests() {
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken("service-user", "n/a");
    authentication.setAuthenticated(false);
    SecurityContextHolder.getContext().setAuthentication(authentication);

    assertThat(auditorAware.getCurrentAuditor()).contains("simpleguard-system");
  }

  private static Account accountTests() {
    OffsetDateTime now = OffsetDateTime.parse("2026-08-09T12:00:00Z");
    return new Account(
        UUID.fromString("00000000-0000-0000-0000-000000000101"),
        "administrator-subject",
        "admin@simpleguard.local",
        "SimpleGuard Admin",
        "ADMIN",
        true,
        "test",
        now,
        "test",
        now);
  }
}
