import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { of, tap } from 'rxjs';

import { SIMPLEGUARD_AUTH_CONFIG } from '../auth/auth.config';
import type {
  CreateDeviceRequest,
  Device,
  DeviceUnpairingDecisionResponse,
  DeviceUnpairingRequest,
  DeviceUnpairingRequestTerminalStatus,
  LatestDeviceTelemetry,
  PairingSession,
  UnpairDeviceResponse,
} from './device.models';

const PAIRING_SESSION_CACHE_TTL_MS = 2 * 60 * 1000;
const PAIRING_SESSION_CACHE_PREFIX = 'simpleguard.pairing-session.';

interface CachedPairingSession {
  readonly session: PairingSession;
  readonly expiresAt: number;
}

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

  latestTelemetry(deviceId: string) {
    return this.http.get<LatestDeviceTelemetry>(
      `${this.config.apiBaseUrl}/devices/${deviceId}/telemetry/latest`,
    );
  }

  latestTelemetryList() {
    return this.http.get<LatestDeviceTelemetry[]>(
      `${this.config.apiBaseUrl}/devices/telemetry/latest`,
    );
  }

  generatePairingSession(deviceId: string) {
    const now = Date.now();
    const cached = this.readCachedPairingSession(deviceId);
    if (cached && cached.expiresAt > now) {
      return of(cached.session);
    }
    this.clearCachedPairingSession(deviceId);

    return this.http
      .post<PairingSession>(
        `${this.config.apiBaseUrl}/devices/${deviceId}/pairing-sessions`,
        {},
      )
      .pipe(tap((session) => this.cachePairingSession(deviceId, session)));
  }

  unpair(deviceId: string) {
    return this.http
      .delete<UnpairDeviceResponse>(
        `${this.config.apiBaseUrl}/devices/${deviceId}/unpairing`,
      )
      .pipe(tap(() => this.clearCachedPairingSession(deviceId)));
  }

  listUnpairingRequests() {
    return this.http.get<DeviceUnpairingRequest[]>(
      `${this.config.apiBaseUrl}/devices/unpairing-requests`,
    );
  }

  decideUnpairingRequest(
    requestId: string,
    status: DeviceUnpairingRequestTerminalStatus,
  ) {
    return this.http.post<DeviceUnpairingDecisionResponse>(
      `${this.config.apiBaseUrl}/devices/unpairing-requests/${requestId}/decision`,
      { status },
    );
  }

  private cachePairingSession(deviceId: string, session: PairingSession): void {
    const now = Date.now();
    const sessionExpiresAt = Date.parse(session.expiresAt);
    const cacheExpiresAt = Math.min(
      now + PAIRING_SESSION_CACHE_TTL_MS,
      sessionExpiresAt,
    );

    if (
      session.status !== 'waiting' ||
      !Number.isFinite(sessionExpiresAt) ||
      cacheExpiresAt <= now
    ) {
      this.clearCachedPairingSession(deviceId);
      return;
    }

    const cached: CachedPairingSession = {
      session,
      expiresAt: cacheExpiresAt,
    };
    sessionStorage.setItem(this.cacheKey(deviceId), JSON.stringify(cached));
  }

  private readCachedPairingSession(
    deviceId: string,
  ): CachedPairingSession | null {
    const rawValue = sessionStorage.getItem(this.cacheKey(deviceId));
    if (!rawValue) {
      return null;
    }

    try {
      const cached = JSON.parse(rawValue) as CachedPairingSession;
      if (
        !cached.session ||
        cached.session.deviceId !== deviceId ||
        cached.session.status !== 'waiting'
      ) {
        this.clearCachedPairingSession(deviceId);
        return null;
      }
      return cached;
    } catch {
      this.clearCachedPairingSession(deviceId);
      return null;
    }
  }

  private clearCachedPairingSession(deviceId: string): void {
    sessionStorage.removeItem(this.cacheKey(deviceId));
  }

  private cacheKey(deviceId: string): string {
    return `${PAIRING_SESSION_CACHE_PREFIX}${deviceId}`;
  }
}
