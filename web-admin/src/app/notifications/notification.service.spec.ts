import { TestBed } from '@angular/core/testing';
import { vi } from 'vitest';

import { NotificationService } from './notification.service';

describe('NotificationServiceTests', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({});
  });

  afterEach(() => {
    TestBed.inject(NotificationService).ngOnDestroy();
    vi.useRealTimers();
  });

  it('showsAndAutoDismissesNotificationAfterTenSecondsTests', () => {
    const notifications = TestBed.inject(NotificationService);

    notifications.success('Dispositivo cadastrado.');

    expect(notifications.notifications()).toEqual([
      {
        id: 1,
        type: 'success',
        message: 'Dispositivo cadastrado.',
      },
    ]);

    vi.advanceTimersByTime(9_999);
    expect(notifications.notifications()).toHaveLength(1);

    vi.advanceTimersByTime(1);
    expect(notifications.notifications()).toHaveLength(0);
  });

  it('dismissesNotificationManuallyTests', () => {
    const notifications = TestBed.inject(NotificationService);

    notifications.error('Falha operacional.');
    notifications.dismiss(1);

    expect(notifications.notifications()).toHaveLength(0);
  });

  it('ignoresDismissForUnknownNotificationTests', () => {
    const notifications = TestBed.inject(NotificationService);

    notifications.dismiss(999);

    expect(notifications.notifications()).toEqual([]);
  });

  it('clearsPendingTimersOnDestroyTests', () => {
    const notifications = TestBed.inject(NotificationService);

    notifications.success('Operacao agendada.');
    notifications.ngOnDestroy();
    vi.advanceTimersByTime(10_000);

    expect(notifications.notifications()).toHaveLength(1);
  });
});
