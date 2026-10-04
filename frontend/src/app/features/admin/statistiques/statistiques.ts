import { Component, inject, signal } from '@angular/core';

import { DashboardService } from '../../../core/admin/dashboard.service';
import { StatsResponse } from '../../../core/admin/dashboard.model';

const STATUS_LABELS: Partial<Record<string, string>> = {
  EN_ATTENTE_PAIEMENT: 'En attente de paiement',
  PAYEE: 'Payée',
  EN_PREPARATION: 'En préparation',
  PRETE: 'Prête (retrait)',
  EXPEDIEE: 'Expédiée',
  LIVREE: 'Livrée',
  ANNULEE: 'Annulée',
};

@Component({
  selector: 'app-statistiques',
  imports: [],
  templateUrl: './statistiques.html',
  styleUrl: './statistiques.scss',
})
export class Statistiques {
  private readonly dashboardService = inject(DashboardService);

  readonly data = signal<StatsResponse | null>(null);
  readonly loading = signal(true);
  readonly statusLabels = STATUS_LABELS;

  readonly statusEntries = () => Object.entries(this.data()?.commandesParStatut ?? {});

  constructor() {
    this.dashboardService.getStats().subscribe({
      next: (data) => {
        this.data.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
