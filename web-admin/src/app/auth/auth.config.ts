import { InjectionToken } from '@angular/core';

export interface SimpleGuardAuthConfig {
  readonly issuer: string;
  readonly clientId: string;
  readonly scope: string;
  readonly apiBaseUrl: string;
}

export const SIMPLEGUARD_AUTH_CONFIG = new InjectionToken<SimpleGuardAuthConfig>('SIMPLEGUARD_AUTH_CONFIG', {
  providedIn: 'root',
  factory: () => ({
    issuer: 'https://idp.localhost/realms/simpleguard',
    clientId: 'web-admin',
    scope: 'openid profile email',
    apiBaseUrl: '/api',
  }),
});
