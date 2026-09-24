// src/app/msal-config.ts
import {
  IPublicClientApplication,
  InteractionType
} from '@azure/msal-browser';
import { environment } from '../environments/environment';
import { msalInstance } from './msal-instance';

export function msalInstanceFactory(): IPublicClientApplication {
  return msalInstance;
}

export function msalInterceptorConfigFactory(): {
  interactionType: InteractionType;
  protectedResourceMap: Map<string, string[]>;
} {
  const protectedResourceMap = new Map<string, string[]>();

  // Scopes completos: openid/profile/offline_access + scopes de la API
  const scopesBackend = [
    'openid',
    'profile',
    'offline_access',
    ...environment.azure.protectedResourceScopes
  ];

  // ⚠️ CLAVE: usar patrón con `/**` para que coincida con subrutas
  protectedResourceMap.set(
    `${environment.apiBaseUrl}/**`,
    scopesBackend
  );

  // También agregar la URL base exacta (por si acaso)
  protectedResourceMap.set(
    environment.apiBaseUrl,
    scopesBackend
  );

  // API Gateway de AWS
  protectedResourceMap.set(
    'https://*.execute-api.*.amazonaws.com/**',
    scopesBackend
  );

  return {
    interactionType: InteractionType.Redirect,
    protectedResourceMap
  };
}

export function msalGuardConfigFactory(): {
  interactionType: InteractionType;
  authRequest: { scopes: string[] };
} {
  return {
    interactionType: InteractionType.Redirect,
    authRequest: {
      scopes: [
        'openid',
        'profile',
        'offline_access',
        ...environment.azure.protectedResourceScopes
      ]
    }
  };
}