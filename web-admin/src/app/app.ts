import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { OidcClientService } from './auth/oidc-client.service';
import { CriticalActionDialogComponent } from './critical-action/critical-action-dialog.component';
import {
  CriticalActionConfirmationEvent,
  CriticalActionConfirmationRequest,
} from './critical-action/critical-action.models';
import { AdministratorSession, SessionApiService } from './session/session-api.service';

@Component({
  selector: 'sg-root',
  imports: [CriticalActionDialogComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App implements OnInit {

  readonly auth = inject(OidcClientService);
  readonly session = signal<AdministratorSession | null>(null);
  readonly sessionError = signal<string | null>(null);
  readonly criticalAction = signal<CriticalActionConfirmationRequest | null>(null);
  readonly criticalActionError = signal<string | null>(null);
  readonly criticalActionEvent = signal<CriticalActionConfirmationEvent | null>(null);

  private readonly sessionApi = inject(SessionApiService);
  private criticalActionFailureSimulation = false;

  async ngOnInit(): Promise<void> {
    await this.auth.initialize();
    if (this.auth.state().status === 'authenticated') {
      await this.loadSession();
    }
  }

  async login(): Promise<void> {
    await this.auth.login();
  }

  async logout(): Promise<void> {
    await this.auth.logout();
  }

  openCriticalActionSimulation(): void {
    this.criticalActionFailureSimulation = false;
    this.criticalActionError.set(null);
    this.criticalAction.set(this.buildCriticalActionRequest());
  }

  openCriticalActionFailureSimulation(): void {
    this.criticalActionFailureSimulation = true;
    this.criticalActionError.set(null);
    this.criticalAction.set(this.buildCriticalActionRequest());
  }

  cancelCriticalAction(): void {
    this.criticalAction.set(null);
    this.criticalActionError.set(null);
  }

  confirmCriticalAction(action: CriticalActionConfirmationRequest): void {
    if (this.criticalActionFailureSimulation) {
      this.criticalActionError.set('Falha ao emitir evento de confirmacao critica.');
      return;
    }

    this.criticalActionEvent.set({
      actionType: action.actionType,
      targetId: action.targetId,
      stepUpRequired: action.stepUpRequirement === 'required',
    });
    this.criticalAction.set(null);
    this.criticalActionError.set(null);
  }

  private async loadSession(): Promise<void> {
    try {
      this.session.set(await firstValueFrom(this.sessionApi.me()));
      this.sessionError.set(null);
    } catch {
      this.session.set(null);
      this.sessionError.set('Nao foi possivel validar a sessao na API.');
    }
  }

  private buildCriticalActionRequest(): CriticalActionConfirmationRequest {
    return {
      actionType: 'TRIGGER_ALARM',
      targetId: 'device-demo-001',
      targetName: 'Notebook operacional demo',
      consequence: 'O comando podera acionar um alarme no dispositivo alvo quando comandos reais forem implementados.',
      connectivityState: 'online',
      lastKnownLocation: 'indisponivel',
      lastUpdatedAt: 'indisponivel',
      stepUpRequirement: 'required',
    };
  }
}
