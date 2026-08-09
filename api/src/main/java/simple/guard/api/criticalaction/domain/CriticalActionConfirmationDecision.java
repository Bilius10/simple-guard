package simple.guard.api.criticalaction.domain;

public record CriticalActionConfirmationDecision(
        CriticalActionType actionType,
        String targetId,
        boolean stepUpRequired
) {}
