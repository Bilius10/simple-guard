package simple.guard.api.shared.audit;

import java.util.Optional;
import java.util.function.Supplier;

public final class AuditContext {

  private static final ThreadLocal<String> CURRENT_AUDITOR = new ThreadLocal<>();

  private AuditContext() {}

  public static Optional<String> currentAuditor() {
    return Optional.ofNullable(CURRENT_AUDITOR.get());
  }

  public static <T> T runAs(String auditor, Supplier<T> action) {
    String previousAuditor = CURRENT_AUDITOR.get();
    CURRENT_AUDITOR.set(auditor);
    try {
      return action.get();
    } finally {
      restore(previousAuditor);
    }
  }

  public static void runAs(String auditor, Runnable action) {
    runAs(
        auditor,
        () -> {
          action.run();
          return null;
        });
  }

  private static void restore(String previousAuditor) {
    if (previousAuditor == null) {
      CURRENT_AUDITOR.remove();
      return;
    }
    CURRENT_AUDITOR.set(previousAuditor);
  }
}
