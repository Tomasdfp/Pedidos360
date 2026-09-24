// src/app/pages/home/home.ts
import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MsalService, MsalBroadcastService } from '@azure/msal-angular';
import {
  EventMessage,
  EventType,
  InteractionStatus,
  AccountInfo
} from '@azure/msal-browser';
import { Subject, filter, takeUntil } from 'rxjs';
import { PedidosService } from '../../services/pedidos.service';
import { Pedido } from '../../models/pedido.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.scss'
})
export class Home implements OnInit, OnDestroy {
  user: AccountInfo | null = null;
  pedidos: Pedido[] = [];
  cargando = false;
  errorMensaje = '';

  private readonly _destroying$ = new Subject<void>();

  constructor(
    private authService: MsalService,
    private msalBroadcastService: MsalBroadcastService,
    private pedidosService: PedidosService
  ) {}

  ngOnInit(): void {
    this.msalBroadcastService.msalSubject$
      .pipe(
        filter(
          (msg: EventMessage) =>
            msg.eventType === EventType.LOGIN_SUCCESS ||
            msg.eventType === EventType.ACQUIRE_TOKEN_SUCCESS
        ),
        takeUntil(this._destroying$)
      )
      .subscribe(() => this.updateUser());

    this.msalBroadcastService.inProgress$
      .pipe(
        filter((status: InteractionStatus) => status === InteractionStatus.None),
        takeUntil(this._destroying$)
      )
      .subscribe(() => this.updateUser());

    this.updateUser();
  }

  updateUser(): void {
    const accounts = this.authService.instance.getAllAccounts();
    this.user = accounts.length > 0 ? accounts[0] : null;
  }

  /**
   * Prueba el consumo del backend protegido.
   * El MsalInterceptor adjunta el token automáticamente.
   */
  probarLlamadaApi(): void {
    this.cargando = true;
    this.errorMensaje = '';

    this.pedidosService.getPedidos().subscribe({
      next: (data) => {
        this.pedidos = data;
        this.cargando = false;
        console.log('✅ Respuesta del backend:', data);
      },
      error: (err) => {
        this.cargando = false;
        this.errorMensaje = `Error al llamar al backend: ${err.message}`;
        console.error('❌ Error en la llamada:', err);
      }
    });
  }

  logout(): void {
    this.authService.logoutRedirect({
      postLogoutRedirectUri: 'http://localhost:4200'
    });
  }

  ngOnDestroy(): void {
    this._destroying$.next(undefined);
    this._destroying$.complete();
  }
}