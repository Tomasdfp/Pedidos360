// src/main.ts
import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { msalInstance } from './app/msal-instance';

msalInstance
  .initialize()
  .then(() => {
    msalInstance
      .handleRedirectPromise()
      .then((response) => {
        if (response) {
          msalInstance.setActiveAccount(response.account);
          console.log('✅ Cuenta activa desde redirect:', response.account?.username);
        } else {
          const accounts = msalInstance.getAllAccounts();
          if (accounts.length > 0) {
            msalInstance.setActiveAccount(accounts[0]);
            console.log('✅ Cuenta activa desde caché:', accounts[0].username);
          } else {
            console.warn('⚠️ No hay cuentas en caché');
          }
        }

        bootstrapApplication(App, appConfig).catch((err) =>
          console.error('Error en bootstrap:', err)
        );
      })
      .catch((err) => console.error('Error en handleRedirect:', err));
  })
  .catch((err) => console.error('Error inicializando MSAL:', err));