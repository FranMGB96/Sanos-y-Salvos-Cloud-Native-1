import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi, HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations';
import {
  MsalService,
  MsalGuard,
  MsalBroadcastService,
  MsalInterceptor,
  MSAL_INSTANCE,
  MSAL_GUARD_CONFIG,
  MSAL_INTERCEPTOR_CONFIG,
} from '@azure/msal-angular';
import { routes } from './app.routes';
import {
  msalInstanceFactory,
  msalGuardConfigFactory,
  msalInterceptorConfigFactory,
} from './msal-config';

// MSAL exige llamar a initialize() antes de que la app lo use, y con el
// flujo Redirect hay que procesar manualmente la respuesta que vuelve de
// Microsoft (handleRedirectPromise), ya que no usamos MsalRedirectComponent.
export function initializeMsal(msalService: MsalService) {
  return () => msalService.instance.initialize().then(() =>
    msalService.instance.handleRedirectPromise().then((result) => {
      if (result?.account) {
        msalService.instance.setActiveAccount(result.account);
      }
    })
  );
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // withInterceptorsFromDi() habilita interceptores basados en clases (como MsalInterceptor)
    provideHttpClient(withInterceptorsFromDi()),
    provideAnimations(),

    MsalService,
    MsalGuard,
    MsalBroadcastService,
    { provide: MSAL_INSTANCE, useFactory: msalInstanceFactory },
    { provide: MSAL_GUARD_CONFIG, useFactory: msalGuardConfigFactory },
    { provide: MSAL_INTERCEPTOR_CONFIG, useFactory: msalInterceptorConfigFactory },
    { provide: HTTP_INTERCEPTORS, useClass: MsalInterceptor, multi: true },
    { provide: APP_INITIALIZER, useFactory: initializeMsal, deps: [MsalService], multi: true },
  ]
};
