import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';

import { OidcClientService } from './auth/oidc-client.service';
import { AdministratorSession, SessionApiService } from './session/session-api.service';

@Component({
  selector: 'sg-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App implements OnInit {

  readonly auth = inject(OidcClientService);
  readonly session = signal<AdministratorSession | null>(null);
  readonly sessionError = signal<string | null>(null);

  private readonly sessionApi = inject(SessionApiService);

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

  private async loadSession(): Promise<void> {
    try {
      this.session.set(await firstValueFrom(this.sessionApi.me()));
      this.sessionError.set(null);
    } catch {
      this.session.set(null);
      this.sessionError.set('Nao foi possivel validar a sessao na API.');
    }
  }
}
