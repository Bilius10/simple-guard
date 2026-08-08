import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { App } from './app';
import { OidcClientService } from './auth/oidc-client.service';
import { SessionApiService } from './session/session-api.service';

describe('AppTests', () => {
  const authServiceStub = {
    state: signal({ status: 'login_required' as const }),
    initialize: vi.fn().mockResolvedValue(undefined),
    login: vi.fn().mockResolvedValue(undefined),
    logout: vi.fn().mockResolvedValue(undefined),
    accessToken: vi.fn().mockReturnValue(null),
  };

  const sessionApiStub = {
    me: vi.fn().mockReturnValue(of({
      subject: '00000000-0000-0000-0000-000000000001',
      email: 'admin@simpleguard.local',
      displayName: 'SimpleGuard Admin',
      role: 'ADMIN',
    })),
  };

  beforeEach(async () => {
    authServiceStub.state.set({ status: 'login_required' });
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: OidcClientService, useValue: authServiceStub },
        { provide: SessionApiService, useValue: sessionApiStub },
      ],
    }).compileComponents();
  });

  it('createsTheApplicationShellTests', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('rendersTheSimpleGuardOperationalIdentityTests', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.brand')?.textContent).toContain('SIMPLEGUARD');
    expect(element.querySelector('h1')?.textContent).toContain('Central operacional');
  });
});
