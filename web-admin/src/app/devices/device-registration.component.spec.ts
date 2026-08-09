import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { DeviceApiService } from './device-api.service';
import { DeviceRegistrationComponent } from './device-registration.component';
import { Device } from './device.models';

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
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    deviceApiStub.list.mockReturnValue(of([]));
    deviceApiStub.create.mockReturnValue(of(androidDevice));

    await TestBed.configureTestingModule({
      imports: [DeviceRegistrationComponent],
      providers: [
        { provide: DeviceApiService, useValue: deviceApiStub },
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
    expect(element.querySelector('.success-feedback')?.textContent).toContain('cadastrado e pendente');
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
    expect(element.querySelector('.error-feedback')?.textContent).toContain('Nao foi possivel cadastrar');
  });
});
