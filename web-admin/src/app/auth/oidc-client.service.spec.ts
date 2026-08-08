import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import { OidcClientService } from './oidc-client.service';

const TOKEN_STORAGE_KEY = 'simpleguard.oidc.tokens';
const STATE_STORAGE_KEY = 'simpleguard.oidc.state';
const VERIFIER_STORAGE_KEY = 'simpleguard.oidc.pkce_verifier';

describe('OidcClientServiceTests', () => {
  beforeEach(() => {
    sessionStorage.clear();
    history.pushState({}, '', '/');
    TestBed.configureTestingModule({});
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('loginValidoViaCallbackOidcTests', async () => {
    sessionStorage.setItem(STATE_STORAGE_KEY, 'expected-state');
    sessionStorage.setItem(VERIFIER_STORAGE_KEY, 'expected-verifier');
    history.pushState({}, '', '/auth/callback?code=valid-code&state=expected-state');

    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValueOnce(jsonResponseTests({
        authorization_endpoint: 'https://idp.localhost/auth',
        token_endpoint: 'https://idp.localhost/token',
        end_session_endpoint: 'https://idp.localhost/logout',
      }))
      .mockResolvedValueOnce(jsonResponseTests({
        access_token: 'valid-access-token',
        expires_in: 300,
        id_token: 'valid-id-token',
        token_type: 'Bearer',
        scope: 'openid profile email',
      })));

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state().status).toBe('authenticated');
    expect(service.accessToken()).toBe('valid-access-token');
    expect(sessionStorage.getItem(STATE_STORAGE_KEY)).toBeNull();
    expect(sessionStorage.getItem(VERIFIER_STORAGE_KEY)).toBeNull();
  });

  it('erroDeCredencialRetornadoPeloIdpTests', async () => {
    history.pushState({}, '', '/auth/callback?error=access_denied&error_description=Credencial%20invalida');

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state()).toEqual({
      status: 'auth_error',
      message: 'Credencial invalida',
    });
  });

  it('sessaoExpiradaArmazenadaTests', async () => {
    sessionStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify({
      accessToken: 'expired-access-token',
      tokenType: 'Bearer',
      expiresAt: Date.now() - 1000,
    }));

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state().status).toBe('session_expired');
    expect(service.accessToken()).toBeNull();
  });

  it('falhaAoIniciarLoginExibeErroTests', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('Keycloak indisponivel')));

    const service = TestBed.inject(OidcClientService);
    await service.login();

    expect(service.state()).toEqual({
      status: 'auth_error',
      message: 'Keycloak indisponivel',
    });
    expect(sessionStorage.getItem(STATE_STORAGE_KEY)).toBeNull();
    expect(sessionStorage.getItem(VERIFIER_STORAGE_KEY)).toBeNull();
  });
});

function jsonResponseTests(body: unknown): Response {
  return new Response(JSON.stringify(body), {
    status: 200,
    headers: {
      'Content-Type': 'application/json',
    },
  });
}
