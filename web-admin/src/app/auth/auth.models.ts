export type AuthStatus =
  | 'loading'
  | 'login_required'
  | 'authenticated'
  | 'auth_error'
  | 'session_expired';

export interface AuthState {
  readonly status: AuthStatus;
  readonly message?: string;
}

export interface OidcDiscoveryDocument {
  readonly authorization_endpoint: string;
  readonly token_endpoint: string;
  readonly end_session_endpoint?: string;
}

export interface TokenResponse {
  readonly access_token: string;
  readonly expires_in: number;
  readonly id_token?: string;
  readonly refresh_token?: string;
  readonly token_type: string;
  readonly scope?: string;
}

export interface StoredTokenSet {
  readonly accessToken: string;
  readonly idToken?: string;
  readonly refreshToken?: string;
  readonly tokenType: string;
  readonly scope?: string;
  readonly expiresAt: number;
}
