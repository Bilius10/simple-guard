import {
  ChangeDetectionStrategy,
  Component,
  input,
  output,
} from '@angular/core';

import type { CriticalActionConfirmationRequest } from './critical-action.models';

@Component({
  selector: 'sg-critical-action-dialog',
  templateUrl: './critical-action-dialog.component.html',
  styleUrl: './critical-action-dialog.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CriticalActionDialogComponent {
  readonly action = input.required<CriticalActionConfirmationRequest>();
  readonly errorMessage = input<string | null>(null);
  readonly busy = input(false);

  readonly cancelAction = output<void>();
  readonly confirmAction = output<CriticalActionConfirmationRequest>();

  cancel(): void {
    this.cancelAction.emit();
  }

  confirm(): void {
    this.confirmAction.emit(this.action());
  }

  title(): string {
    return this.action().actionType === 'UNPAIR_DEVICE'
      ? 'Desparear dispositivo'
      : 'Confirmar comando';
  }

  confirmLabel(): string {
    if (this.busy()) {
      return 'Processando...';
    }
    return this.action().actionType === 'UNPAIR_DEVICE'
      ? 'Desparear dispositivo'
      : 'Confirmar comando';
  }
}
