import type {
  AfterViewInit,
  ElementRef,
  OnDestroy,
  OnInit,
} from '@angular/core';
import {
  ChangeDetectionStrategy,
  Component,
  NgZone,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import * as L from 'leaflet';
import { firstValueFrom } from 'rxjs';

import { OidcClientService } from './auth/oidc-client.service';
import { CriticalActionDialogComponent } from './critical-action/critical-action-dialog.component';
import type {
  CriticalActionConfirmationEvent,
  CriticalActionConfirmationRequest,
} from './critical-action/critical-action.models';
import { DeviceApiService } from './devices/device-api.service';
import { DeviceRegistrationComponent } from './devices/device-registration.component';
import type { Device, LatestDeviceTelemetry } from './devices/device.models';
import { NotificationContainerComponent } from './notifications/notification-container.component';
import { NotificationService } from './notifications/notification.service';
import type { AdministratorSession } from './session/session-api.service';
import { SessionApiService } from './session/session-api.service';
import { apiErrorMessage } from './shared/api-error-message';

const STALE_TELEMETRY_THRESHOLD_MS = 15 * 60 * 1000;
const UNAVAILABLE = 'indisponivel';
const DEFAULT_MAP_CENTER = L.latLng(-23.55052, -46.633308);
const DEFAULT_MAP_ZOOM = 12;
const FOCUSED_MAP_ZOOM = 16;

interface DeviceMapMarker {
  readonly device: Device;
  readonly telemetry: LatestDeviceTelemetry & {
    readonly latitude: number;
    readonly longitude: number;
  };
}

@Component({
  selector: 'sg-root',
  imports: [
    CriticalActionDialogComponent,
    DeviceRegistrationComponent,
    NotificationContainerComponent,
  ],
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('mapContainer')
  private readonly mapContainer?: ElementRef<HTMLElement>;

  readonly auth = inject(OidcClientService);
  readonly session = signal<AdministratorSession | null>(null);
  readonly sessionError = signal<string | null>(null);
  readonly devices = signal<Device[]>([]);
  readonly devicesError = signal<string | null>(null);
  readonly selectedDeviceId = signal<string | null>(null);
  readonly telemetryByDevice = signal<Record<string, LatestDeviceTelemetry>>(
    {},
  );
  readonly telemetryLoading = signal(false);
  readonly telemetryError = signal<string | null>(null);
  readonly pairedDevices = computed(() =>
    this.devices().filter((device) => device.pairingStatus === 'paired'),
  );
  readonly latestTelemetry = computed(() => {
    const selectedId = this.selectedDeviceId();
    return selectedId ? (this.telemetryByDevice()[selectedId] ?? null) : null;
  });
  readonly deviceMarkers = computed(() =>
    this.pairedDevices()
      .map((device) => this.markerForDevice(device))
      .filter((marker): marker is DeviceMapMarker => marker !== null),
  );
  readonly criticalAction = signal<CriticalActionConfirmationRequest | null>(
    null,
  );
  readonly criticalActionError = signal<string | null>(null);
  readonly criticalActionEvent = signal<CriticalActionConfirmationEvent | null>(
    null,
  );
  readonly operatorPanelExpanded = signal(true);

  private readonly deviceApi = inject(DeviceApiService);
  private readonly notifications = inject(NotificationService);
  private readonly sessionApi = inject(SessionApiService);
  private readonly zone = inject(NgZone);
  private readonly leafletMarkers = new Map<string, L.Marker>();
  private map: L.Map | null = null;
  private mapResizeObserver: ResizeObserver | null = null;
  private criticalActionFailureSimulation = false;

  async ngOnInit(): Promise<void> {
    await this.auth.initialize();
    if (this.auth.state().status === 'authenticated') {
      await this.loadSession();
      if (this.session()) {
        await this.loadDevices();
      }
    }
  }

  ngAfterViewInit(): void {
    this.initializeMap();
    this.syncMapMarkers();
  }

  ngOnDestroy(): void {
    this.mapResizeObserver?.disconnect();
    this.mapResizeObserver = null;
    this.map?.remove();
    this.map = null;
    this.leafletMarkers.clear();
  }

  async login(): Promise<void> {
    await this.auth.login();
  }

  async logout(): Promise<void> {
    await this.auth.logout();
  }

  toggleOperatorPanel(): void {
    this.operatorPanelExpanded.update((expanded) => !expanded);
  }

  async selectDevice(deviceId: string): Promise<void> {
    const device = this.devices().find(
      (candidate) => candidate.deviceId === deviceId,
    );
    if (!device || device.pairingStatus !== 'paired') {
      return;
    }

    this.selectedDeviceId.set(deviceId);
    if (!this.telemetryByDevice()[deviceId]) {
      await this.loadLatestTelemetry(deviceId);
    }
    this.syncMapMarkers(true);
  }

  focusSelectedDevice(): void {
    this.syncMapMarkers(true);
  }

  clearSelectedDevice(): void {
    this.selectedDeviceId.set(null);
    this.syncMapMarkers();
  }

  hasTelemetryLocation(
    telemetry: LatestDeviceTelemetry | null | undefined,
  ): telemetry is LatestDeviceTelemetry & {
    readonly latitude: number;
    readonly longitude: number;
  } {
    return (
      telemetry?.latitude !== null &&
      telemetry?.latitude !== undefined &&
      telemetry.longitude !== null &&
      telemetry.longitude !== undefined
    );
  }

  selectedDevice(): Device | null {
    const selectedId = this.selectedDeviceId();
    return (
      this.pairedDevices().find((device) => device.deviceId === selectedId) ??
      null
    );
  }

  formatUnavailable<T>(value: T | null | undefined): T | string {
    return value ?? UNAVAILABLE;
  }

  formatBattery(telemetry: LatestDeviceTelemetry | null): string {
    if (
      telemetry?.batteryLevelPercentage === null ||
      telemetry?.batteryLevelPercentage === undefined
    ) {
      return UNAVAILABLE;
    }
    const charging = telemetry.batteryCharging ? ' carregando' : '';
    return `${telemetry.batteryLevelPercentage}%${charging}`;
  }

  isBatteryLow(telemetry: LatestDeviceTelemetry | null): boolean {
    return (
      telemetry?.batteryLevelPercentage !== null &&
      telemetry?.batteryLevelPercentage !== undefined &&
      telemetry.batteryLevelPercentage <= 20
    );
  }

  formatSignal(telemetry: LatestDeviceTelemetry | null): string {
    if (
      telemetry?.signalStrengthDbm === null ||
      telemetry?.signalStrengthDbm === undefined
    ) {
      return UNAVAILABLE;
    }
    return `${telemetry.signalStrengthDbm} dBm`;
  }

  isSignalWeak(telemetry: LatestDeviceTelemetry | null): boolean {
    return (
      telemetry?.signalStrengthDbm !== null &&
      telemetry?.signalStrengthDbm !== undefined &&
      telemetry.signalStrengthDbm <= -100
    );
  }

  formatLastUpdated(telemetry: LatestDeviceTelemetry | null): string {
    if (!telemetry?.lastUpdatedAt) {
      return UNAVAILABLE;
    }
    const parsed = Date.parse(telemetry.lastUpdatedAt);
    if (!Number.isFinite(parsed)) {
      return UNAVAILABLE;
    }
    const formatted = new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'medium',
    }).format(new Date(parsed));
    return this.isTelemetryStale(telemetry)
      ? `${formatted} (antigo)`
      : formatted;
  }

  isTelemetryStale(telemetry: LatestDeviceTelemetry | null): boolean {
    if (!telemetry?.lastUpdatedAt) {
      return false;
    }
    const parsed = Date.parse(telemetry.lastUpdatedAt);
    return (
      Number.isFinite(parsed) &&
      Date.now() - parsed > STALE_TELEMETRY_THRESHOLD_MS
    );
  }

  deviceOperationalState(telemetry: LatestDeviceTelemetry | null): string {
    if (!telemetry) {
      return UNAVAILABLE;
    }
    return this.isTelemetryStale(telemetry) ||
      this.isBatteryLow(telemetry) ||
      this.isSignalWeak(telemetry)
      ? 'Atencao'
      : 'Monitorando';
  }

  formatCoordinates(telemetry: LatestDeviceTelemetry | null): string {
    if (!this.hasTelemetryLocation(telemetry)) {
      return UNAVAILABLE;
    }
    return `${this.formatNumber(telemetry.latitude)}, ${this.formatNumber(telemetry.longitude)}`;
  }

  formatAccuracy(telemetry: LatestDeviceTelemetry | null): string {
    if (
      telemetry?.accuracyMeters === null ||
      telemetry?.accuracyMeters === undefined
    ) {
      return UNAVAILABLE;
    }
    return `${this.formatNumber(telemetry.accuracyMeters)} m`;
  }

  deviceIconKind(device: Device): 'phone' | 'desktop' | 'generic' {
    if (device.type === 'MOBILE') {
      return 'phone';
    }
    if (device.type === 'DESKTOP' || device.type === 'NOTEBOOK') {
      return 'desktop';
    }
    return 'generic';
  }

  openCriticalActionSimulation(): void {
    this.criticalActionFailureSimulation = false;
    this.criticalActionError.set(null);
    this.criticalAction.set(this.buildCriticalActionRequest());
  }

  openCriticalActionFailureSimulation(): void {
    this.criticalActionFailureSimulation = true;
    this.criticalActionError.set(null);
    this.criticalAction.set(this.buildCriticalActionRequest());
  }

  cancelCriticalAction(): void {
    this.criticalAction.set(null);
    this.criticalActionError.set(null);
  }

  confirmCriticalAction(action: CriticalActionConfirmationRequest): void {
    if (this.criticalActionFailureSimulation) {
      this.notifications.error(
        'Falha ao emitir evento de confirmacao critica.',
      );
      return;
    }

    this.criticalActionEvent.set({
      actionType: action.actionType,
      targetId: action.targetId,
      stepUpRequired: action.stepUpRequirement === 'required',
    });
    this.criticalAction.set(null);
    this.criticalActionError.set(null);
  }

  private initializeMap(): void {
    const element = this.mapContainer?.nativeElement;
    if (!element || this.map) {
      return;
    }

    this.map = L.map(element, {
      attributionControl: true,
      zoomControl: true,
    }).setView(DEFAULT_MAP_CENTER, DEFAULT_MAP_ZOOM);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; Contribuidores do OpenStreetMap',
      maxZoom: 19,
    }).addTo(this.map);

    if (typeof ResizeObserver !== 'undefined') {
      this.mapResizeObserver = new ResizeObserver(() =>
        this.invalidateMapSize(),
      );
      this.mapResizeObserver.observe(element);
    }
    setTimeout(() => this.invalidateMapSize());
  }

  private async loadSession(): Promise<void> {
    try {
      this.session.set(await firstValueFrom(this.sessionApi.me()));
      this.sessionError.set(null);
    } catch (error) {
      const message = apiErrorMessage(
        error,
        'Nao foi possivel validar a sessao na API.',
      );
      this.session.set(null);
      this.sessionError.set(message);
      this.notifications.error(message);
    }
  }

  private async loadDevices(): Promise<void> {
    try {
      const devices = await firstValueFrom(this.deviceApi.list());
      this.devices.set(devices);
      this.devicesError.set(null);
      this.selectedDeviceId.set(null);
      this.telemetryByDevice.set({});
      await this.loadPairedDeviceTelemetry(devices);
      this.syncMapMarkers();
    } catch (error) {
      const message = apiErrorMessage(
        error,
        'Nao foi possivel carregar dispositivos.',
      );
      this.devices.set([]);
      this.selectedDeviceId.set(null);
      this.telemetryByDevice.set({});
      this.devicesError.set(message);
      this.notifications.error(message);
      this.syncMapMarkers();
    }
  }

  private async loadPairedDeviceTelemetry(
    devices: readonly Device[],
  ): Promise<void> {
    const pairedDeviceIds = new Set(
      devices
        .filter((device) => device.pairingStatus === 'paired')
        .map((device) => device.deviceId),
    );
    if (pairedDeviceIds.size === 0) {
      return;
    }

    const telemetryList = await firstValueFrom(
      this.deviceApi.latestTelemetryList(),
    );
    const telemetryByDevice = telemetryList.reduce<
      Record<string, LatestDeviceTelemetry>
    >((current, telemetry) => {
      if (pairedDeviceIds.has(telemetry.deviceId)) {
        current[telemetry.deviceId] = telemetry;
      }
      return current;
    }, {});
    this.telemetryByDevice.set(telemetryByDevice);
  }

  private async loadLatestTelemetry(deviceId: string): Promise<void> {
    this.telemetryLoading.set(true);
    try {
      const telemetry = await firstValueFrom(
        this.deviceApi.latestTelemetry(deviceId),
      );
      this.telemetryByDevice.update((current) => ({
        ...current,
        [deviceId]: telemetry,
      }));
      this.telemetryError.set(null);
      this.syncMapMarkers();
    } catch (error) {
      const message = apiErrorMessage(
        error,
        'Nao foi possivel carregar a telemetria do dispositivo.',
      );
      this.telemetryError.set(message);
      this.notifications.error(message);
    } finally {
      this.telemetryLoading.set(false);
    }
  }

  private invalidateMapSize(): void {
    if (!this.map) {
      return;
    }

    this.map.invalidateSize();
    this.syncMapMarkers();
  }

  private syncMapMarkers(focusSelected = false): void {
    if (!this.map) {
      return;
    }

    const activeDeviceIds = new Set<string>();
    const markerBounds = L.latLngBounds([]);

    for (const markerModel of this.deviceMarkers()) {
      const latLng = L.latLng(
        markerModel.telemetry.latitude,
        markerModel.telemetry.longitude,
      );
      const deviceId = markerModel.device.deviceId;
      activeDeviceIds.add(deviceId);
      markerBounds.extend(latLng);

      const marker = this.leafletMarkers.get(deviceId);
      if (marker) {
        marker.setLatLng(latLng);
        marker.setIcon(this.deviceMapIcon(markerModel));
        marker.setTooltipContent(markerModel.device.name);
      } else {
        const nextMarker = L.marker(latLng, {
          icon: this.deviceMapIcon(markerModel),
          keyboard: true,
          title: markerModel.device.name,
        })
          .bindTooltip(markerModel.device.name, {
            className: 'simpleguard-device-tooltip',
            direction: 'top',
            offset: [0, -18],
          })
          .on('click', () => {
            this.zone.run(() => {
              void this.selectDevice(deviceId);
            });
          })
          .addTo(this.map);
        this.leafletMarkers.set(deviceId, nextMarker);
      }
    }

    for (const [deviceId, marker] of this.leafletMarkers.entries()) {
      if (!activeDeviceIds.has(deviceId)) {
        marker.remove();
        this.leafletMarkers.delete(deviceId);
      }
    }

    const selectedTelemetry = this.latestTelemetry();
    if (focusSelected && this.hasTelemetryLocation(selectedTelemetry)) {
      this.map.setView(
        [selectedTelemetry.latitude, selectedTelemetry.longitude],
        FOCUSED_MAP_ZOOM,
        {
          animate: true,
        },
      );
      return;
    }

    if (markerBounds.isValid() && !this.selectedDeviceId()) {
      this.map.fitBounds(markerBounds.pad(0.25), {
        animate: false,
        maxZoom: FOCUSED_MAP_ZOOM,
      });
    }
  }

  private markerForDevice(device: Device): DeviceMapMarker | null {
    const telemetry = this.telemetryByDevice()[device.deviceId];
    if (!this.hasTelemetryLocation(telemetry)) {
      return null;
    }
    return { device, telemetry };
  }

  private deviceMapIcon(marker: DeviceMapMarker): L.DivIcon {
    const selected = marker.device.deviceId === this.selectedDeviceId();
    const attention =
      this.deviceOperationalState(marker.telemetry) === 'Atencao';
    const iconKind = this.deviceIconKind(marker.device);
    return L.divIcon({
      className: [
        'simpleguard-device-marker',
        selected ? 'simpleguard-device-marker-selected' : '',
        attention ? 'simpleguard-device-marker-attention' : '',
      ]
        .filter(Boolean)
        .join(' '),
      html: `<span class="device-marker-icon device-marker-icon-${iconKind}" aria-hidden="true"></span>`,
      iconAnchor: [17, 17],
      iconSize: [34, 34],
    });
  }

  private buildCriticalActionRequest(): CriticalActionConfirmationRequest {
    const telemetry = this.latestTelemetry();
    const device = this.selectedDevice();
    return {
      actionType: 'TRIGGER_ALARM',
      targetId: device?.deviceId ?? 'device-demo-001',
      targetName: device?.name ?? 'Notebook operacional demo',
      consequence:
        'O comando podera acionar um alarme no dispositivo alvo quando comandos reais forem implementados.',
      connectivityState: this.isTelemetryStale(telemetry)
        ? 'offline'
        : 'online',
      lastKnownLocation: this.formatCoordinates(telemetry),
      lastUpdatedAt: this.formatLastUpdated(telemetry),
      stepUpRequirement: 'required',
    };
  }

  private formatNumber(value: number): string {
    return new Intl.NumberFormat('pt-BR', {
      maximumFractionDigits: 6,
    }).format(value);
  }
}
