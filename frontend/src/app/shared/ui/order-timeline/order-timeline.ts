import { Component, computed, inject, input } from '@angular/core';
import { DatePipe } from '@angular/common';

import { FileUrlService } from '../../../core/common/file-url.service';
import { OrderStatus, OrderStatusHistoryResponse } from '../../../core/order/order.model';

const STEP_LABELS: Record<string, string> = {
  PAYEE: 'Paiement confirmé',
  EN_PREPARATION: 'En préparation',
  PRETE: 'Prête (retrait)',
  EXPEDIEE: 'Expédiée',
  RECUPEREE: 'Récupérée par le livreur',
  EN_ROUTE: 'En route',
  LIVREE: 'Livrée',
};

const CHANGED_BY_LABELS: Record<string, string> = {
  SYSTEME: 'Auto',
  VENDEUR: 'Vendeur',
  ACHETEUR: 'Acheteur',
  LIVREUR: 'Livreur',
};

interface TimelineStep {
  status: string;
  label: string;
  reached: boolean;
  current: boolean;
  changedAt: string | null;
  changedByLabel: string | null;
  note: string | null;
  proofUrl: string | null;
  proofTypeLabel: string | null;
}

@Component({
  selector: 'app-order-timeline',
  imports: [DatePipe],
  templateUrl: './order-timeline.html',
  styleUrl: './order-timeline.scss',
})
export class OrderTimeline {
  private readonly fileUrlService = inject(FileUrlService);

  readonly historique = input.required<OrderStatusHistoryResponse[]>();
  readonly status = input.required<OrderStatus>();

  readonly cancelled = computed(() => this.status() === 'ANNULEE');

  readonly cancelledEntry = computed(() => this.historique().find((h) => h.status === 'ANNULEE') ?? null);

  readonly steps = computed<TimelineStep[]>(() => {
    const history = this.historique();
    const hasLivreurSteps = history.some((h) => h.status === 'RECUPEREE' || h.status === 'EN_ROUTE');
    const thirdStepStatus = history.find((h) => h.status === 'EXPEDIEE')
      ? 'EXPEDIEE'
      : history.find((h) => h.status === 'PRETE')
        ? 'PRETE'
        : 'PRETE';

    const sequence = hasLivreurSteps
      ? ['PAYEE', 'EN_PREPARATION', 'EXPEDIEE', 'RECUPEREE', 'EN_ROUTE', 'LIVREE']
      : ['PAYEE', 'EN_PREPARATION', thirdStepStatus, 'LIVREE'];
    const currentIndex = sequence.indexOf(this.status());

    return sequence.map((status, index) => {
      const entry = history.find((h) => h.status === status);
      // Ancienne commande sans historique complet : on deduit les etapes passees a partir du
      // statut courant plutot que de laisser la frise vide quand aucune ligne d'historique n'existe.
      const reached = !!entry || (currentIndex >= 0 && index <= currentIndex);
      return {
        status,
        label: STEP_LABELS[status],
        reached,
        current: this.status() === status,
        changedAt: entry?.changedAt ?? null,
        changedByLabel: entry ? (CHANGED_BY_LABELS[entry.changedBy] ?? entry.changedBy) : null,
        note: entry?.note ?? null,
        proofUrl: entry?.proofUrl ?? null,
        proofTypeLabel: entry?.proofType === 'SIGNATURE' ? 'Signature' : entry?.proofType === 'PHOTO' ? 'Photo' : null,
      };
    });
  });

  viewProof(proofUrl: string): void {
    this.fileUrlService.getBlobUrl(proofUrl).subscribe((blobUrl) => window.open(blobUrl, '_blank'));
  }
}
