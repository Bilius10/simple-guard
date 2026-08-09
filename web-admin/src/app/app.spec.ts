import { TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { vi } from 'vitest';

import { App } from './app';
import { AuthState } from './auth/auth.models';
import { OidcClientService } from './auth/oidc-client.service';
import { SessionApiService } from './session/session-api.service';

describe('AppTests', () => {
  const authServiceStub = {
    state: signal<AuthState>({ status: 'login_required' }),
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

  it('opensCriticalActionDialogTests', async () => {
    const fixture = await createAuthenticatedFixtureTests();

    clickTests(fixture.nativeElement, '.danger-outline-action');
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[role="dialog"]')).not.toBeNull();
    expect(element.textContent).toContain('Confirmar comando');
    expect(element.textContent).toContain('Notebook operacional demo');
  });

  it('cancelsCriticalActionWithoutEmittingEventTests', async () => {
    const fixture = await createAuthenticatedFixtureTests();

    clickTests(fixture.nativeElement, '.danger-outline-action');
    fixture.detectChanges();
    clickTests(fixture.nativeElement, '.critical-dialog .secondary-action');
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[role="dialog"]')).toBeNull();
    expect(element.querySelector('.confirmation-event')).toBeNull();
    expect(fixture.componentInstance.criticalActionEvent()).toBeNull();
  });

  it('confirmsCriticalActionAndEmitsEventTests', async () => {
    const fixture = await createAuthenticatedFixtureTests();

    clickTests(fixture.nativeElement, '.danger-outline-action');
    fixture.detectChanges();
    clickTests(fixture.nativeElement, '.critical-dialog .danger-action');
    fixture.detectChanges();

    const event = fixture.componentInstance.criticalActionEvent();
    const element = fixture.nativeElement as HTMLElement;
    expect(event).toEqual({
      actionType: 'TRIGGER_ALARM',
      targetId: 'device-demo-001',
      stepUpRequired: true,
    });
    expect(element.querySelector('[role="dialog"]')).toBeNull();
    expect(element.querySelector('.confirmation-event')?.textContent).toContain('command-confirmed');
  });

  it('showsCriticalActionConfirmationErrorTests', async () => {
    const fixture = await createAuthenticatedFixtureTests();

    clickTests(fixture.nativeElement, '.authenticated-actions .secondary-action');
    fixture.detectChanges();
    clickTests(fixture.nativeElement, '.critical-dialog .danger-action');
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[role="dialog"]')).not.toBeNull();
    expect(element.querySelector('[role="alert"]')?.textContent).toContain('Falha ao emitir evento');
    expect(fixture.componentInstance.criticalActionEvent()).toBeNull();
  });

  async function createAuthenticatedFixtureTests() {
    authServiceStub.state.set({ status: 'authenticated' });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
    return fixture;
  }

  function clickTests(root: Element, selector: string): void {
    const button = root.querySelector<HTMLButtonElement>(selector);
    expect(button).not.toBeNull();
    button?.click();
  }
});
