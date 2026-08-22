export type DeviceType = 'MOBILE' | 'NOTEBOOK' | 'DESKTOP' | 'OTHER';

export type DevicePlatform =
  'ANDROID' | 'WINDOWS' | 'LINUX' | 'MACOS' | 'OTHER';

export interface CreateDeviceRequest {
  readonly name: string;
  readonly type: DeviceType;
  readonly platform: DevicePlatform;
}

export interface Device {
  readonly deviceId: string;
  readonly name: string;
  readonly type: DeviceType;
  readonly platform: DevicePlatform;
  readonly pairingStatus: 'unpaired' | 'paired';
  readonly createdAt: string;
}

export interface LatestDeviceTelemetry {
  readonly deviceId: string;
  readonly deviceName: string;
  readonly lastUpdatedAt: string | null;
  readonly batteryLevelPercentage: number | null;
  readonly batteryCharging: boolean | null;
  readonly networkType: string | null;
  readonly signalStrengthDbm: number | null;
  readonly latitude: number | null;
  readonly longitude: number | null;
  readonly accuracyMeters: number | null;
}

export interface PairingSession {
  readonly pairingSessionId: string;
  readonly deviceId: string;
  readonly pairingCode: string;
  readonly status: 'waiting' | 'used' | 'expired';
  readonly expiresAt: string;
  readonly createdAt: string;
}

export interface UnpairDeviceResponse {
  readonly deviceId: string;
  readonly pairingStatus: 'unpaired';
  readonly revokedKeyCount: number;
  readonly unpairedAt: string;
}

export interface DeviceUnpairingRequest {
  readonly requestId: string;
  readonly deviceId: string;
  readonly deviceName: string;
  readonly agentInstanceId: string;
  readonly status: DeviceUnpairingRequestStatus;
  readonly requestedAt: string;
  readonly decidedAt: string | null;
}

export type DeviceUnpairingRequestStatus = 'pending' | 'approved' | 'rejected';
export type DeviceUnpairingRequestTerminalStatus = Exclude<
  DeviceUnpairingRequestStatus,
  'pending'
>;

export interface DeviceUnpairingDecisionResponse {
  readonly request: DeviceUnpairingRequest;
  readonly unpairing: UnpairDeviceResponse | null;
}
