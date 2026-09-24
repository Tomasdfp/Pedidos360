// src/app/app.spec.ts
declare const describe: any;
declare const beforeEach: any;
declare const it: any;
declare const expect: any;

import { TestBed } from '@angular/core/testing';
import { App } from './app';
import { MsalService, MsalBroadcastService, MSAL_GUARD_CONFIG } from '@azure/msal-angular';
import { of } from 'rxjs';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        { provide: MSAL_GUARD_CONFIG, useValue: { interactionType: 1, authRequest: { scopes: [] } } },
        { provide: MsalService, useValue: {
            instance: {
              getAllAccounts: () => [],
              setActiveAccount: () => {},
              loginRedirect: () => {},
              logoutRedirect: () => {}
            }
        }},
        { provide: MsalBroadcastService, useValue: {
            inProgress$: of(0),
            msalSubject$: of({})
        }}
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should have the title "Pedidos360"', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance.title).toEqual('Pedidos360');
  });
});