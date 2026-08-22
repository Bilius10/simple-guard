import { DOCUMENT } from '@angular/common';
import { inject, Injectable, signal } from '@angular/core';
import { sha256 } from 'js-sha256';

import { SIMPLEGUARD_AUTH_CONFIG } from './auth.config';
import {
  AuthState,
  OidcDiscoveryDocument,
  StoredTokenSet,
  TokenResponse,
} from './auth.models';

const TOKEN_STORAGE_KEY = 'simpleguard.oidc.tokens';
const STATE_STORAGE_KEY = 'simpleguard.oidc.state';
const VERIFIER_STORAGE_KEY = 'simpleguard.oidc.pkce_verifier';
const EXPIRATION_SKEW_MS = 30_000;

@Injectable({ providedIn: 'root' })
export class OidcClientService {
  private readonly config = inject(SIMPLEGUARD_AUTH_CONFIG);
  private readonly document = inject(DOCUMENT);
  private discoveryDocument?: OidcDiscoveryDocument;

  readonly state = signal<AuthState>({ status: 'loading' });

  async initialize(): Promise<void> {
    if (this.currentLocation().pathname === '/auth/callback') {
      await this.handleCallback();
      return;
    }

    const tokens = this.readStoredTokens();
    if (!tokens) {
      this.state.set({ status: 'login_required' });
      return;
    }

    if (this.isExpired(tokens)) {
      this.clearSession();
      this.state.set({
        status: 'session_expired',
        message: 'Sessao expirada. Faca login novamente.',
      });
      return;
    }

    this.state.set({ status: 'authenticated' });
  }

  async login(): Promise<void> {
    try {
      const discovery = await this.discover();
      const state = this.randomToken();
      const verifier = this.randomToken();
      const challenge = await this.createCodeChallenge(verifier);

      sessionStorage.setItem(STATE_STORAGE_KEY, state);
      sessionStorage.setItem(VERIFIER_STORAGE_KEY, verifier);

      const authorizationUrl = new URL(discovery.authorization_endpoint);
      authorizationUrl.searchParams.set('client_id', this.config.clientId);
      authorizationUrl.searchParams.set('redirect_uri', this.redirectUri());
      authorizationUrl.searchParams.set('response_type', 'code');
      authorizationUrl.searchParams.set('scope', this.config.scope);
      authorizationUrl.searchParams.set('state', state);
      authorizationUrl.searchParams.set('code_challenge', challenge);
      authorizationUrl.searchParams.set('code_challenge_method', 'S256');

      this.document.location.assign(authorizationUrl.toString());
    } catch (error) {
      this.clearTransientLoginState();
      this.state.set({
        status: 'auth_error',
        message:
          error instanceof Error ? error.message : 'Falha ao iniciar login.',
      });
    }
  }

  async logout(): Promise<void> {
    const tokens = this.readStoredTokens();
    this.clearSession();

    const discovery = await this.discover();
    if (!discovery.end_session_endpoint) {
      this.state.set({ status: 'login_required' });
      return;
    }

    const logoutUrl = new URL(discovery.end_session_endpoint);
    logoutUrl.searchParams.set('client_id', this.config.clientId);
    logoutUrl.searchParams.set(
      'post_logout_redirect_uri',
      this.postLogoutRedirectUri(),
    );
    if (tokens?.idToken) {
      logoutUrl.searchParams.set('id_token_hint', tokens.idToken);
    }

    this.document.location.assign(logoutUrl.toString());
  }

  accessToken(): string | null {
    const tokens = this.readStoredTokens();
    if (!tokens || this.isExpired(tokens)) {
      return null;
    }

    return tokens.accessToken;
  }

  private async handleCallback(): Promise<void> {
    const location = this.currentLocation();
    const parameters = new URLSearchParams(location.search);
    const expectedState = sessionStorage.getItem(STATE_STORAGE_KEY);
    const verifier = sessionStorage.getItem(VERIFIER_STORAGE_KEY);

    const error = parameters.get('error');
    if (error) {
      this.clearTransientLoginState();
      this.state.set({
        status: 'auth_error',
        message: parameters.get('error_description') ?? error,
      });
      return;
    }

    const code = parameters.get('code');
    const returnedState = parameters.get('state');
    if (
      !code ||
      !expectedState ||
      !verifier ||
      returnedState !== expectedState
    ) {
      this.clearTransientLoginState();
      this.state.set({
        status: 'auth_error',
        message: 'Retorno OIDC invalido.',
      });
      return;
    }

    try {
      const discovery = await this.discover();
      const body = new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: this.config.clientId,
        code,
        redirect_uri: this.redirectUri(),
        code_verifier: verifier,
      });

