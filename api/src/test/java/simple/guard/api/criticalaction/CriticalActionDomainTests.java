package simple.guard.api.criticalaction;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import simple.guard.api.criticalaction.domain.CriticalActionConfirmation;
import simple.guard.api.criticalaction.domain.CriticalActionConfirmationDecision;
import simple.guard.api.criticalaction.domain.CriticalActionStepUpRequirement;
import simple.guard.api.criticalaction.domain.CriticalActionType;

class CriticalActionDomainTests {

  @Test
  void exposesSupportedCriticalActionTypesTests() {
    assertThat(CriticalActionType.values())
        .containsExactly(
            CriticalActionType.LOCK_DEVICE,
            CriticalActionType.TRIGGER_ALARM,
            CriticalActionType.UNPAIR_DEVICE,
            CriticalActionType.CLOSE_INCIDENT);
    assertThat(CriticalActionType.valueOf("LOCK_DEVICE")).isEqualTo(CriticalActionType.LOCK_DEVICE);
  }

  @Test
  void exposesSupportedStepUpRequirementsTests() {
    assertThat(CriticalActionStepUpRequirement.values())
        .containsExactly(
            CriticalActionStepUpRequirement.NOT_REQUIRED, CriticalActionStepUpRequirement.REQUIRED);
    assertThat(CriticalActionStepUpRequirement.valueOf("REQUIRED"))
        .isEqualTo(CriticalActionStepUpRequirement.REQUIRED);
  }

  @Test
  void carriesCriticalActionConfirmationDataTests() {
    CriticalActionConfirmation confirmation =
        new CriticalActionConfirmation(
            CriticalActionType.UNPAIR_DEVICE,
            "device-123",
            CriticalActionStepUpRequirement.REQUIRED);
    CriticalActionConfirmation sameConfirmation =
        new CriticalActionConfirmation(
            CriticalActionType.UNPAIR_DEVICE,
            "device-123",
            CriticalActionStepUpRequirement.REQUIRED);

    assertThat(confirmation.actionType()).isEqualTo(CriticalActionType.UNPAIR_DEVICE);
    assertThat(confirmation.targetId()).isEqualTo("device-123");
    assertThat(confirmation.stepUpRequirement())
        .isEqualTo(CriticalActionStepUpRequirement.REQUIRED);
    assertThat(confirmation).isEqualTo(sameConfirmation);
    assertThat(confirmation).hasSameHashCodeAs(sameConfirmation);
    assertThat(confirmation.toString()).contains("UNPAIR_DEVICE", "device-123", "REQUIRED");
  }

  @Test
  void carriesCriticalActionConfirmationDecisionDataTests() {
    CriticalActionConfirmationDecision decision =
        new CriticalActionConfirmationDecision(
            CriticalActionType.CLOSE_INCIDENT, "incident-456", true);
    CriticalActionConfirmationDecision sameDecision =
        new CriticalActionConfirmationDecision(
            CriticalActionType.CLOSE_INCIDENT, "incident-456", true);

    assertThat(decision.actionType()).isEqualTo(CriticalActionType.CLOSE_INCIDENT);
    assertThat(decision.targetId()).isEqualTo("incident-456");
    assertThat(decision.stepUpRequired()).isTrue();
    assertThat(decision).isEqualTo(sameDecision);
    assertThat(decision).hasSameHashCodeAs(sameDecision);
    assertThat(decision.toString()).contains("CLOSE_INCIDENT", "incident-456", "true");
  }
}
