import {
  IPublicClientApplication,
  PublicClientApplication,
  BrowserCacheLocation,
  InteractionType,
} from '@azure/msal-browser';
import {
  MsalGuardConfiguration,
  MsalInterceptorConfiguration,
} from '@azure/msal-angular';
import { environment } from '../environments/environment';

export function msalInstanceFactory(): IPublicClientApplication {
  return new PublicClientApplication({
    auth: {
      clientId: environment.azure.clientId,
      authority: environment.azure.authority,
      redirectUri: environment.azure.redirectUri,
    },
    cache: {
      cacheLocation: BrowserCacheLocation.LocalStorage,
    },
  });
}

// Redirect es el flujo recomendado por Microsoft para apps standalone de
// Angular: MsalRedirectComponent (la alternativa "oficial" para Popup) no es
// standalone ni está exportado por MsalModule, así que no es utilizable
// limpiamente aquí. Con Redirect, el procesamiento del resultado se hace
// manualmente vía handleRedirectPromise() en app.config.ts.
export function msalGuardConfigFactory(): MsalGuardConfiguration {
  return {
    interactionType: InteractionType.Redirect,
    authRequest: {
      scopes: environment.azure.scopes,
    },
  };
}

export function msalInterceptorConfigFactory(): MsalInterceptorConfiguration {
  const protectedResourceMap = new Map<string, Array<string>>();
  // Cualquier llamada a nuestra API (a través del gateway) lleva el token
  // con el scope Pets.Access adjunto automáticamente.
  protectedResourceMap.set(`${environment.apiUrl}/*`, environment.azure.scopes);

  return {
    interactionType: InteractionType.Redirect,
    protectedResourceMap,
  };
}
