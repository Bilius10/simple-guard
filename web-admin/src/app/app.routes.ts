import type { Routes } from '@angular/router';

import { AuthCallbackPage } from './auth/auth-callback-page';

export const routes: Routes = [
  {
    path: 'auth/callback',
    component: AuthCallbackPage,
  },
];
