import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { LivreurService } from '../../../core/livreur/livreur.service';
import { LivreurAdminResponse } from '../../../core/livreur/livreur.model';
import { AvatarThumb } from '../../../shared/ui/avatar-thumb/avatar-thumb';

const STATUS_FILTERS = [
  { value: 'EN_ATTENTE', label: 'En attente' },
  { value: 'VALIDE', label: 'Validés' },
  { value: 'REJETE', label: 'Rejetés' },
  { value: '', label: 'Tous' },
  { value: 'SUSPENDUS', label: 'Suspendus' },
];

@Component({
  selector: 'app-livreurs-kyc-list',
  imports: [RouterLink, DatePipe, AvatarThumb, FormsModule],
  templateUrl: './livreurs-kyc-list.html',
  styleUrl: './livreurs-kyc-list.scss',
})
export class LivreursKycList {
  private readonly livreurService = inject(LivreurService);

  readonly statusFilters = STATUS_FILTERS;
  readonly selectedStatus = signal('EN_ATTENTE');
  readonly livreurs = signal<LivreurAdminResponse[]>([]);
  readonly loading = signal(true);
  readonly searchQuery = signal('');

  /** "Suspendus" n'est pas un statut KYC : on recupere tout puis on filtre ici sur enabled. */
  readonly visibleLivreurs = computed(() => {
    const byStatus =
      this.selectedStatus() === 'SUSPENDUS' ? this.livreurs().filter((l) => !l.enabled) : this.livreurs();
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) return byStatus;
    return byStatus.filter(
      (l) =>
        `${l.firstName} ${l.lastName}`.toLowerCase().includes(q) ||
        l.username.toLowerCase().includes(q) ||
        (l.shopName ?? '').toLowerCase().includes(q) ||
        (l.city ?? '').toLowerCase().includes(q),
    );
  });

  constructor() {
    this.load();
  }

  selectStatus(status: string): void {
    this.selectedStatus.set(status);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    const kycStatus = this.selectedStatus() === 'SUSPENDUS' ? undefined : this.selectedStatus() || undefined;
    this.livreurService.listForAdmin(kycStatus).subscribe({
      next: (page) => {
        this.livreurs.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
