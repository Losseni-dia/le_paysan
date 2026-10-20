import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';

import { LivreurService } from '../../../core/livreur/livreur.service';
import { LivreurAdminResponse } from '../../../core/livreur/livreur.model';
import { AvatarThumb } from '../../../shared/ui/avatar-thumb/avatar-thumb';

const STATUS_FILTERS = [
  { value: 'EN_ATTENTE', label: 'En attente' },
  { value: 'VALIDE', label: 'Validés' },
  { value: 'REJETE', label: 'Rejetés' },
  { value: '', label: 'Tous' },
];

@Component({
  selector: 'app-livreurs-kyc-list',
  imports: [RouterLink, DatePipe, AvatarThumb],
  templateUrl: './livreurs-kyc-list.html',
  styleUrl: './livreurs-kyc-list.scss',
})
export class LivreursKycList {
  private readonly livreurService = inject(LivreurService);

  readonly statusFilters = STATUS_FILTERS;
  readonly selectedStatus = signal('EN_ATTENTE');
  readonly livreurs = signal<LivreurAdminResponse[]>([]);
  readonly loading = signal(true);

  constructor() {
    this.load();
  }

  selectStatus(status: string): void {
    this.selectedStatus.set(status);
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.livreurService.listForAdmin(this.selectedStatus() || undefined).subscribe({
      next: (page) => {
        this.livreurs.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