      const response = await fetch(discovery.token_endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body,
      });

      if (!response.ok) {
        throw new Error('Falha ao trocar codigo OIDC por token.');
      }

      const tokenResponse = (await response.json()) as TokenResponse;
      this.storeTokens(tokenResponse);
      this.clearTransientLoginState();
      history.replaceState({}, this.document.title, '/');
      this.state.set({ status: 'authenticated' });
    } catch (error) {
      this.clearSession();
      this.state.set({
        status: 'auth_error',
        message:
          error instanceof Error ? error.message : 'Falha de autenticacao.',
      });
    }
  }

  private async discover(): Promise<OidcDiscoveryDocument> {
    if (this.discoveryDocument) {
      return this.discoveryDocument;
    }

    const response = await fetch(
      `${this.config.issuer}/.well-known/openid-configuration`,
    );
    if (!response.ok) {
      throw new Error('Nao foi possivel carregar a configuracao OIDC.');
    }

    this.discoveryDocument = (await response.json()) as OidcDiscoveryDocument;
    return this.discoveryDocument;
  }

  private storeTokens(tokenResponse: TokenResponse): void {
    const tokens: StoredTokenSet = {
      accessToken: tokenResponse.access_token,
      idToken: tokenResponse.id_token,
      refreshToken: tokenResponse.refresh_token,
      tokenType: tokenResponse.token_type,
      scope: tokenResponse.scope,
      expiresAt:
        Date.now() + tokenResponse.expires_in * 1000 - EXPIRATION_SKEW_MS,
    };

    sessionStorage.setItem(TOKEN_STORAGE_KEY, JSON.stringify(tokens));
  }

  private readStoredTokens(): StoredTokenSet | null {
    const rawValue = sessionStorage.getItem(TOKEN_STORAGE_KEY);
    if (!rawValue) {
      return null;
    }

    try {
      return JSON.parse(rawValue) as StoredTokenSet;
    } catch {
      this.clearSession();
      return null;
    }
  }

  private isExpired(tokens: StoredTokenSet): boolean {
    return Date.now() >= tokens.expiresAt;
  }

  private clearSession(): void {
    sessionStorage.removeItem(TOKEN_STORAGE_KEY);
    this.clearTransientLoginState();
  }

  private clearTransientLoginState(): void {
    sessionStorage.removeItem(STATE_STORAGE_KEY);
    sessionStorage.removeItem(VERIFIER_STORAGE_KEY);
  }

  private redirectUri(): string {
    return new URL('/auth/callback', this.currentLocation().origin).toString();
  }

  private postLogoutRedirectUri(): string {
    return new URL('/', this.currentLocation().origin).toString();
  }

  private currentLocation(): Location {
    return this.document.location;
  }

  private randomToken(): string {
    const bytes = new Uint8Array(32);
    crypto.getRandomValues(bytes);
    return this.toBase64Url(bytes);
  }

  private async createCodeChallenge(verifier: string): Promise<string> {
    const encodedVerifier = new TextEncoder().encode(verifier);
    return this.toBase64Url(await this.sha256(encodedVerifier));
  }

  private async sha256(bytes: Uint8Array): Promise<Uint8Array> {
    if (globalThis.crypto?.subtle?.digest) {
      const digestInput = bytes.buffer.slice(
        bytes.byteOffset,
        bytes.byteOffset + bytes.byteLength,
      ) as ArrayBuffer;
      return new Uint8Array(
        await globalThis.crypto.subtle.digest('SHA-256', digestInput),
      );
    }

    return new Uint8Array(sha256.array(bytes));
  }

  private toBase64Url(bytes: Uint8Array): string {
    let binary = '';
    bytes.forEach((byte) => {
      binary += String.fromCharCode(byte);
    });

    return btoa(binary)
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/u, '');
  }
}
