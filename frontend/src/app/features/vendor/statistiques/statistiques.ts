import { Component, inject, signal } from '@angular/core';

import { VendorDashboardService } from '../../../core/vendor/vendor-dashboard.service';
import { VendorStatsResponse } from '../../../core/vendor/vendor-dashboard.model';

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
  selector: 'app-vendor-statistiques',
  imports: [],
  templateUrl: './statistiques.html',
  styleUrl: './statistiques.scss',
})
export class Statistiques {
  private readonly dashboardService = inject(VendorDashboardService);

  readonly data = signal<VendorStatsResponse | null>(null);
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
