import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { NotificationService } from '../notifications/notification.service';
import { DeviceApiService } from './device-api.service';
import { DeviceRegistrationComponent } from './device-registration.component';
import { Device, PairingSession } from './device.models';

describe('DeviceRegistrationComponentTests', () => {
  const androidDevice: Device = {
    deviceId: '00000000-0000-0000-0000-000000000201',
    name: 'Celular operacional',
    type: 'MOBILE',
    platform: 'ANDROID',
    pairingStatus: 'unpaired',
    createdAt: '2026-08-08T20:00:00Z',
  };

  const deviceApiStub = {
    list: vi.fn(),
    create: vi.fn(),
    generatePairingSession: vi.fn(),
  };
  const notificationStub = {
    success: vi.fn(),
    error: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    deviceApiStub.list.mockReturnValue(of([]));
    deviceApiStub.create.mockReturnValue(of(androidDevice));
    deviceApiStub.generatePairingSession.mockReturnValue(of(pairingSessionTests()));

    await TestBed.configureTestingModule({
      imports: [DeviceRegistrationComponent],
      providers: [
        { provide: DeviceApiService, useValue: deviceApiStub },
        { provide: NotificationService, useValue: notificationStub },
      ],
    }).compileComponents();
  });

  it('showsEmptyStateWhenNoDevicesExistTests', async () => {
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.empty-state')?.textContent).toContain('Nenhum dispositivo cadastrado');
    expect(deviceApiStub.list).toHaveBeenCalledOnce();
  });

  it('validatesRequiredFormFieldsTests', async () => {
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    await fixture.componentInstance.submit();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelectorAll('.field-error')).toHaveLength(3);
    expect(deviceApiStub.create).not.toHaveBeenCalled();
  });

  it('registersAndListsDeviceAsPendingPairingTests', async () => {
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    fixture.componentInstance.form.setValue({
      name: '  Celular operacional  ',
      type: 'MOBILE',
      platform: 'ANDROID',
    });
    await fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(deviceApiStub.create).toHaveBeenCalledWith({
      name: 'Celular operacional',
      type: 'MOBILE',
      platform: 'ANDROID',
    });
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.device-name')?.textContent).toContain('Celular operacional');
    expect(element.querySelector('.pairing-status')?.textContent).toContain('Pendente de pareamento');
    expect(element.querySelector('.success-feedback')).toBeNull();
    expect(notificationStub.success).toHaveBeenCalledWith('Celular operacional cadastrado e pendente de pareamento.');
  });

  it('showsLoadFailureAndRetryActionTests', async () => {
    deviceApiStub.list.mockReturnValue(throwError(() => new Error('offline')));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.error-state')?.textContent).toContain('Nao foi possivel carregar');
    expect(element.querySelector('.retry-action')).not.toBeNull();
    expect(notificationStub.error).toHaveBeenCalledWith('Nao foi possivel carregar os dispositivos.');
  });

  it('keepsFormDataWhenRegistrationFailsTests', async () => {
    deviceApiStub.create.mockReturnValue(throwError(() => new Error('offline')));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    fixture.componentInstance.form.setValue({
      name: 'Notebook de campo',
      type: 'NOTEBOOK',
      platform: 'LINUX',
    });
    await fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.componentInstance.form.controls.name.value).toBe('Notebook de campo');
    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.error-feedback')).toBeNull();
    expect(notificationStub.error).toHaveBeenCalledWith(
      'Nao foi possivel cadastrar o dispositivo. Revise os dados e tente novamente.',
    );
  });

  it('showsWaitingPairingSessionTests', async () => {
    deviceApiStub.list.mockReturnValue(of([androidDevice]));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    await fixture.componentInstance.generatePairingSession(androidDevice);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(deviceApiStub.generatePairingSession).toHaveBeenCalledWith(androidDevice.deviceId);
    expect(element.querySelector('.waiting-state')?.textContent).toContain('Aguardando agente');
    expect(element.querySelector('.pairing-code')?.textContent).toContain('ABCD-2345');
    expect(element.querySelector('.pairing-expiration')?.textContent).toContain('Expira em');
  });

  it('showsExpiredPairingSessionTests', async () => {
    deviceApiStub.list.mockReturnValue(of([androidDevice]));
    deviceApiStub.generatePairingSession.mockReturnValue(of(pairingSessionTests(-1)));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    await fixture.componentInstance.generatePairingSession(androidDevice);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.expired-state')?.textContent).toContain('Codigo expirado');
    expect(element.querySelector('.regenerate-action')?.textContent).toContain('Gerar novo codigo');
    expect(element.querySelector('.pairing-code')).toBeNull();
  });

  it('showsPairingGenerationFailureTests', async () => {
    deviceApiStub.list.mockReturnValue(of([androidDevice]));
    deviceApiStub.generatePairingSession.mockReturnValue(throwError(() => new Error('offline')));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    await fixture.componentInstance.generatePairingSession(androidDevice);
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.pairing-error')).toBeNull();
    expect(notificationStub.error).toHaveBeenCalledWith(
      'Nao foi possivel gerar o codigo de pareamento para Celular operacional.',
    );
  });

  function pairingSessionTests(offsetMinutes = 5): PairingSession {
    return {
      pairingSessionId: '00000000-0000-0000-0000-000000000301',
      deviceId: androidDevice.deviceId,
      pairingCode: 'ABCD-2345',
      status: 'waiting',
      expiresAt: new Date(Date.now() + offsetMinutes * 60_000).toISOString(),
      createdAt: new Date().toISOString(),
    };
  }
});
