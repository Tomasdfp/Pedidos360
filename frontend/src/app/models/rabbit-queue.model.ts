export interface QueueInfo {
  name: string;
  messageCount: number;
  consumerCount: number;
  exists: boolean;
  status: string;
}

export interface QueueCreateRequest {
  name: string;
  durable?: boolean;
  exclusive?: boolean;
  autoDelete?: boolean;
  deadLetterExchange?: string;
  deadLetterRoutingKey?: string;
}

export interface TicketRequest {
  id?: number;
  titulo: string;
  descripcion: string;
  prioridad?: string;
  solicitante?: string;
  debeFallar?: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}
