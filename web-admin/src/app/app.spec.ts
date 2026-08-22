import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { App } from './app';
import type { AuthState } from './auth/auth.models';
import { OidcClientService } from './auth/oidc-client.service';
import { DeviceApiService } from './devices/device-api.service';
import type { Device, LatestDeviceTelemetry } from './devices/device.models';
import { NotificationService } from './notifications/notification.service';
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
    me: vi.fn(),
  };

  const deviceApiStub = {
    list: vi.fn(),
    latestTelemetry: vi.fn(),
    latestTelemetryList: vi.fn(),
    create: vi.fn(),
    generatePairingSession: vi.fn(),
    unpair: vi.fn(),
    listUnpairingRequests: vi.fn(),
    decideUnpairingRequest: vi.fn(),
  };

  const notificationStub = {
    notifications: signal([]),
    success: vi.fn(),
    error: vi.fn(),
    dismiss: vi.fn(),
  };

  beforeEach(async () => {
    authServiceStub.state.set({ status: 'login_required' });
    sessionApiStub.me.mockReturnValue(of(sessionTests()));
    deviceApiStub.list.mockReturnValue(of([]));
    deviceApiStub.latestTelemetry.mockImplementation((deviceId: string) =>
      of(telemetryTests({ deviceId })),
    );
    deviceApiStub.latestTelemetryList.mockReturnValue(of([telemetryTests()]));
    deviceApiStub.listUnpairingRequests.mockReturnValue(of([]));
    vi.useRealTimers();
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: OidcClientService, useValue: authServiceStub },
        { provide: SessionApiService, useValue: sessionApiStub },
        { provide: DeviceApiService, useValue: deviceApiStub },
        { provide: NotificationService, useValue: notificationStub },
      ],
    }).compileComponents();
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.unstubAllGlobals();
  });

  it('createsTheApplicationShellTests', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('rendersTheSimpleGuardOperationalIdentityTests', () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.brand')?.textContent).toContain(
      'SIMPLEGUARD',
    );
    expect(element.querySelector('.status')?.textContent).toContain(
      'NOT AUTHENTICATED',
    );
    expect(element.querySelector('.timestamp')).toBeNull();
    expect(element.querySelector('h1')?.textContent).toContain(
      'Central operacional',
    );
  });

  it('rendersOnlyPairedDevicesWithLocationOnMapTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(
      of([
        deviceTests({ deviceId: 'device-paired', pairingStatus: 'paired' }),
        deviceTests({ deviceId: 'device-unpaired', pairingStatus: 'unpaired' }),
      ]),
    );
    deviceApiStub.latestTelemetryList.mockReturnValueOnce(
      of([telemetryTests({ deviceId: 'device-paired' })]),
    );

    const fixture = await createAuthenticatedFixtureTests();
    const element = fixture.nativeElement as HTMLElement;

    expect(deviceApiStub.latestTelemetryList).toHaveBeenCalledOnce();
    expect(deviceApiStub.latestTelemetry).not.toHaveBeenCalled();
    expect(element.querySelectorAll('.rail-device')).toHaveLength(1);
    expect(element.querySelectorAll('.simpleguard-device-marker')).toHaveLength(
      1,
    );
    expect(element.querySelector('.rail-modules')).toBeNull();
    expect(element.textContent).toContain('1 dispositivos pareados');
    expect(element.textContent).toContain('nao pareados ocultos');
  });

  it('usesCustomPhoneAndDesktopMarkerIconsTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(
      of([
        deviceTests({ deviceId: 'phone-001', type: 'MOBILE' }),
        deviceTests({ deviceId: 'desktop-001', type: 'DESKTOP' }),
      ]),
    );
    deviceApiStub.latestTelemetryList.mockReturnValueOnce(
      of([
        telemetryTests({
          deviceId: 'phone-001',
          latitude: -23.55,
          longitude: -46.633308,
        }),
        telemetryTests({
          deviceId: 'desktop-001',
          latitude: -23.57,
          longitude: -46.61,
        }),
      ]),
    );

    const fixture = await createAuthenticatedFixtureTests();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelectorAll('.simpleguard-device-marker')).toHaveLength(
      2,
    );
    expect(
      element.querySelector(
        '.simpleguard-device-marker .device-marker-icon-phone',
      ),
    ).not.toBeNull();
    expect(
      element.querySelector(
        '.simpleguard-device-marker .device-marker-icon-desktop',
      ),
    ).not.toBeNull();
    expect(element.querySelector('.rail-device')?.getAttribute('title')).toBe(
      'Celular operacional',
    );
    expect(
      element.querySelector('.rail-device')?.getAttribute('data-device-name'),
    ).toBe('Celular operacional');
  });

  it('opensSelectedDevicePanelAfterMarkerClickTests', async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-08-20T13:20:00Z'));
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    deviceApiStub.latestTelemetryList.mockReturnValueOnce(
      of([telemetryTests()]),
    );

    const fixture = await createAuthenticatedFixtureTests();
    clickTests(fixture.nativeElement, '.rail-device');
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('.selected-device-panel')).not.toBeNull();
    expect(element.querySelector('#initial-panel-title')).toBeNull();
    expect(element.textContent).toContain('Dispositivo selecionado');
    expect(element.textContent).toContain('Celular operacional');
    expect(element.textContent).toContain('12%');
    expect(element.textContent).toContain('-101 dBm');
    expect(element.textContent).toContain('CELLULAR');
    expect(element.textContent).toContain('-23,55052, -46,633308');
    expect(element.textContent).toContain('4,5 m');
  });

  it('doesNotRenderMarkerWhenPairedDeviceHasNoCoordinatesTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    deviceApiStub.latestTelemetryList.mockReturnValueOnce(
      of([telemetryTests({ latitude: null, longitude: null })]),
    );

    const fixture = await createAuthenticatedFixtureTests();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('.simpleguard-device-marker')).toBeNull();
    expect(element.textContent).toContain(
      'Nenhum dispositivo pareado com localizacao.',
    );
  });

  it('rendersUnavailableForMissingSelectedDeviceTelemetryTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    deviceApiStub.latestTelemetryList.mockReturnValueOnce(
      of([
        telemetryTests({
          lastUpdatedAt: null,
          batteryLevelPercentage: null,
          batteryCharging: null,
          networkType: null,
          signalStrengthDbm: null,
        }),
      ]),
    );

    const fixture = await createAuthenticatedFixtureTests();
    clickTests(fixture.nativeElement, '.rail-device');
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(
      element.querySelector('.selected-device-panel')?.textContent,
    ).toContain('indisponivel');
    expect(rowsWithValueTests(element, 'indisponivel')).toBeGreaterThanOrEqual(
      4,
    );
  });

  it('marksOldTelemetryAsAttentionTests', async () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-08-20T14:00:00Z'));
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    deviceApiStub.latestTelemetryList.mockReturnValueOnce(
      of([telemetryTests()]),
    );

    const fixture = await createAuthenticatedFixtureTests();
    clickTests(fixture.nativeElement, '.rail-device');
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(
      element.querySelector('.selected-device-panel')?.textContent,
    ).toContain('Atencao');
    expect(
      element.querySelector('.selected-device-panel')?.textContent,
    ).toContain('antigo');
  });

  it('marksLowBatteryAsWarningTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    deviceApiStub.latestTelemetryList.mockReturnValueOnce(
      of([{ ...telemetryTests(), batteryLevelPercentage: 9 }]),
    );

    const fixture = await createAuthenticatedFixtureTests();
    clickTests(fixture.nativeElement, '.rail-device');
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;
    const batteryRow = telemetryRowTests(element, 'Bateria');

    expect(batteryRow?.querySelector('dd')?.textContent).toContain('9%');
    expect(batteryRow?.querySelector('dd')?.classList).toContain('warning');
  });

  it('opensCriticalActionDialogTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    const fixture = await createAuthenticatedFixtureTests();

    clickTests(fixture.nativeElement, '.rail-device');
    fixture.detectChanges();
    clickTests(fixture.nativeElement, '.danger-outline-action');
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[role="dialog"]')).not.toBeNull();
    expect(element.textContent).toContain('Confirmar comando');
    expect(element.textContent).toContain('Celular operacional');
  });

  it('cancelsCriticalActionWithoutEmittingEventTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    const fixture = await createAuthenticatedFixtureTests();

    clickTests(fixture.nativeElement, '.rail-device');
    fixture.detectChanges();
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
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    const fixture = await createAuthenticatedFixtureTests();

    clickTests(fixture.nativeElement, '.rail-device');
    fixture.detectChanges();
    clickTests(fixture.nativeElement, '.danger-outline-action');
    fixture.detectChanges();
    clickTests(fixture.nativeElement, '.critical-dialog .danger-action');
    fixture.detectChanges();

    const event = fixture.componentInstance.criticalActionEvent();
    const element = fixture.nativeElement as HTMLElement;
    expect(event).toEqual({
      actionType: 'TRIGGER_ALARM',
      targetId: 'device-001',
      stepUpRequired: true,
    });
    expect(element.querySelector('[role="dialog"]')).toBeNull();
    expect(element.querySelector('.confirmation-event')?.textContent).toContain(
      'command-confirmed',
    );
  });

  it('showsCriticalActionConfirmationErrorTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    const fixture = await createAuthenticatedFixtureTests();

    fixture.componentInstance.openCriticalActionFailureSimulation();
    fixture.detectChanges();
    clickTests(fixture.nativeElement, '.critical-dialog .danger-action');
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[role="dialog"]')).not.toBeNull();
    expect(element.querySelector('[role="alert"]')).toBeNull();
    expect(notificationStub.error).toHaveBeenCalledWith(
      'Falha ao emitir evento de confirmacao critica.',
    );
    expect(fixture.componentInstance.criticalActionEvent()).toBeNull();
  });

  it('expandsAndCollapsesOperatorPanelTests', async () => {
    const fixture = await createAuthenticatedFixtureTests();
    const element = fixture.nativeElement as HTMLElement;
    const toggle = element.querySelector<HTMLButtonElement>(
      '.operator-panel-toggle',
    );

    expect(fixture.componentInstance.operatorPanelExpanded()).toBe(true);
    expect(toggle?.getAttribute('aria-expanded')).toBe('true');

    toggle?.click();
    fixture.detectChanges();
    expect(fixture.componentInstance.operatorPanelExpanded()).toBe(false);
    expect(element.querySelector('.workspace')?.classList).toContain(
      'operator-panel-collapsed',
    );
    expect(toggle?.getAttribute('aria-expanded')).toBe('false');

    toggle?.click();
    fixture.detectChanges();
    expect(fixture.componentInstance.operatorPanelExpanded()).toBe(true);
  });

  it('loadsSelectedDeviceTelemetryOnDemandTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(
      of([
        deviceTests(),
        deviceTests({ deviceId: 'device-unpaired', pairingStatus: 'unpaired' }),
      ]),
    );
    deviceApiStub.latestTelemetryList.mockReturnValueOnce(
      of([telemetryTests({ deviceId: 'outside-paired-devices' })]),
    );
    deviceApiStub.latestTelemetry.mockReturnValueOnce(
      of(
        telemetryTests({
          batteryCharging: true,
          signalStrengthDbm: -80,
          accuracyMeters: null,
          lastUpdatedAt: 'invalid-date',
        }),
      ),
    );

    const fixture = await createAuthenticatedFixtureTests();
    await fixture.componentInstance.selectDevice('missing-device');
    await fixture.componentInstance.selectDevice('device-unpaired');
    expect(fixture.componentInstance.selectedDeviceId()).toBeNull();

    await fixture.componentInstance.selectDevice('device-001');
    await fixture.whenStable();
    fixture.detectChanges();

    const telemetry = fixture.componentInstance.latestTelemetry();
    expect(deviceApiStub.latestTelemetry).toHaveBeenCalledWith('device-001');
    expect(fixture.componentInstance.selectedDeviceId()).toBe('device-001');
    expect(fixture.componentInstance.telemetryLoading()).toBe(false);
    expect(fixture.componentInstance.telemetryError()).toBeNull();
    expect(fixture.componentInstance.formatBattery(telemetry)).toBe(
      '12% carregando',
    );
    expect(fixture.componentInstance.formatSignal(telemetry)).toBe('-80 dBm');
    expect(fixture.componentInstance.isSignalWeak(telemetry)).toBe(false);
    expect(fixture.componentInstance.formatLastUpdated(telemetry)).toBe(
      'indisponivel',
    );
    expect(fixture.componentInstance.formatAccuracy(telemetry)).toBe(
      'indisponivel',
    );
    expect(fixture.componentInstance.deviceOperationalState(null)).toBe(
      'indisponivel',
    );
    expect(
      fixture.componentInstance.deviceIconKind(deviceTests({ type: 'OTHER' })),
    ).toBe('generic');

    fixture.componentInstance.focusSelectedDevice();
    fixture.componentInstance.clearSelectedDevice();
    expect(fixture.componentInstance.selectedDeviceId()).toBeNull();
  });

  it('showsTelemetryLoadingFailureWhenOnDemandRequestFailsTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    deviceApiStub.latestTelemetryList.mockReturnValueOnce(
      of([telemetryTests({ deviceId: 'outside-paired-devices' })]),
    );
    deviceApiStub.latestTelemetry.mockReturnValueOnce(
      throwError(() => ({ error: { mensagem: 'Telemetria indisponivel.' } })),
    );

    const fixture = await createAuthenticatedFixtureTests();
    await fixture.componentInstance.selectDevice('device-001');
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.componentInstance.telemetryLoading()).toBe(false);
    expect(fixture.componentInstance.telemetryError()).toBe(
      'Telemetria indisponivel.',
    );
    expect(notificationStub.error).toHaveBeenCalledWith(
      'Telemetria indisponivel.',
    );
  });

  it('doesNotRequestTelemetryWhenThereAreNoPairedDevicesTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(
      of([deviceTests({ pairingStatus: 'unpaired' })]),
    );

    const fixture = await createAuthenticatedFixtureTests();
    const element = fixture.nativeElement as HTMLElement;

    expect(deviceApiStub.latestTelemetryList).not.toHaveBeenCalled();
    expect(element.textContent).toContain('Nenhum dispositivo pareado.');
    expect(element.querySelector('.rail-empty')).not.toBeNull();
  });

  it('showsDeviceLoadingFailureTests', async () => {
    deviceApiStub.list.mockReturnValueOnce(
      throwError(() => ({ error: { mensagem: 'Falha ao listar.' } })),
    );

    const fixture = await createAuthenticatedFixtureTests();
    const element = fixture.nativeElement as HTMLElement;

    expect(fixture.componentInstance.devices()).toEqual([]);
    expect(fixture.componentInstance.devicesError()).toBe('Falha ao listar.');
    expect(element.textContent).toContain('Falha ao listar.');
    expect(notificationStub.error).toHaveBeenCalledWith('Falha ao listar.');
  });

  it('selectsDeviceFromMapMarkerAndRemovesStaleMarkersTests', async () => {
    let resizeCallback: ResizeObserverCallback | null = null;
    const disconnect = vi.fn();
    const observe = vi.fn();
    class ResizeObserverStub {
      observe = observe;
      disconnect = disconnect;
      unobserve = vi.fn();

      constructor(callback: ResizeObserverCallback) {
        resizeCallback = callback;
      }
    }
    vi.stubGlobal('ResizeObserver', ResizeObserverStub);
    deviceApiStub.list.mockReturnValueOnce(of([deviceTests()]));
    deviceApiStub.latestTelemetryList.mockReturnValueOnce(
      of([
        telemetryTests({ batteryLevelPercentage: 70, signalStrengthDbm: -70 }),
      ]),
    );

    const fixture = await createAuthenticatedFixtureTests();
    const element = fixture.nativeElement as HTMLElement;

    expect(observe).toHaveBeenCalled();
    (resizeCallback as unknown as ResizeObserverCallback)(
      [],
      {} as ResizeObserver,
    );
    clickTests(element, '.simpleguard-device-marker');
    await fixture.whenStable();
    fixture.detectChanges();
    expect(fixture.componentInstance.selectedDeviceId()).toBe('device-001');

    fixture.componentInstance.devices.set([]);
    fixture.componentInstance.telemetryByDevice.set({});
    fixture.componentInstance.focusSelectedDevice();
    fixture.detectChanges();

    expect(element.querySelector('.simpleguard-device-marker')).toBeNull();
    fixture.destroy();
    (
      fixture.componentInstance as unknown as { invalidateMapSize: () => void }
    ).invalidateMapSize();
    expect(disconnect).toHaveBeenCalled();
  });

  it('delegatesLoginAndLogoutToOidcClientTests', async () => {
    const fixture = TestBed.createComponent(App);

    await fixture.componentInstance.login();
    await fixture.componentInstance.logout();

    expect(authServiceStub.login).toHaveBeenCalledOnce();
    expect(authServiceStub.logout).toHaveBeenCalledOnce();
  });

  it('showsSessionValidationFailureWhenAuthenticatedSessionFailsTests', async () => {
    authServiceStub.state.set({ status: 'authenticated' });
    sessionApiStub.me.mockReturnValueOnce(
      throwError(() => new Error('offline')),
    );
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.componentInstance.session()).toBeNull();
    expect(fixture.componentInstance.sessionError()).toBe(
      'Nao foi possivel validar a sessao na API.',
    );
    expect(notificationStub.error).toHaveBeenCalledWith(
      'Nao foi possivel validar a sessao na API.',
    );
  });

  it('showsBackendMessageWhenSessionValidationFailsTests', async () => {
    authServiceStub.state.set({ status: 'authenticated' });
    sessionApiStub.me.mockReturnValueOnce(
      throwError(() => ({
        error: {
          erro_code: 'INVALID_TOKEN',
          mensagem: 'Token invalido ou sessao expirada.',
        },
      })),
    );
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.componentInstance.sessionError()).toBe(
      'Token invalido ou sessao expirada.',
    );
    expect(notificationStub.error).toHaveBeenCalledWith(
      'Token invalido ou sessao expirada.',
    );
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

  function telemetryRowTests(root: HTMLElement, label: string): Element | null {
    return (
      Array.from(root.querySelectorAll('.selected-device-list div')).find(
        (row) => row.querySelector('dt')?.textContent?.trim() === label,
      ) ?? null
    );
  }

  function rowsWithValueTests(root: HTMLElement, value: string): number {
    return Array.from(root.querySelectorAll('.selected-device-list dd')).filter(
      (row) => row.textContent?.includes(value),
    ).length;
  }

  function sessionTests() {
    return {
      subject: '00000000-0000-0000-0000-000000000001',
      email: 'admin@simpleguard.local',
      displayName: 'SimpleGuard Admin',
      role: 'ADMIN',
    };
  }

  function deviceTests(overrides: Partial<Device> = {}): Device {
    return {
      deviceId: overrides.deviceId ?? 'device-001',
      name: overrides.name ?? 'Celular operacional',
      type: overrides.type ?? 'MOBILE',
      platform: overrides.platform ?? 'ANDROID',
      pairingStatus: overrides.pairingStatus ?? 'paired',
      createdAt: overrides.createdAt ?? '2026-08-20T12:00:00Z',
    };
  }

  function telemetryTests(
    overrides: Partial<LatestDeviceTelemetry> = {},
  ): LatestDeviceTelemetry {
    return {
      deviceId: overrides.deviceId ?? 'device-001',
      deviceName: overrides.deviceName ?? 'Celular operacional',
      lastUpdatedAt:
        overrides.lastUpdatedAt === undefined
          ? '2026-08-20T10:17:00-03:00'
          : overrides.lastUpdatedAt,
      batteryLevelPercentage:
        overrides.batteryLevelPercentage === undefined
          ? 12
          : overrides.batteryLevelPercentage,
      batteryCharging:
        overrides.batteryCharging === undefined
          ? false
          : overrides.batteryCharging,
      networkType:
        overrides.networkType === undefined
          ? 'CELLULAR'
          : overrides.networkType,
      signalStrengthDbm:
        overrides.signalStrengthDbm === undefined
          ? -101
          : overrides.signalStrengthDbm,
      latitude:
        overrides.latitude === undefined ? -23.55052 : overrides.latitude,
      longitude:
        overrides.longitude === undefined ? -46.633308 : overrides.longitude,
      accuracyMeters:
        overrides.accuracyMeters === undefined ? 4.5 : overrides.accuracyMeters,
    };
  }
});
