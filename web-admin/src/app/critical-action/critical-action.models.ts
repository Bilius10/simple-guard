export type CriticalActionType = 'LOCK_DEVICE' | 'TRIGGER_ALARM' | 'UNPAIR_DEVICE' | 'CLOSE_INCIDENT';

export type CriticalActionStepUpRequirement = 'not_required' | 'required';

export interface CriticalActionConfirmationRequest {
  readonly actionType: CriticalActionType;
  readonly targetId: string;
  readonly targetName: string;
  readonly consequence: string;
  readonly connectivityState: string;
  readonly lastKnownLocation: string;
  readonly lastUpdatedAt: string;
  readonly stepUpRequirement: CriticalActionStepUpRequirement;
}

export interface CriticalActionConfirmationEvent {
  readonly actionType: CriticalActionType;
  readonly targetId: string;
  readonly stepUpRequired: boolean;
}
