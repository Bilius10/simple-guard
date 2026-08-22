import { HttpRequest } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { OidcClientService } from './oidc-client.service';
import { authInterceptor } from './auth.interceptor';

describe('AuthInterceptorTests', () => {
  const oidcStub = {
    accessToken: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    TestBed.configureTestingModule({
      providers: [{ provide: OidcClientService, useValue: oidcStub }],
    });
  });

  it('addsBearerTokenForApiRequestsTests', () => {
    oidcStub.accessToken.mockReturnValue('access-token-001');
    const request = new HttpRequest('GET', '/api/devices');
    const next = vi.fn();

    TestBed.runInInjectionContext(() => authInterceptor(request, next));

    expect(next).toHaveBeenCalledOnce();
    const forwarded = next.mock.calls[0][0] as HttpRequest<unknown>;
    expect(forwarded.headers.get('Authorization')).toBe(
      'Bearer access-token-001',
    );
  });

  it('keepsRequestWhenTokenDoesNotExistTests', () => {
    oidcStub.accessToken.mockReturnValue(null);
    const request = new HttpRequest('GET', '/api/devices');
    const next = vi.fn();

    TestBed.runInInjectionContext(() => authInterceptor(request, next));

    expect(next).toHaveBeenCalledWith(request);
  });

  it('keepsExternalRequestsWithoutAuthorizationTests', () => {
    oidcStub.accessToken.mockReturnValue('access-token-001');
    const request = new HttpRequest(
      'GET',
      'https://cdn.simpleguard.local/assets',
    );
    const next = vi.fn();

    TestBed.runInInjectionContext(() => authInterceptor(request, next));

    expect(next).toHaveBeenCalledWith(request);
  });
});
