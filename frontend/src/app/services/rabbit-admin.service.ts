import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApiResponse, QueueCreateRequest, QueueInfo, TicketRequest } from '../models/rabbit-queue.model';

@Injectable({
  providedIn: 'root'
})
export class RabbitAdminService {
  private readonly baseUrl = environment.rabbitAdminApiUrl || 'http://localhost:8082';

  constructor(private http: HttpClient) {}

  /**
   * Crea una nueva cola en RabbitMQ
   */
  createQueue(request: QueueCreateRequest): Observable<ApiResponse<QueueInfo>> {
    return this.http.post<ApiResponse<QueueInfo>>(`${this.baseUrl}/queues`, request);
  }

  /**
   * Elimina una cola existente
   */
  deleteQueue(name: string): Observable<ApiResponse<boolean>> {
    return this.http.delete<ApiResponse<boolean>>(`${this.baseUrl}/queues/${encodeURIComponent(name)}`);
  }

  /**
   * Obtiene la información en tiempo real de una cola (conteo de mensajes y consumidores)
   */
  getQueueInfo(name: string): Observable<ApiResponse<QueueInfo>> {
    return this.http.get<ApiResponse<QueueInfo>>(`${this.baseUrl}/queues/${encodeURIComponent(name)}`);
  }

  /**
   * Publica un ticket de soporte/documentos para validar el procesamiento asíncrono y DLQ
   */
  sendTicket(ticket: TicketRequest): Observable<ApiResponse<TicketRequest>> {
    return this.http.post<ApiResponse<TicketRequest>>(`${this.baseUrl}/tickets`, ticket);
  }
}
