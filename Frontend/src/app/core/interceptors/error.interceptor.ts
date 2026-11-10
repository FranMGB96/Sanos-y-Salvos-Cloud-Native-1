import { Injectable } from '@angular/core';
import { HttpContextToken, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ErrorToastService } from '../services/error-toast.service';

/**
 * Permite que una petición maneje su propio error sin mostrar el toast global.
 * Lo usa el panel de actividad: consulta cada 5 segundos y, si el servicio no responde,
 * muestra el aviso dentro del panel en vez de llenar la pantalla de toasts.
 */
export const SIN_TOAST_DE_ERROR = new HttpContextToken<boolean>(() => false);

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private toast: ErrorToastService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((err: HttpErrorResponse) => {
        if (!req.context.get(SIN_TOAST_DE_ERROR)) {
          this.toast.show({
            status: err.status,
            message: err.error?.message || err.message || 'Error desconocido',
            url: req.url,
          });
        }
        return throwError(() => err);
      })
    );
  }
}
