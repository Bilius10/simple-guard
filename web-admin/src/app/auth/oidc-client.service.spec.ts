import { DOCUMENT } from '@angular/common';
import { TestBed } from '@angular/core/testing';
import { sha256 } from 'js-sha256';
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
    history.pushState(
      {},
      '',
      '/auth/callback?code=valid-code&state=expected-state',
    );

    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValueOnce(
          jsonResponseTests({
            authorization_endpoint: 'https://idp.localhost/auth',
            token_endpoint: 'https://idp.localhost/token',
            end_session_endpoint: 'https://idp.localhost/logout',
          }),
        )
        .mockResolvedValueOnce(
          jsonResponseTests({
            access_token: 'valid-access-token',
            expires_in: 300,
            id_token: 'valid-id-token',
            token_type: 'Bearer',
            scope: 'openid profile email',
          }),
        ),
    );

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state().status).toBe('authenticated');
    expect(service.accessToken()).toBe('valid-access-token');
    expect(sessionStorage.getItem(STATE_STORAGE_KEY)).toBeNull();
    expect(sessionStorage.getItem(VERIFIER_STORAGE_KEY)).toBeNull();
  });

  it('erroDeCredencialRetornadoPeloIdpTests', async () => {
    history.pushState(
      {},
      '',
      '/auth/callback?error=access_denied&error_description=Credencial%20invalida',
    );

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state()).toEqual({
      status: 'auth_error',
      message: 'Credencial invalida',
    });
  });

  it('erroDeCredencialSemDescricaoUsaCodigoOidcTests', async () => {
    history.pushState({}, '', '/auth/callback?error=temporarily_unavailable');

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state()).toEqual({
      status: 'auth_error',
      message: 'temporarily_unavailable',
    });
  });

  it('retornoOidcInvalidoLimpaEstadoTransienteTests', async () => {
    sessionStorage.setItem(STATE_STORAGE_KEY, 'expected-state');
    sessionStorage.setItem(VERIFIER_STORAGE_KEY, 'expected-verifier');
    history.pushState(
      {},
      '',
      '/auth/callback?code=valid-code&state=wrong-state',
    );

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state()).toEqual({
      status: 'auth_error',
      message: 'Retorno OIDC invalido.',
    });
    expect(sessionStorage.getItem(STATE_STORAGE_KEY)).toBeNull();
    expect(sessionStorage.getItem(VERIFIER_STORAGE_KEY)).toBeNull();
  });

  it('falhaNaTrocaDoCodigoPorTokenExibeErroTests', async () => {
    sessionStorage.setItem(STATE_STORAGE_KEY, 'expected-state');
    sessionStorage.setItem(VERIFIER_STORAGE_KEY, 'expected-verifier');
    history.pushState(
      {},
      '',
      '/auth/callback?code=valid-code&state=expected-state',
    );

    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValueOnce(jsonResponseTests(discoveryDocumentTests()))
        .mockResolvedValueOnce(new Response('', { status: 401 })),
    );

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state()).toEqual({
      status: 'auth_error',
      message: 'Falha ao trocar codigo OIDC por token.',
    });
    expect(service.accessToken()).toBeNull();
  });

  it('sessaoExpiradaArmazenadaTests', async () => {
    sessionStorage.setItem(
      TOKEN_STORAGE_KEY,
      JSON.stringify({
        accessToken: 'expired-access-token',
        tokenType: 'Bearer',
        expiresAt: Date.now() - 1000,
      }),
    );

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state().status).toBe('session_expired');
    expect(service.accessToken()).toBeNull();
  });

  it('sessaoValidaArmazenadaAutenticaSemCallbackTests', async () => {
    sessionStorage.setItem(
      TOKEN_STORAGE_KEY,
      JSON.stringify({
        accessToken: 'stored-access-token',
        tokenType: 'Bearer',
        expiresAt: Date.now() + 300_000,
      }),
    );

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state().status).toBe('authenticated');
    expect(service.accessToken()).toBe('stored-access-token');
  });

  it('sessaoCorrompidaArmazenadaExigeLoginTests', async () => {
    sessionStorage.setItem(TOKEN_STORAGE_KEY, '{invalid-json');

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state().status).toBe('login_required');
    expect(sessionStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull();
  });

  it('falhaAoIniciarLoginExibeErroTests', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockRejectedValue(new Error('Keycloak indisponivel')),
    );

    const service = TestBed.inject(OidcClientService);
    await service.login();

    expect(service.state()).toEqual({
      status: 'auth_error',
      message: 'Keycloak indisponivel',
    });
    expect(sessionStorage.getItem(STATE_STORAGE_KEY)).toBeNull();
    expect(sessionStorage.getItem(VERIFIER_STORAGE_KEY)).toBeNull();
  });

  it('falhaAoDescobrirConfiguracaoOidcExibeErroDeDiscoveryTests', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValueOnce(new Response('', { status: 503 })),
    );

    const service = TestBed.inject(OidcClientService);
    await service.login();

    expect(service.state()).toEqual({
      status: 'auth_error',
      message: 'Nao foi possivel carregar a configuracao OIDC.',
    });
  });

  it('falhaNaoMapeadaAoIniciarLoginUsaMensagemPadraoTests', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue('offline'));

    const service = TestBed.inject(OidcClientService);
    await service.login();

    expect(service.state()).toEqual({
      status: 'auth_error',
      message: 'Falha ao iniciar login.',
    });
  });

  it('iniciaLoginComPkceERedirecionaParaAutorizacaoTests', async () => {
    const assign = vi.fn();
    TestBed.overrideProvider(DOCUMENT, {
      useValue: documentTests({
        assign,
        pathname: '/',
        search: '',
      }),
    });
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValueOnce(jsonResponseTests(discoveryDocumentTests())),
    );

    const service = TestBed.inject(OidcClientService);
    await service.login();

    expect(assign).toHaveBeenCalledOnce();
    const authorizationUrl = new URL(assign.mock.calls[0][0]);
    expect(authorizationUrl.origin).toBe('https://idp.localhost');
    expect(authorizationUrl.pathname).toBe('/auth');
    expect(authorizationUrl.searchParams.get('client_id')).toBe('web-admin');
    expect(authorizationUrl.searchParams.get('redirect_uri')).toBe(
      'https://app.localhost/auth/callback',
    );
    expect(authorizationUrl.searchParams.get('response_type')).toBe('code');
    expect(authorizationUrl.searchParams.get('scope')).toBe(
      'openid profile email',
    );
    expect(authorizationUrl.searchParams.get('state')).toBeTruthy();
    expect(authorizationUrl.searchParams.get('code_challenge')).toBeTruthy();
    expect(authorizationUrl.searchParams.get('code_challenge_method')).toBe(
      'S256',
    );
    expect(sessionStorage.getItem(STATE_STORAGE_KEY)).toBeTruthy();
    expect(sessionStorage.getItem(VERIFIER_STORAGE_KEY)).toBeTruthy();
  });

  it('reutilizaDiscoveryDocumentEmLoginsSequenciaisTests', async () => {
    const assign = vi.fn();
    TestBed.overrideProvider(DOCUMENT, {
      useValue: documentTests({
        assign,
        pathname: '/',
        search: '',
      }),
    });
    const fetch = vi
      .fn()
      .mockResolvedValueOnce(jsonResponseTests(discoveryDocumentTests()));
    vi.stubGlobal('fetch', fetch);

    const service = TestBed.inject(OidcClientService);
    await service.login();
    await service.login();

    expect(fetch).toHaveBeenCalledOnce();
    expect(assign).toHaveBeenCalledTimes(2);
  });

  it('iniciaLoginComPkceQuandoCryptoSubtleNaoExisteTests', async () => {
    const assign = vi.fn();
    TestBed.overrideProvider(DOCUMENT, {
      useValue: documentTests({
        assign,
        pathname: '/',
        search: '',
      }),
    });
    vi.stubGlobal('crypto', {
      getRandomValues: vi.fn((bytes: Uint8Array) => {
        bytes.fill(1);
        return bytes;
      }),
    });
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValueOnce(jsonResponseTests(discoveryDocumentTests())),
    );

    const service = TestBed.inject(OidcClientService);
    await service.login();

    const authorizationUrl = new URL(assign.mock.calls[0][0]);
    const verifier = sessionStorage.getItem(VERIFIER_STORAGE_KEY);
    expect(verifier).toBeTruthy();
    expect(authorizationUrl.searchParams.get('code_challenge')).toBe(
      base64UrlTests(new Uint8Array(sha256.array(verifier ?? ''))),
    );
    expect(authorizationUrl.searchParams.get('code_challenge_method')).toBe(
      'S256',
    );
  });

  it('logoutSemEndpointFinalizaSessaoLocalTests', async () => {
    sessionStorage.setItem(
      TOKEN_STORAGE_KEY,
      JSON.stringify({
        accessToken: 'stored-access-token',
        tokenType: 'Bearer',
        expiresAt: Date.now() + 300_000,
      }),
    );
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValueOnce(
        jsonResponseTests({
          authorization_endpoint: 'https://idp.localhost/auth',
          token_endpoint: 'https://idp.localhost/token',
        }),
      ),
    );

    const service = TestBed.inject(OidcClientService);
    await service.logout();

    expect(service.state().status).toBe('login_required');
    expect(sessionStorage.getItem(TOKEN_STORAGE_KEY)).toBeNull();
  });

  it('logoutComEndpointRedirecionaComIdTokenTests', async () => {
    const assign = vi.fn();
    TestBed.overrideProvider(DOCUMENT, {
      useValue: documentTests({
        assign,
        pathname: '/',
        search: '',
      }),
    });
    sessionStorage.setItem(
      TOKEN_STORAGE_KEY,
      JSON.stringify({
        accessToken: 'stored-access-token',
        idToken: 'stored-id-token',
        tokenType: 'Bearer',
        expiresAt: Date.now() + 300_000,
      }),
    );
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValueOnce(jsonResponseTests(discoveryDocumentTests())),
    );

    const service = TestBed.inject(OidcClientService);
    await service.logout();

    expect(assign).toHaveBeenCalledOnce();
    const logoutUrl = new URL(assign.mock.calls[0][0]);
    expect(logoutUrl.origin).toBe('https://idp.localhost');
    expect(logoutUrl.pathname).toBe('/logout');
    expect(logoutUrl.searchParams.get('client_id')).toBe('web-admin');
    expect(logoutUrl.searchParams.get('post_logout_redirect_uri')).toBe(
      'https://app.localhost/',
    );
    expect(logoutUrl.searchParams.get('id_token_hint')).toBe('stored-id-token');
  });

  it('logoutComEndpointSemIdTokenNaoEnviaHintTests', async () => {
    const assign = vi.fn();
    TestBed.overrideProvider(DOCUMENT, {
      useValue: documentTests({
        assign,
        pathname: '/',
        search: '',
      }),
    });
    sessionStorage.setItem(
      TOKEN_STORAGE_KEY,
      JSON.stringify({
        accessToken: 'stored-access-token',
        tokenType: 'Bearer',
        expiresAt: Date.now() + 300_000,
      }),
    );
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValueOnce(jsonResponseTests(discoveryDocumentTests())),
    );

    const service = TestBed.inject(OidcClientService);
    await service.logout();

    const logoutUrl = new URL(assign.mock.calls[0][0]);
    expect(logoutUrl.searchParams.has('id_token_hint')).toBe(false);
  });

  it('falhaNaoMapeadaNoCallbackUsaMensagemPadraoTests', async () => {
    sessionStorage.setItem(STATE_STORAGE_KEY, 'expected-state');
    sessionStorage.setItem(VERIFIER_STORAGE_KEY, 'expected-verifier');
    history.pushState(
      {},
      '',
      '/auth/callback?code=valid-code&state=expected-state',
    );

    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValueOnce(jsonResponseTests(discoveryDocumentTests()))
        .mockRejectedValueOnce('offline'),
    );

    const service = TestBed.inject(OidcClientService);
    await service.initialize();

    expect(service.state()).toEqual({
      status: 'auth_error',
      message: 'Falha de autenticacao.',
    });
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

function discoveryDocumentTests() {
  return {
    authorization_endpoint: 'https://idp.localhost/auth',
    token_endpoint: 'https://idp.localhost/token',
    end_session_endpoint: 'https://idp.localhost/logout',
  };
}

function documentTests(options: {
  readonly assign: (url: string) => void;
  readonly pathname: string;
  readonly search: string;
}) {
  return {
    title: 'SimpleGuard',
    location: {
      origin: 'https://app.localhost',
      pathname: options.pathname,
      search: options.search,
      assign: options.assign,
    },
  };
}

function base64UrlTests(bytes: Uint8Array): string {
  let binary = '';
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });

  return btoa(binary)
    .replace(/\+/g, '-')
    .replace(/\//g, '_')
    .replace(/=+$/u, '');
}
