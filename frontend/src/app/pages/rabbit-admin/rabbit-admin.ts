import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RabbitAdminService } from '../../services/rabbit-admin.service';
import { QueueCreateRequest, QueueInfo, TicketRequest } from '../../models/rabbit-queue.model';

@Component({
  selector: 'app-rabbit-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './rabbit-admin.html',
  styleUrl: './rabbit-admin.scss'
})
export class RabbitAdminComponent implements OnInit {
  // Colas monitoreadas del sistema
  monitoredQueues: string[] = [
    'pedidos.queue',
    'notificacion.queue',
    'ticket.queue',
    'pedidos.dlq',
    'notificacion.dlq',
    'ticket.dlq'
  ];

  queueStates: QueueInfo[] = [];
  cargandoEstados = false;

  // Formulario Crear Cola
  nuevaCola: QueueCreateRequest = {
    name: '',
    durable: true,
    autoDelete: false,
    deadLetterExchange: 'pedidos.dlx',
    deadLetterRoutingKey: 'pedidos.dead-letter'
  };

  // Formulario Eliminar Cola
  colaAEliminar: string = '';

  // Formulario Emitir Ticket
  nuevoTicket: TicketRequest = {
    titulo: '',
    descripcion: '',
    prioridad: 'MEDIA',
    solicitante: '',
    debeFallar: false
  };

  mensajeExito: string = '';
  mensajeError: string = '';

  constructor(private rabbitService: RabbitAdminService) {}

  ngOnInit(): void {
    this.cargarEstadosColas();
  }

  cargarEstadosColas(): void {
    this.cargandoEstados = true;
    this.queueStates = [];

    this.monitoredQueues.forEach((queueName) => {
      this.rabbitService.getQueueInfo(queueName).subscribe({
        next: (resp) => {
          if (resp.data) {
            this.queueStates.push(resp.data);
          }
          this.cargandoEstados = false;
        },
        error: () => {
          this.queueStates.push({
            name: queueName,
            messageCount: 0,
            consumerCount: 0,
            exists: false,
            status: 'NO_DISPONIBLE'
          });
          this.cargandoEstados = false;
        }
      });
    });
  }

  crearCola(): void {
    if (!this.nuevaCola.name || this.nuevaCola.name.trim().length === 0) {
      this.mostrarError('El nombre de la cola es obligatorio.');
      return;
    }

    this.limpiarMensajes();
    this.rabbitService.createQueue(this.nuevaCola).subscribe({
      next: (resp) => {
        this.mostrarExito(resp.message || `Cola '${this.nuevaCola.name}' creada exitosamente.`);
        if (!this.monitoredQueues.includes(this.nuevaCola.name.trim())) {
          this.monitoredQueues.push(this.nuevaCola.name.trim());
        }
        this.nuevaCola.name = '';
        this.cargarEstadosColas();
      },
      error: (err) => {
        const msg = err.error?.message || err.error?.data?.name || 'Error al crear la cola en RabbitMQ.';
        this.mostrarError(msg);
      }
    });
  }

  eliminarCola(nombre?: string): void {
    const queueToDelete = nombre || this.colaAEliminar;
    if (!queueToDelete || queueToDelete.trim().length === 0) {
      this.mostrarError('Indica el nombre de la cola a eliminar.');
      return;
    }

    if (!confirm(`¿Estás seguro de eliminar la cola '${queueToDelete}'?`)) {
      return;
    }

    this.limpiarMensajes();
    this.rabbitService.deleteQueue(queueToDelete).subscribe({
      next: (resp) => {
        this.mostrarExito(resp.message || `Cola '${queueToDelete}' eliminada exitosamente.`);
        this.monitoredQueues = this.monitoredQueues.filter((q) => q !== queueToDelete.trim());
        this.colaAEliminar = '';
        this.cargarEstadosColas();
      },
      error: (err) => {
        const msg = err.error?.message || 'Error al eliminar la cola de RabbitMQ.';
        this.mostrarError(msg);
      }
    });
  }

  enviarTicket(): void {
    if (!this.nuevoTicket.titulo || !this.nuevoTicket.descripcion) {
      this.mostrarError('El título y descripción del ticket son obligatorios.');
      return;
    }

    this.limpiarMensajes();
    this.rabbitService.sendTicket(this.nuevoTicket).subscribe({
      next: (resp) => {
        const alerta = this.nuevoTicket.debeFallar
          ? `Ticket enviado con simulación de fallo. Observa los logs del backend para ver el envío automático a 'ticket.dlq'!`
          : `Ticket enviado a 'ticket.queue'. El consumidor lo procesará con ACK manual!`;
        this.mostrarExito(alerta);
        this.nuevoTicket.titulo = '';
        this.nuevoTicket.descripcion = '';
        this.nuevoTicket.debeFallar = false;
        setTimeout(() => this.cargarEstadosColas(), 800);
      },
      error: (err) => {
        this.mostrarError(err.error?.message || 'Error al emitir el ticket.');
      }
    });
  }

  private mostrarExito(msg: string): void {
    this.mensajeExito = msg;
    this.mensajeError = '';
    setTimeout(() => (this.mensajeExito = ''), 6000);
  }

  private mostrarError(msg: string): void {
    this.mensajeError = msg;
    this.mensajeExito = '';
    setTimeout(() => (this.mensajeError = ''), 6000);
  }

  private limpiarMensajes(): void {
    this.mensajeExito = '';
    this.mensajeError = '';
  }
}
