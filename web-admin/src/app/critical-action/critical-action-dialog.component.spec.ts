import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { CriticalActionConfirmationRequest } from './critical-action.models';
import { CriticalActionDialogComponent } from './critical-action-dialog.component';

describe('CriticalActionDialogComponentTests', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CriticalActionDialogComponent],
    }).compileComponents();
  });

  it('rendersUnpairingLabelsAndEmitsActionsTests', () => {
    const fixture = TestBed.createComponent(CriticalActionDialogComponent);
    const action = actionTests('UNPAIR_DEVICE');
    const confirmed = vi.fn();
    const cancelled = vi.fn();
    fixture.componentRef.setInput('action', action);
    fixture.componentInstance.confirmAction.subscribe(confirmed);
    fixture.componentInstance.cancelAction.subscribe(cancelled);
    fixture.detectChanges();

    expect(fixture.componentInstance.title()).toBe('Desparear dispositivo');
    expect(fixture.componentInstance.confirmLabel()).toBe('Desparear dispositivo');
    expect((fixture.nativeElement as HTMLElement).textContent).not.toContain('Ultima localizacao');

    fixture.componentInstance.confirm();
    fixture.componentInstance.cancel();
    expect(confirmed).toHaveBeenCalledWith(action);
    expect(cancelled).toHaveBeenCalledOnce();
  });

  it('rendersGenericCommandAndBusyStateTests', () => {
    const fixture = TestBed.createComponent(CriticalActionDialogComponent);
    fixture.componentRef.setInput('action', actionTests('TRIGGER_ALARM'));
    fixture.componentRef.setInput('busy', true);
    fixture.detectChanges();

    expect(fixture.componentInstance.title()).toBe('Confirmar comando');
    expect(fixture.componentInstance.confirmLabel()).toBe('Processando...');
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Ultima localizacao');
    expect((fixture.nativeElement as HTMLElement).querySelector<HTMLButtonElement>('.danger-action')?.disabled)
      .toBe(true);
  });

  function actionTests(actionType: CriticalActionConfirmationRequest['actionType']): CriticalActionConfirmationRequest {
    return {
      actionType,
      targetId: 'device-001',
      targetName: 'Celular operacional',
      consequence: 'Consequencia operacional',
      connectivityState: 'paired',
      lastKnownLocation: 'indisponivel',
      lastUpdatedAt: 'indisponivel',
      stepUpRequirement: 'not_required',
    };
  }
});
