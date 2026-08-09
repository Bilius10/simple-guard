export type DeviceType = 'MOBILE' | 'NOTEBOOK' | 'DESKTOP' | 'OTHER';

export type DevicePlatform = 'ANDROID' | 'WINDOWS' | 'LINUX' | 'MACOS' | 'OTHER';

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
  readonly pairingStatus: 'unpaired';
  readonly createdAt: string;
}
