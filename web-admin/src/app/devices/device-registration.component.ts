import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  OnDestroy,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import {
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { CriticalActionDialogComponent } from '../critical-action/critical-action-dialog.component';
import { CriticalActionConfirmationRequest } from '../critical-action/critical-action.models';
import { NotificationService } from '../notifications/notification.service';
import { apiErrorMessage } from '../shared/api-error-message';
import { DeviceApiService } from './device-api.service';
import {
  CreateDeviceRequest,
  Device,
  DevicePlatform,
  DeviceType,
  DeviceUnpairingRequest,
  PairingSession,
} from './device.models';

interface SelectOption<T> {
  readonly value: T;
  readonly label: string;
}

@Component({
  selector: 'sg-device-registration',
  imports: [CriticalActionDialogComponent, DatePipe, ReactiveFormsModule],
  templateUrl: './device-registration.component.html',
  styleUrl: './device-registration.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeviceRegistrationComponent implements OnInit, OnDestroy {
  readonly devices = signal<Device[]>([]);
  readonly unpairingRequests = signal<DeviceUnpairingRequest[]>([]);
  readonly loading = signal(true);
  readonly submitting = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly pairingLoadingDeviceId = signal<string | null>(null);
  readonly activePairing = signal<{
    readonly device: Device;
    readonly session: PairingSession;
  } | null>(null);
  readonly pairingExpired = signal(false);
  readonly unpairAction = signal<CriticalActionConfirmationRequest | null>(
    null,
  );
  readonly unpairError = signal<string | null>(null);
  readonly unpairingDeviceId = signal<string | null>(null);
  readonly unpairingRequestAction =
    signal<CriticalActionConfirmationRequest | null>(null);
  readonly unpairingRequestError = signal<string | null>(null);
  readonly decidingUnpairingRequestId = signal<string | null>(null);

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
      validators: [
        Validators.required,
        Validators.pattern(/\S/),
        Validators.maxLength(160),
      ],
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
      const [devices, unpairingRequests] = await Promise.all([
        firstValueFrom(this.deviceApi.list()),
        firstValueFrom(this.deviceApi.listUnpairingRequests()),
      ]);
      this.devices.set(devices);
      this.unpairingRequests.set(unpairingRequests);
    } catch (error) {
      const message = apiErrorMessage(
        error,
        'Nao foi possivel carregar os dispositivos.',
      );
      this.devices.set([]);
      this.unpairingRequests.set([]);
      this.loadError.set(message);
      this.notifications.error(message);
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
      this.devices.update((devices) => [created, ...devices]);
      this.form.reset();
      this.notifications.success(
        `${created.name} cadastrado e pendente de pareamento.`,
      );
    } catch (error) {
      this.notifications.error(
        apiErrorMessage(
          error,
          'Nao foi possivel cadastrar o dispositivo. Revise os dados e tente novamente.',
        ),
      );
    } finally {
      this.submitting.set(false);
    }
  }

  async generatePairingSession(device: Device): Promise<void> {
    this.pairingLoadingDeviceId.set(device.deviceId);

    try {
      const session = await firstValueFrom(
        this.deviceApi.generatePairingSession(device.deviceId),
      );
      this.activePairing.set({ device, session });
      this.watchExpiration(session);
    } catch (error) {
      this.notifications.error(
        apiErrorMessage(
          error,
          `Nao foi possivel gerar o codigo de pareamento para ${device.name}.`,
        ),
      );
    } finally {
      this.pairingLoadingDeviceId.set(null);
    }
  }

  closePairingSession(): void {
    this.clearExpirationTimer();
    this.activePairing.set(null);
    this.pairingExpired.set(false);
  }

  openUnpairConfirmation(device: Device): void {
    this.unpairError.set(null);
    this.unpairAction.set({
      actionType: 'UNPAIR_DEVICE',
      targetId: device.deviceId,
      targetName: device.name,
      consequence:
        'As chaves ativas serao revogadas. O agente deixara de enviar telemetria e receber comandos.',
      connectivityState: device.pairingStatus,
      lastKnownLocation: 'indisponivel',
      lastUpdatedAt: device.createdAt,
      stepUpRequirement: 'not_required',
    });
  }

  cancelUnpair(): void {
    this.unpairAction.set(null);
    this.unpairError.set(null);
  }

  async confirmUnpair(
    action: CriticalActionConfirmationRequest,
  ): Promise<void> {
    const device = this.devices().find(
      (candidate) => candidate.deviceId === action.targetId,
    );
    if (!device) {
      this.unpairError.set('O dispositivo nao esta mais disponivel na lista.');
      return;
    }

    this.unpairingDeviceId.set(device.deviceId);
    this.unpairError.set(null);
    try {
      const response = await firstValueFrom(
        this.deviceApi.unpair(device.deviceId),
      );
      this.devices.update((devices) =>
        devices.map((current) =>
          current.deviceId === response.deviceId
            ? { ...current, pairingStatus: response.pairingStatus }
            : current,
        ),
      );
      this.unpairingRequests.update((requests) =>
        requests.filter((request) => request.deviceId !== response.deviceId),
      );
      this.unpairAction.set(null);
      this.notifications.success(
        `${device.name} foi despareado e suas chaves foram revogadas.`,
      );
    } catch (error) {
      const detailMessage = apiErrorMessage(
        error,
        'Nao foi possivel desparear o dispositivo. Nenhuma alteracao local foi aplicada.',
      );
      const notificationMessage = apiErrorMessage(
        error,
        `Nao foi possivel desparear ${device.name}.`,
      );
      this.unpairError.set(detailMessage);
      this.notifications.error(notificationMessage);
    } finally {
      this.unpairingDeviceId.set(null);
    }
  }

  openApproveUnpairingRequestConfirmation(
    request: DeviceUnpairingRequest,
  ): void {
    this.unpairingRequestError.set(null);
    this.unpairingRequestAction.set({
      actionType: 'UNPAIR_DEVICE',
      targetId: request.requestId,
      targetName: request.deviceName,
      consequence:
        'As chaves ativas serao revogadas. O dispositivo passara para despareado.',
      connectivityState: 'paired',
      lastKnownLocation: `agente ${request.agentInstanceId}`,
      lastUpdatedAt: request.requestedAt,
      stepUpRequirement: 'not_required',
    });
  }

  cancelUnpairingRequestDecision(): void {
    this.unpairingRequestAction.set(null);
    this.unpairingRequestError.set(null);
  }

  async confirmApproveUnpairingRequest(
    action: CriticalActionConfirmationRequest,
  ): Promise<void> {
    const request = this.unpairingRequests().find(
      (candidate) => candidate.requestId === action.targetId,
    );
    if (!request) {
      this.unpairingRequestError.set(
        'A solicitacao nao esta mais disponivel na lista.',
      );
      return;
    }

    this.decidingUnpairingRequestId.set(request.requestId);
    this.unpairingRequestError.set(null);
    try {
      const response = await firstValueFrom(
        this.deviceApi.decideUnpairingRequest(request.requestId, 'approved'),
      );
      this.removeUnpairingRequest(response.request.requestId);
      if (response.unpairing) {
        this.devices.update((devices) =>
          devices.map((device) =>
            device.deviceId === response.unpairing?.deviceId
              ? { ...device, pairingStatus: response.unpairing.pairingStatus }
              : device,
          ),
        );
      }
      this.unpairingRequestAction.set(null);
      this.notifications.success(
        `${request.deviceName} foi despareado apos aprovacao administrativa.`,
      );
    } catch (error) {
      const detailMessage = apiErrorMessage(
        error,
        'Nao foi possivel aprovar o despareamento. Nenhuma alteracao local foi aplicada.',
      );
      this.unpairingRequestError.set(detailMessage);
      this.notifications.error(
        apiErrorMessage(
          error,
          `Nao foi possivel aprovar o despareamento de ${request.deviceName}.`,
        ),
      );
    } finally {
      this.decidingUnpairingRequestId.set(null);
    }
  }

  async rejectUnpairingRequest(request: DeviceUnpairingRequest): Promise<void> {
    this.decidingUnpairingRequestId.set(request.requestId);
    try {
      const response = await firstValueFrom(
        this.deviceApi.decideUnpairingRequest(request.requestId, 'rejected'),
      );
      this.removeUnpairingRequest(response.request.requestId);
      this.notifications.success(
        `Solicitacao de despareamento de ${request.deviceName} rejeitada.`,
      );
    } catch (error) {
      this.notifications.error(
        apiErrorMessage(
          error,
          `Nao foi possivel rejeitar a solicitacao de ${request.deviceName}.`,
        ),
      );
    } finally {
      this.decidingUnpairingRequestId.set(null);
    }
  }

  pendingUnpairingRequest(deviceId: string): DeviceUnpairingRequest | null {
    return (
      this.unpairingRequests().find(
        (request) =>
          request.deviceId === deviceId && request.status === 'pending',
      ) ?? null
    );
  }

  typeLabel(type: DeviceType): string {
    return (
      this.typeOptions.find((option) => option.value === type)?.label ?? type
    );
  }

  platformLabel(platform: DevicePlatform): string {
    return (
      this.platformOptions.find((option) => option.value === platform)?.label ??
      platform
    );
  }

  private removeUnpairingRequest(requestId: string): void {
    this.unpairingRequests.update((requests) =>
      requests.filter((request) => request.requestId !== requestId),
    );
  }

  private watchExpiration(session: PairingSession): void {
    this.clearExpirationTimer();
    const remainingMilliseconds = Date.parse(session.expiresAt) - Date.now();

    if (remainingMilliseconds <= 0) {
      this.pairingExpired.set(true);
      return;
    }

    this.pairingExpired.set(false);
    this.expirationTimer = setTimeout(
      () => this.pairingExpired.set(true),
      remainingMilliseconds,
    );
  }

  private clearExpirationTimer(): void {
    if (this.expirationTimer !== null) {
      clearTimeout(this.expirationTimer);
      this.expirationTimer = null;
    }
  }
}
