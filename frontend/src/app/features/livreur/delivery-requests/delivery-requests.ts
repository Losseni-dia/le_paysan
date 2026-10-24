import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

import { LivreurService } from '../../../core/livreur/livreur.service';
import { DeliveryRequestResponse } from '../../../core/livreur/livreur.model';

@Component({
  selector: 'app-delivery-requests',
  imports: [DatePipe],
  templateUrl: './delivery-requests.html',
  styleUrl: './delivery-requests.scss',
})
export class DeliveryRequests {
  private readonly livreurService = inject(LivreurService);

  readonly requests = signal<DeliveryRequestResponse[]>([]);
  readonly loading = signal(true);
  readonly acceptingId = signal<number | null>(null);
  readonly error = signal<string | null>(null);

  constructor() {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.livreurService.listDeliveryRequests().subscribe({
      next: (requests) => {
        this.requests.set(requests);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  accept(request: DeliveryRequestResponse): void {
    this.acceptingId.set(request.id);
    this.error.set(null);
    this.livreurService.acceptDeliveryRequest(request.id).subscribe({
      next: () => {
        this.acceptingId.set(null);
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        this.acceptingId.set(null);
        this.error.set(err.error?.message ?? "Impossible d'accepter cette demande.");
        this.load();
      },
    });
  }
}
