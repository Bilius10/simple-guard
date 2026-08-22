import { inject } from '@angular/core';
import type { HttpInterceptorFn } from '@angular/common/http';

import { OidcClientService } from './oidc-client.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const token = inject(OidcClientService).accessToken();

  if (!token || !request.url.startsWith('/api')) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }),
  );
};
