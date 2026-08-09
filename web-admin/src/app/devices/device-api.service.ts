import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

import { SIMPLEGUARD_AUTH_CONFIG } from '../auth/auth.config';
import { CreateDeviceRequest, Device, PairingSession } from './device.models';

@Injectable({ providedIn: 'root' })
export class DeviceApiService {

  private readonly http = inject(HttpClient);
  private readonly config = inject(SIMPLEGUARD_AUTH_CONFIG);

  list() {
    return this.http.get<Device[]>(`${this.config.apiBaseUrl}/devices`);
  }

  create(request: CreateDeviceRequest) {
    return this.http.post<Device>(`${this.config.apiBaseUrl}/devices`, request);
  }

  generatePairingSession(deviceId: string) {
    return this.http.post<PairingSession>(
      `${this.config.apiBaseUrl}/devices/${deviceId}/pairing-sessions`,
      {},
    );
  }
}
