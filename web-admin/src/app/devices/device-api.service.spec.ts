import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SIMPLEGUARD_AUTH_CONFIG } from '../auth/auth.config';
import { DeviceApiService } from './device-api.service';
import {
  CreateDeviceRequest,
  Device,
  DeviceUnpairingRequest,
  PairingSession,
} from './device.models';

describe('DeviceApiServiceTests', () => {
  let http: HttpTestingController;
  let service: DeviceApiService;

  beforeEach(() => {
    sessionStorage.clear();
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
    service = TestBed.inject(DeviceApiService);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
    vi.useRealTimers();
  });

  it('listsDevicesFromApiTests', () => {
    const devices: Device[] = [deviceTests()];

    service.list().subscribe((result) => {
      expect(result).toEqual(devices);
    });

    const request = http.expectOne('/api/devices');
    expect(request.request.method).toBe('GET');
    request.flush(devices);
  });

  it('createsDeviceThroughApiTests', () => {
    const createRequest: CreateDeviceRequest = {
      name: 'Notebook operacional',
      type: 'NOTEBOOK',
      platform: 'LINUX',
    };
    const created = deviceTests(createRequest);

    service.create(createRequest).subscribe((result) => {
      expect(result).toEqual(created);
    });

    const request = http.expectOne('/api/devices');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(createRequest);
    request.flush(created);
  });

  it('generatesPairingSessionThroughApiTests', () => {
    const session: PairingSession = {
      pairingSessionId: 'pairing-session-001',
      deviceId: 'device-001',
      pairingCode: 'ABCD-2345',
      status: 'waiting',
      expiresAt: '2026-08-11T01:00:00Z',
      createdAt: '2026-08-11T00:55:00Z',
    };

    service.generatePairingSession('device-001').subscribe((result) => {
      expect(result).toEqual(session);
    });

    const request = http.expectOne('/api/devices/device-001/pairing-sessions');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush(session);
  });

  it('reusesPairingSessionFromSessionStorageForTwoMinutesTests', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-08-11T00:55:00Z'));
    const session: PairingSession = {
      pairingSessionId: 'pairing-session-cached',
      deviceId: 'device-001',
      pairingCode: 'CACHE-2345',
      status: 'waiting',
      expiresAt: '2026-08-11T01:00:00Z',
      createdAt: '2026-08-11T00:55:00Z',
    };

    service.generatePairingSession('device-001').subscribe();
    http.expectOne('/api/devices/device-001/pairing-sessions').flush(session);

    let cachedResult: PairingSession | undefined;
    service
      .generatePairingSession('device-001')
      .subscribe((result) => (cachedResult = result));

    http.expectNone('/api/devices/device-001/pairing-sessions');
    expect(cachedResult).toEqual(session);
  });

  it('requestsNewPairingSessionAfterTwoMinuteCacheExpiresTests', () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-08-11T00:55:00Z'));
    const session: PairingSession = {
      pairingSessionId: 'pairing-session-expiring-cache',
      deviceId: 'device-001',
      pairingCode: 'CACHE-6789',
      status: 'waiting',
      expiresAt: '2026-08-11T01:00:00Z',
      createdAt: '2026-08-11T00:55:00Z',
    };

    service.generatePairingSession('device-001').subscribe();
    http.expectOne('/api/devices/device-001/pairing-sessions').flush(session);
    vi.advanceTimersByTime(2 * 60 * 1000 + 1);

    service.generatePairingSession('device-001').subscribe();
    const request = http.expectOne('/api/devices/device-001/pairing-sessions');
    expect(request.request.method).toBe('POST');
    request.flush({ ...session, pairingSessionId: 'pairing-session-renewed' });
  });

  it('ignoresInvalidCachedPairingSessionTests', () => {
    sessionStorage.setItem(
      'simpleguard.pairing-session.device-001',
      JSON.stringify({
        session: { ...pairingSessionTests(), status: 'used' },
        expiresAt: Date.now() + 60_000,
      }),
    );

    service.generatePairingSession('device-001').subscribe();

    const request = http.expectOne('/api/devices/device-001/pairing-sessions');
    expect(request.request.method).toBe('POST');
    expect(
      sessionStorage.getItem('simpleguard.pairing-session.device-001'),
    ).toBeNull();
    request.flush(pairingSessionTests());
  });

  it('ignoresMalformedCachedPairingSessionTests', () => {
    sessionStorage.setItem(
      'simpleguard.pairing-session.device-001',
      '{not-json',
    );

    service.generatePairingSession('device-001').subscribe();

    const request = http.expectOne('/api/devices/device-001/pairing-sessions');
    expect(request.request.method).toBe('POST');
    expect(
      sessionStorage.getItem('simpleguard.pairing-session.device-001'),
    ).toBeNull();
    request.flush(pairingSessionTests());
  });

  it('unpairsDeviceThroughApiTests', () => {
    service.unpair('device-001').subscribe((result) => {
      expect(result.pairingStatus).toBe('unpaired');
      expect(result.revokedKeyCount).toBe(1);
    });

    const request = http.expectOne('/api/devices/device-001/unpairing');
    expect(request.request.method).toBe('DELETE');
    expect(request.request.headers.has('X-SimpleGuard-Confirmation')).toBe(
      false,
    );
    request.flush({
      deviceId: 'device-001',
      pairingStatus: 'unpaired',
      revokedKeyCount: 1,
      unpairedAt: '2026-08-11T12:00:00Z',
    });
  });

  it('listsPendingUnpairingRequestsFromApiTests', () => {
    const requests: DeviceUnpairingRequest[] = [unpairingRequestTests()];

    service.listUnpairingRequests().subscribe((result) => {
      expect(result).toEqual(requests);
    });

    const request = http.expectOne('/api/devices/unpairing-requests');
    expect(request.request.method).toBe('GET');
    request.flush(requests);
  });

  it('approvesPendingUnpairingRequestTests', () => {
    service
      .decideUnpairingRequest('request-001', 'approved')
      .subscribe((result) => {
        expect(result.request.status).toBe('approved');
        expect(result.unpairing?.pairingStatus).toBe('unpaired');
      });

    const request = http.expectOne(
      '/api/devices/unpairing-requests/request-001/decision',
    );
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      status: 'approved',
    });
    request.flush({
      request: { ...unpairingRequestTests(), status: 'approved' },
      unpairing: {
        deviceId: 'device-001',
        pairingStatus: 'unpaired',
        revokedKeyCount: 1,
        unpairedAt: '2026-08-11T12:00:00Z',
      },
    });
  });

  it('rejectsPendingUnpairingRequestTests', () => {
    service
      .decideUnpairingRequest('request-001', 'rejected')
      .subscribe((result) => {
        expect(result.request.status).toBe('rejected');
        expect(result.unpairing).toBeNull();
      });

    const request = http.expectOne(
      '/api/devices/unpairing-requests/request-001/decision',
    );
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ status: 'rejected' });
    request.flush({
      request: { ...unpairingRequestTests(), status: 'rejected' },
      unpairing: null,
    });
  });

  function deviceTests(overrides: Partial<CreateDeviceRequest> = {}): Device {
    return {
      deviceId: 'device-001',
      name: overrides.name ?? 'Celular operacional',
      type: overrides.type ?? 'MOBILE',
      platform: overrides.platform ?? 'ANDROID',
      pairingStatus: 'unpaired',
      createdAt: '2026-08-11T00:00:00Z',
    };
  }

  function pairingSessionTests(): PairingSession {
    return {
      pairingSessionId: 'pairing-session-001',
      deviceId: 'device-001',
      pairingCode: 'ABCD-2345',
      status: 'waiting',
      expiresAt: '2026-08-11T01:00:00Z',
      createdAt: '2026-08-11T00:55:00Z',
    };
  }

  function unpairingRequestTests(): DeviceUnpairingRequest {
    return {
      requestId: 'request-001',
      deviceId: 'device-001',
      deviceName: 'Celular operacional',
      agentInstanceId: 'android-agent-001',
      status: 'pending',
      requestedAt: '2026-08-11T12:00:00Z',
      decidedAt: null,
    };
  }
});
