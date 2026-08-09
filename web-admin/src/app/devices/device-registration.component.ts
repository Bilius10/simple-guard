import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { DeviceApiService } from './device-api.service';
import { CreateDeviceRequest, Device, DevicePlatform, DeviceType } from './device.models';

interface SelectOption<T> {
  readonly value: T;
  readonly label: string;
}

@Component({
  selector: 'sg-device-registration',
  imports: [ReactiveFormsModule],
  templateUrl: './device-registration.component.html',
  styleUrl: './device-registration.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeviceRegistrationComponent implements OnInit {

  readonly devices = signal<Device[]>([]);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly submitError = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);

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

  ngOnInit(): void {
    void this.loadDevices();
  }

  async loadDevices(): Promise<void> {
    this.loading.set(true);
    this.loadError.set(null);

    try {
      this.devices.set(await firstValueFrom(this.deviceApi.list()));
    } catch {
      this.devices.set([]);
      this.loadError.set('Nao foi possivel carregar os dispositivos.');
    } finally {
      this.loading.set(false);
    }
  }

  async submit(): Promise<void> {
    this.successMessage.set(null);
    this.submitError.set(null);

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
      this.successMessage.set(`${created.name} cadastrado e pendente de pareamento.`);
    } catch {
      this.submitError.set('Nao foi possivel cadastrar o dispositivo. Revise os dados e tente novamente.');
    } finally {
      this.submitting.set(false);
    }
  }

  typeLabel(type: DeviceType): string {
    return this.typeOptions.find(option => option.value === type)?.label ?? type;
  }

  platformLabel(platform: DevicePlatform): string {
    return this.platformOptions.find(option => option.value === platform)?.label ?? platform;
  }
}
