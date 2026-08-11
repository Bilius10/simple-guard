import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { SIMPLEGUARD_AUTH_CONFIG } from '../auth/auth.config';
import { DeviceApiService } from './device-api.service';
import { CreateDeviceRequest, Device, PairingSession } from './device.models';

describe('DeviceApiServiceTests', () => {
  let http: HttpTestingController;
  let service: DeviceApiService;

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
    service = TestBed.inject(DeviceApiService);
  });

  afterEach(() => {
    http.verify();
  });

  it('listsDevicesFromApiTests', () => {
    const devices: Device[] = [deviceTests()];

    service.list().subscribe(result => {
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

    service.create(createRequest).subscribe(result => {
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

    service.generatePairingSession('device-001').subscribe(result => {
      expect(result).toEqual(session);
    });

    const request = http.expectOne('/api/devices/device-001/pairing-sessions');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush(session);
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
});
