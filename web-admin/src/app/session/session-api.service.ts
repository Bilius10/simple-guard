import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { SIMPLEGUARD_AUTH_CONFIG } from '../auth/auth.config';

export interface AdministratorSession {
  readonly subject: string;
  readonly email: string;
  readonly displayName: string;
  readonly role: string;
}

@Injectable({ providedIn: 'root' })
export class SessionApiService {
  private readonly http = inject(HttpClient);
  private readonly config = inject(SIMPLEGUARD_AUTH_CONFIG);

  me() {
    return this.http.get<AdministratorSession>(
      `${this.config.apiBaseUrl}/session/me`,
    );
  }
}
