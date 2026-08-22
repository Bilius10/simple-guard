import type { OnDestroy } from '@angular/core';
import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error';

export interface AppNotification {
  readonly id: number;
  readonly type: NotificationType;
  readonly message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private static readonly DEFAULT_DURATION_MS = 10_000;

  readonly notifications = signal<readonly AppNotification[]>([]);

  private nextId = 1;
  private readonly timers = new Map<number, ReturnType<typeof setTimeout>>();

  ngOnDestroy(): void {
    this.timers.forEach((timer) => clearTimeout(timer));
    this.timers.clear();
  }

  success(message: string): void {
    this.show('success', message);
  }

  error(message: string): void {
    this.show('error', message);
  }

  dismiss(id: number): void {
    const timer = this.timers.get(id);
    if (timer !== undefined) {
      clearTimeout(timer);
      this.timers.delete(id);
    }
    this.notifications.update((notifications) =>
      notifications.filter((notification) => notification.id !== id),
    );
  }

  private show(type: NotificationType, message: string): void {
    const id = this.nextId++;
    const notification: AppNotification = { id, type, message };

    this.notifications.update((notifications) => [
      ...notifications,
      notification,
    ]);
    this.timers.set(
      id,
      setTimeout(
        () => this.dismiss(id),
        NotificationService.DEFAULT_DURATION_MS,
      ),
    );
  }
}
