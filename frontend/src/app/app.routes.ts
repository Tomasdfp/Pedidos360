import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { Home } from './pages/home/home';

export const routes: Routes = [
  {
    path: '',
    component: Home,
    canActivate: [MsalGuard]
  },
  {
    path: 'admin-rabbit',
    loadComponent: () => import('./pages/rabbit-admin/rabbit-admin').then(m => m.RabbitAdminComponent),
    canActivate: [MsalGuard]
  },
  {
    path: '**',
    redirectTo: ''
  }
];