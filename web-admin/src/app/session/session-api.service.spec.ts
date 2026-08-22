import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SIMPLEGUARD_AUTH_CONFIG } from '../auth/auth.config';
import type { AdministratorSession } from './session-api.service';
import { SessionApiService } from './session-api.service';

describe('SessionApiServiceTests', () => {
  let http: HttpTestingController;
  let service: SessionApiService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        {
          provide: SIMPLEGUARD_AUTH_CONFIG,
          useValue: {
            issuer: 'https://idp.test',
            clientId: 'web-admin',
            scope: 'openid',
            apiBaseUrl: '/api',
          },
        },
      ],
    });
    http = TestBed.inject(HttpTestingController);
    service = TestBed.inject(SessionApiService);
  });

  afterEach(() => {
    http.verify();
  });

  it('loadsCurrentAdministratorSessionTests', () => {
    const session: AdministratorSession = {
      subject: 'subject-001',
      email: 'admin@simpleguard.local',
      displayName: 'SimpleGuard Admin',
      role: 'ADMIN',
    };

    service.me().subscribe((result) => {
      expect(result).toEqual(session);
    });

    const request = http.expectOne('/api/session/me');
    expect(request.request.method).toBe('GET');
    request.flush(session);
  });
});
