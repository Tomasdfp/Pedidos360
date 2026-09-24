// src/app/models/pedido.model.ts

/**
 * Representa un pedido del sistema Pedidos360.
 */
export interface Pedido {
  id: number;
  numeroOrden: string;
  cliente: string;
  descripcion: string;
  monto: number;
  estado: 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADO' | 'CANCELADO';
  fechaCreacion: string; // ISO 8601
  fechaActualizacion?: string;
}

/**
 * Payload para crear un nuevo pedido.
 */
export interface CrearPedidoRequest {
  cliente: string;
  descripcion: string;
  monto: number;
}

/**
 * Payload para actualizar un pedido existente.
 */
export interface ActualizarPedidoRequest {
  descripcion?: string;
  estado?: 'PENDIENTE' | 'EN_PROCESO' | 'COMPLETADO' | 'CANCELADO';
}