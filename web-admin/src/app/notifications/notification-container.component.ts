import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { NotificationService } from './notification.service';

@Component({
  selector: 'sg-notification-container',
  templateUrl: './notification-container.component.html',
  styleUrl: './notification-container.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NotificationContainerComponent {

  readonly notifications = inject(NotificationService);
}
