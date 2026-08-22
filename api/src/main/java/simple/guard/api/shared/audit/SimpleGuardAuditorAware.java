package simple.guard.api.shared.audit;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import simple.guard.api.identity.domain.Account;

@Component
public class SimpleGuardAuditorAware implements AuditorAware<String> {

  private static final String SYSTEM_AUDITOR = "simpleguard-system";

  @Override
  public Optional<String> getCurrentAuditor() {
    return Optional.of(AuditContext.currentAuditor().orElseGet(this::authenticatedAuditor));
  }

  private String authenticatedAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      return SYSTEM_AUDITOR;
    }

    if (authentication.getDetails() instanceof Account account) {
      return account.getSubject();
    }

    return authentication.getName();
  }
}
