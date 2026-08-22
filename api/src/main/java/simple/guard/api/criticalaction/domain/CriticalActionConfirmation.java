package simple.guard.api.criticalaction.domain;

public record CriticalActionConfirmation(
    CriticalActionType actionType,
    String targetId,
    CriticalActionStepUpRequirement stepUpRequirement) {}
