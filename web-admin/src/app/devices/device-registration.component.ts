import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { NotificationService } from '../notifications/notification.service';
import { DeviceApiService } from './device-api.service';
import { CreateDeviceRequest, Device, DevicePlatform, DeviceType, PairingSession } from './device.models';

interface SelectOption<T> {
  readonly value: T;
  readonly label: string;
}

@Component({
  selector: 'sg-device-registration',
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './device-registration.component.html',
  styleUrl: './device-registration.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeviceRegistrationComponent implements OnInit, OnDestroy {

  readonly devices = signal<Device[]>([]);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly pairingLoadingDeviceId = signal<string | null>(null);
  readonly activePairing = signal<{ readonly device: Device; readonly session: PairingSession } | null>(null);
  readonly pairingExpired = signal(false);

  readonly typeOptions: readonly SelectOption<DeviceType>[] = [
    { value: 'MOBILE', label: 'Celular' },
    { value: 'NOTEBOOK', label: 'Notebook' },
    { value: 'DESKTOP', label: 'Desktop' },
    { value: 'OTHER', label: 'Outro' },
  ];

  readonly platformOptions: readonly SelectOption<DevicePlatform>[] = [
    { value: 'ANDROID', label: 'Android' },
    { value: 'WINDOWS', label: 'Windows' },
    { value: 'LINUX', label: 'Linux' },
    { value: 'MACOS', label: 'macOS' },
    { value: 'OTHER', label: 'Outra' },
  ];

  readonly form = new FormGroup({
    name: new FormControl('', {
      nonNullable: true,
      validators: [Validators.required, Validators.pattern(/\S/), Validators.maxLength(160)],
    }),
    type: new FormControl<DeviceType | null>(null, Validators.required),
    platform: new FormControl<DevicePlatform | null>(null, Validators.required),
  });

  private readonly deviceApi = inject(DeviceApiService);
  private readonly notifications = inject(NotificationService);
  private expirationTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    void this.loadDevices();
  }

  ngOnDestroy(): void {
    this.clearExpirationTimer();
  }

  async loadDevices(): Promise<void> {
    this.loading.set(true);
    this.loadError.set(null);

    try {
      this.devices.set(await firstValueFrom(this.deviceApi.list()));
    } catch {
      this.devices.set([]);
      this.loadError.set('Nao foi possivel carregar os dispositivos.');
      this.notifications.error('Nao foi possivel carregar os dispositivos.');
    } finally {
      this.loading.set(false);
    }
  }

  async submit(): Promise<void> {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    if (value.type === null || value.platform === null) {
      return;
    }

    const request: CreateDeviceRequest = {
      name: value.name.trim(),
      type: value.type,
      platform: value.platform,
    };

    this.submitting.set(true);
    try {
      const created = await firstValueFrom(this.deviceApi.create(request));
      this.devices.update(devices => [created, ...devices]);
      this.form.reset();
      this.notifications.success(`${created.name} cadastrado e pendente de pareamento.`);
    } catch {
      this.notifications.error('Nao foi possivel cadastrar o dispositivo. Revise os dados e tente novamente.');
    } finally {
      this.submitting.set(false);
    }
  }

  async generatePairingSession(device: Device): Promise<void> {
    this.pairingLoadingDeviceId.set(device.deviceId);

    try {
      const session = await firstValueFrom(this.deviceApi.generatePairingSession(device.deviceId));
      this.activePairing.set({ device, session });
      this.watchExpiration(session);
    } catch {
      this.notifications.error(`Nao foi possivel gerar o codigo de pareamento para ${device.name}.`);
    } finally {
      this.pairingLoadingDeviceId.set(null);
    }
  }

  closePairingSession(): void {
    this.clearExpirationTimer();
    this.activePairing.set(null);
    this.pairingExpired.set(false);
  }

  typeLabel(type: DeviceType): string {
    return this.typeOptions.find(option => option.value === type)?.label ?? type;
  }

  platformLabel(platform: DevicePlatform): string {
    return this.platformOptions.find(option => option.value === platform)?.label ?? platform;
  }

  private watchExpiration(session: PairingSession): void {
    this.clearExpirationTimer();
    const remainingMilliseconds = Date.parse(session.expiresAt) - Date.now();

    if (remainingMilliseconds <= 0) {
      this.pairingExpired.set(true);
      return;
    }

    this.pairingExpired.set(false);
    this.expirationTimer = setTimeout(() => this.pairingExpired.set(true), remainingMilliseconds);
  }

  private clearExpirationTimer(): void {
    if (this.expirationTimer !== null) {
      clearTimeout(this.expirationTimer);
      this.expirationTimer = null;
    }
  }
}
