import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (auth.isLoggedIn() && auth.isAdmin()) return true;

  // Si está logueado pero no es admin, lo manda al inicio
  if (auth.isLoggedIn()) {
    router.navigate(['/inicio']);
  } else {
    router.navigate(['/login']);
  }

  return false;
};