// src/app/services/pedidos.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  Pedido,
  CrearPedidoRequest,
  ActualizarPedidoRequest
} from '../models/pedido.model';

@Injectable({
  providedIn: 'root'
})
export class PedidosService {
  private readonly apiUrl = `${environment.apiBaseUrl}/api/pedidos`;

  constructor(private http: HttpClient) {}

  /**
   * Obtiene la lista completa de pedidos del usuario autenticado.
   * Requiere scope: pedidos.read
   */
  getPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.apiUrl);
  }

  /**
   * Obtiene un pedido específico por su ID.
   * Requiere scope: pedidos.read
   */
  getPedidoById(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.apiUrl}/${id}`);
  }

  /**
   * Crea un nuevo pedido.
   * Requiere scope: pedidos.write
   */
  crearPedido(data: CrearPedidoRequest): Observable<Pedido> {
    return this.http.post<Pedido>(this.apiUrl, data);
  }

  /**
   * Actualiza un pedido existente.
   * Requiere scope: pedidos.write
   */
  actualizarPedido(id: number, data: ActualizarPedidoRequest): Observable<Pedido> {
    return this.http.put<Pedido>(`${this.apiUrl}/${id}`, data);
  }

  /**
   * Elimina un pedido.
   * Requiere scope: pedidos.write
   */
  eliminarPedido(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}                                       