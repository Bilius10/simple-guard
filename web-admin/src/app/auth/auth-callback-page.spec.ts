import { TestBed } from '@angular/core/testing';

import { AuthCallbackPage } from './auth-callback-page';

describe('AuthCallbackPageTests', () => {
  it('createsEmptyCallbackPageTests', () => {
    TestBed.configureTestingModule({
      imports: [AuthCallbackPage],
    });

    const fixture = TestBed.createComponent(AuthCallbackPage);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
    expect((fixture.nativeElement as HTMLElement).textContent?.trim()).toBe('');
  });
});
