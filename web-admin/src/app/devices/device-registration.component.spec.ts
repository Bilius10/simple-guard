import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { NotificationService } from '../notifications/notification.service';
import { DeviceApiService } from './device-api.service';
import { DeviceRegistrationComponent } from './device-registration.component';
import { Device, DeviceUnpairingRequest, PairingSession } from './device.models';

describe('DeviceRegistrationComponentTests', () => {
  const androidDevice: Device = {
    deviceId: '00000000-0000-0000-0000-000000000201',
    name: 'Celular operacional',
    type: 'MOBILE',
    platform: 'ANDROID',
    pairingStatus: 'unpaired',
    createdAt: '2026-08-08T20:00:00Z',
  };

  const pairedAndroidDevice: Device = {
    ...androidDevice,
    pairingStatus: 'paired',
  };

  const pairedNotebookDevice: Device = {
    ...androidDevice,
    deviceId: '00000000-0000-0000-0000-000000000202',
    name: 'Notebook operacional',
    type: 'NOTEBOOK',
    platform: 'LINUX',
    pairingStatus: 'paired',
  };

  const deviceApiStub = {
    list: vi.fn(),
    create: vi.fn(),
    generatePairingSession: vi.fn(),
    unpair: vi.fn(),
    listUnpairingRequests: vi.fn(),
    decideUnpairingRequest: vi.fn(),
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
    deviceApiStub.listUnpairingRequests.mockReturnValue(of([]));
    deviceApiStub.unpair.mockReturnValue(of({
      deviceId: pairedAndroidDevice.deviceId,
      pairingStatus: 'unpaired',
      revokedKeyCount: 1,
      unpairedAt: '2026-08-11T12:00:00Z',
    }));
    deviceApiStub.decideUnpairingRequest.mockReturnValue(of({
      request: { ...unpairingRequestTests(), status: 'approved', decidedAt: '2026-08-11T12:00:00Z' },
      unpairing: {
        deviceId: pairedAndroidDevice.deviceId,
        pairingStatus: 'unpaired',
        revokedKeyCount: 1,
        unpairedAt: '2026-08-11T12:00:00Z',
      },
    }));

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
    expect(deviceApiStub.listUnpairingRequests).toHaveBeenCalledOnce();
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

  it('showsBackendMessageWhenDeviceRequestFailsTests', async () => {
    deviceApiStub.list.mockReturnValue(throwError(() => ({
      error: {
        erro_code: 'ACCESS_DENIED',
        mensagem: 'Acesso negado para este recurso.',
      },
    })));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.componentInstance.loadError()).toBe('Acesso negado para este recurso.');
    expect(notificationStub.error).toHaveBeenCalledWith('Acesso negado para este recurso.');
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
    const dialog = element.querySelector<HTMLElement>('.pairing-console[role="dialog"]');
    expect(dialog?.textContent).toContain('Aguardando agente');
    expect(dialog?.closest('.device-list')).toBeNull();
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

  it('showsBackendMessageWhenPairingGenerationFailsTests', async () => {
    deviceApiStub.list.mockReturnValue(of([androidDevice]));
    deviceApiStub.generatePairingSession.mockReturnValue(throwError(() => ({
      error: {
        erro_code: 'MAX_OPEN_PAIRING_SESSIONS_REACHED',
        mensagem: 'Limite de sessoes de pareamento abertas atingido.',
      },
    })));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    await fixture.componentInstance.generatePairingSession(androidDevice);

    expect(notificationStub.error).toHaveBeenCalledWith(
      'Limite de sessoes de pareamento abertas atingido.',
    );
  });

  it('closesActivePairingSessionTests', async () => {
    deviceApiStub.list.mockReturnValue(of([androidDevice]));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    await fixture.componentInstance.generatePairingSession(androidDevice);
    fixture.componentInstance.closePairingSession();
    fixture.detectChanges();

    expect(fixture.componentInstance.activePairing()).toBeNull();
    expect(fixture.componentInstance.pairingExpired()).toBe(false);
    expect((fixture.nativeElement as HTMLElement).querySelector('.waiting-state')).toBeNull();
  });

  it('expiresActivePairingSessionWhenTimerEndsTests', async () => {
    vi.useFakeTimers();
    deviceApiStub.list.mockReturnValue(of([androidDevice]));
    deviceApiStub.generatePairingSession.mockReturnValue(of(pairingSessionTests(1 / 60)));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    await fixture.componentInstance.generatePairingSession(androidDevice);
    expect(fixture.componentInstance.pairingExpired()).toBe(false);

    vi.advanceTimersByTime(1_001);
    fixture.detectChanges();

    expect(fixture.componentInstance.pairingExpired()).toBe(true);
    vi.useRealTimers();
  });

  it('returnsRawLabelsForUnknownDeviceValuesTests', () => {
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);

    expect(fixture.componentInstance.typeLabel('UNKNOWN' as never)).toBe('UNKNOWN');
    expect(fixture.componentInstance.platformLabel('UNKNOWN' as never)).toBe('UNKNOWN');
  });

  it('doesNotSubmitWhenRawRequiredValuesBecomeNullTests', async () => {
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.componentInstance.form.controls.name.setValue('Notebook operacional');
    fixture.componentInstance.form.controls.type.clearValidators();
    fixture.componentInstance.form.controls.type.setValue(null);
    fixture.componentInstance.form.controls.type.updateValueAndValidity();
    fixture.componentInstance.form.controls.platform.setValue('LINUX');

    await fixture.componentInstance.submit();

    expect(deviceApiStub.create).not.toHaveBeenCalled();
  });

  it('opensAndCancelsDangerousUnpairingConfirmationTests', async () => {
    deviceApiStub.list.mockReturnValue(of([pairedAndroidDevice]));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    clickTests(fixture.nativeElement, '.unpair-action');
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('[role="dialog"]')?.textContent).toContain('Desparear dispositivo');
    expect(element.textContent).toContain('As chaves ativas serao revogadas');

    clickTests(element, '.critical-dialog .secondary-action');
    fixture.detectChanges();
    expect(element.querySelector('[role="dialog"]')).toBeNull();
    expect(deviceApiStub.unpair).not.toHaveBeenCalled();
  });

  it('unpairsDeviceAndUpdatesListAfterConfirmationTests', async () => {
    deviceApiStub.list.mockReturnValue(of([pairedAndroidDevice, pairedNotebookDevice]));
    deviceApiStub.listUnpairingRequests.mockReturnValue(of([
      unpairingRequestTests(),
      {
        ...unpairingRequestTests(),
        requestId: 'request-002',
        deviceId: pairedNotebookDevice.deviceId,
        deviceName: pairedNotebookDevice.name,
      },
    ]));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    fixture.componentInstance.openUnpairConfirmation(pairedAndroidDevice);
    const action = fixture.componentInstance.unpairAction();
    expect(action).not.toBeNull();
    await fixture.componentInstance.confirmUnpair(action!);
    fixture.detectChanges();

    expect(deviceApiStub.unpair).toHaveBeenCalledWith(pairedAndroidDevice.deviceId);
    expect(fixture.componentInstance.devices()[0].pairingStatus).toBe('unpaired');
    expect(fixture.componentInstance.devices()[1]).toEqual(pairedNotebookDevice);
    expect(fixture.componentInstance.unpairingRequests()).toEqual([{
      ...unpairingRequestTests(),
      requestId: 'request-002',
      deviceId: pairedNotebookDevice.deviceId,
      deviceName: pairedNotebookDevice.name,
    }]);
    expect(fixture.componentInstance.unpairAction()).toBeNull();
    expect(notificationStub.success).toHaveBeenCalledWith(
      'Celular operacional foi despareado e suas chaves foram revogadas.',
    );
  });

  it('keepsDangerousConfirmationOpenWhenUnpairingFailsTests', async () => {
    deviceApiStub.list.mockReturnValue(of([pairedAndroidDevice]));
    deviceApiStub.unpair.mockReturnValue(throwError(() => new Error('offline')));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    fixture.componentInstance.openUnpairConfirmation(pairedAndroidDevice);
    await fixture.componentInstance.confirmUnpair(fixture.componentInstance.unpairAction()!);
    fixture.detectChanges();

    expect(fixture.componentInstance.devices()[0].pairingStatus).toBe('paired');
    expect(fixture.componentInstance.unpairError()).toContain('Nenhuma alteracao local');
    expect((fixture.nativeElement as HTMLElement).querySelector('[role="alert"]')).not.toBeNull();
    expect(notificationStub.error).toHaveBeenCalledWith('Nao foi possivel desparear Celular operacional.');
  });

  it('rejectsUnpairingWhenDeviceDisappearedFromCurrentListTests', async () => {
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    const missingAction = {
      actionType: 'UNPAIR_DEVICE' as const,
      targetId: 'missing-device',
      targetName: 'Ausente',
      consequence: 'Revogar',
      connectivityState: 'paired',
      lastKnownLocation: 'indisponivel',
      lastUpdatedAt: 'indisponivel',
      stepUpRequirement: 'not_required' as const,
    };

    await fixture.componentInstance.confirmUnpair(missingAction);

    expect(fixture.componentInstance.unpairError()).toContain('nao esta mais disponivel');
    expect(deviceApiStub.unpair).not.toHaveBeenCalled();
  });

  it('showsPendingUnpairingRequestActionsTests', async () => {
    deviceApiStub.list.mockReturnValue(of([pairedAndroidDevice]));
    deviceApiStub.listUnpairingRequests.mockReturnValue(of([unpairingRequestTests()]));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.querySelector('.unpairing-request-status')?.textContent).toContain('Despareamento solicitado');
    expect(element.querySelector('.approve-unpair-request-action')).not.toBeNull();
    expect(element.querySelector('.reject-unpair-request-action')).not.toBeNull();
    expect(element.querySelector('.unpair-action')).toBeNull();
  });

  it('approvesPendingUnpairingRequestAndUpdatesDeviceTests', async () => {
    deviceApiStub.list.mockReturnValue(of([pairedAndroidDevice, pairedNotebookDevice]));
    deviceApiStub.listUnpairingRequests.mockReturnValue(of([unpairingRequestTests()]));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    fixture.componentInstance.openApproveUnpairingRequestConfirmation(unpairingRequestTests());
    const action = fixture.componentInstance.unpairingRequestAction();
    expect(action).not.toBeNull();
    await fixture.componentInstance.confirmApproveUnpairingRequest(action!);
    fixture.detectChanges();

    expect(deviceApiStub.decideUnpairingRequest).toHaveBeenCalledWith('request-001', 'approved');
    expect(fixture.componentInstance.devices()[0].pairingStatus).toBe('unpaired');
    expect(fixture.componentInstance.devices()[1]).toEqual(pairedNotebookDevice);
    expect(fixture.componentInstance.unpairingRequests()).toEqual([]);
    expect(fixture.componentInstance.unpairingRequestAction()).toBeNull();
    expect(notificationStub.success).toHaveBeenCalledWith(
      'Celular operacional foi despareado apos aprovacao administrativa.',
    );
  });

  it('cancelsPendingUnpairingRequestDecisionTests', () => {
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);

    fixture.componentInstance.openApproveUnpairingRequestConfirmation(unpairingRequestTests());
    fixture.componentInstance.unpairingRequestError.set('Falha anterior');

    fixture.componentInstance.cancelUnpairingRequestDecision();

    expect(fixture.componentInstance.unpairingRequestAction()).toBeNull();
    expect(fixture.componentInstance.unpairingRequestError()).toBeNull();
  });

  it('removesApprovedRequestWithoutDeviceUpdateWhenNoUnpairingResponseTests', async () => {
    deviceApiStub.list.mockReturnValue(of([pairedAndroidDevice]));
    deviceApiStub.listUnpairingRequests.mockReturnValue(of([unpairingRequestTests()]));
    deviceApiStub.decideUnpairingRequest.mockReturnValue(of({
      request: { ...unpairingRequestTests(), status: 'approved', decidedAt: '2026-08-11T12:00:00Z' },
      unpairing: null,
    }));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    fixture.componentInstance.openApproveUnpairingRequestConfirmation(unpairingRequestTests());
    await fixture.componentInstance.confirmApproveUnpairingRequest(fixture.componentInstance.unpairingRequestAction()!);

    expect(fixture.componentInstance.devices()[0].pairingStatus).toBe('paired');
    expect(fixture.componentInstance.unpairingRequests()).toEqual([]);
  });

  it('keepsApprovalConfirmationOpenWhenDecisionFailsTests', async () => {
    deviceApiStub.list.mockReturnValue(of([pairedAndroidDevice]));
    deviceApiStub.listUnpairingRequests.mockReturnValue(of([unpairingRequestTests()]));
    deviceApiStub.decideUnpairingRequest.mockReturnValue(throwError(() => new Error('offline')));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    fixture.componentInstance.openApproveUnpairingRequestConfirmation(unpairingRequestTests());
    await fixture.componentInstance.confirmApproveUnpairingRequest(fixture.componentInstance.unpairingRequestAction()!);
    fixture.detectChanges();

    expect(fixture.componentInstance.devices()[0].pairingStatus).toBe('paired');
    expect(fixture.componentInstance.unpairingRequestError()).toContain('Nenhuma alteracao local');
    expect(fixture.componentInstance.unpairingRequests()).toHaveLength(1);
    expect(notificationStub.error).toHaveBeenCalledWith(
      'Nao foi possivel aprovar o despareamento de Celular operacional.',
    );
  });

  it('rejectsPendingUnpairingRequestTests', async () => {
    deviceApiStub.list.mockReturnValue(of([pairedAndroidDevice]));
    deviceApiStub.listUnpairingRequests.mockReturnValue(of([unpairingRequestTests()]));
    deviceApiStub.decideUnpairingRequest.mockReturnValue(of({
      request: { ...unpairingRequestTests(), status: 'rejected', decidedAt: '2026-08-11T12:00:00Z' },
      unpairing: null,
    }));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    fixture.detectChanges();
    await fixture.whenStable();

    await fixture.componentInstance.rejectUnpairingRequest(unpairingRequestTests());
    fixture.detectChanges();

    expect(deviceApiStub.decideUnpairingRequest).toHaveBeenCalledWith('request-001', 'rejected');
    expect(fixture.componentInstance.devices()[0].pairingStatus).toBe('paired');
    expect(fixture.componentInstance.unpairingRequests()).toEqual([]);
    expect(notificationStub.success).toHaveBeenCalledWith(
      'Solicitacao de despareamento de Celular operacional rejeitada.',
    );
  });

  it('showsNotificationWhenRejectingUnpairingRequestFailsTests', async () => {
    deviceApiStub.decideUnpairingRequest.mockReturnValue(throwError(() => new Error('offline')));
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);

    await fixture.componentInstance.rejectUnpairingRequest(unpairingRequestTests());

    expect(notificationStub.error).toHaveBeenCalledWith(
      'Nao foi possivel rejeitar a solicitacao de Celular operacional.',
    );
  });

  it('rejectsApprovalWhenRequestDisappearedFromCurrentListTests', async () => {
    const fixture = TestBed.createComponent(DeviceRegistrationComponent);
    const missingAction = {
      actionType: 'UNPAIR_DEVICE' as const,
      targetId: 'missing-request',
      targetName: 'Ausente',
      consequence: 'Revogar',
      connectivityState: 'paired',
      lastKnownLocation: 'indisponivel',
      lastUpdatedAt: 'indisponivel',
      stepUpRequirement: 'not_required' as const,
    };

    await fixture.componentInstance.confirmApproveUnpairingRequest(missingAction);

    expect(fixture.componentInstance.unpairingRequestError()).toContain('nao esta mais disponivel');
    expect(deviceApiStub.decideUnpairingRequest).not.toHaveBeenCalled();
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

  function unpairingRequestTests(): DeviceUnpairingRequest {
    return {
      requestId: 'request-001',
      deviceId: pairedAndroidDevice.deviceId,
      deviceName: 'Celular operacional',
      agentInstanceId: 'android-agent-001',
      status: 'pending',
      requestedAt: '2026-08-11T12:00:00Z',
      decidedAt: null,
    };
  }

  function clickTests(root: Element, selector: string): void {
    const button = root.querySelector<HTMLButtonElement>(selector);
    expect(button).not.toBeNull();
    button?.click();
  }
});
