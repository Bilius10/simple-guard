import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'sg-root',
  templateUrl: './app.html',
  styleUrl: './app.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App {}
