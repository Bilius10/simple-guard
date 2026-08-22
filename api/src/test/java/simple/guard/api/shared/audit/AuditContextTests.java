package simple.guard.api.shared.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditContextTests {

    @Test
    void exposesAuditorOnlyInsideScopeTests() {
        assertThat(AuditContext.currentAuditor()).isEmpty();

        String auditor = AuditContext.runAs("agent:android-001", () ->
                AuditContext.currentAuditor().orElseThrow()
        );

        assertThat(auditor).isEqualTo("agent:android-001");
        assertThat(AuditContext.currentAuditor()).isEmpty();
    }

    @Test
    void restoresPreviousAuditorAfterNestedScopeTests() {
        String auditor = AuditContext.runAs("outer", () -> {
            AuditContext.runAs("inner", () ->
                    assertThat(AuditContext.currentAuditor()).contains("inner")
            );
            return AuditContext.currentAuditor().orElseThrow();
        });

        assertThat(auditor).isEqualTo("outer");
        assertThat(AuditContext.currentAuditor()).isEmpty();
    }
}
