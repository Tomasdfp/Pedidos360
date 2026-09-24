// src/app/msal-instance.ts
import { PublicClientApplication, IPublicClientApplication } from '@azure/msal-browser';
import { environment } from '../environments/environment';

/**
 * Instancia SINGLETON de PublicClientApplication.
 *
 * Se exporta como constante para garantizar que TODA la aplicación
 * (main.ts, msal-config.ts, app.config.ts) use la MISMA instancia.
 *
 * Esto evita el error 'uninitialized_public_client_application' que
 * ocurre cuando MsalGuard usa una instancia diferente a la inicializada
 * en main.ts.
 */
export const msalInstance: IPublicClientApplication = new PublicClientApplication({
  auth: {
    clientId: environment.azure.clientId,
    authority: environment.azure.authority,
    redirectUri: environment.azure.redirectUri,
    postLogoutRedirectUri: environment.azure.redirectUri
  },
  cache: {
    cacheLocation: 'localStorage'
  }
});