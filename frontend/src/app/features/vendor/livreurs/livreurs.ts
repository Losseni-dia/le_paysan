import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { LivreurService } from '../../../core/livreur/livreur.service';
import { CreateLivreurRequest, LivreurResponse } from '../../../core/livreur/livreur.model';

@Component({
  selector: 'app-livreurs',
  imports: [FormsModule],
  templateUrl: './livreurs.html',
  styleUrl: './livreurs.scss',
})
export class Livreurs {
  private readonly livreurService = inject(LivreurService);

  readonly livreurs = signal<LivreurResponse[]>([]);
  readonly loading = signal(true);
  readonly showForm = signal(false);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly form: CreateLivreurRequest = { username: '', password: '', firstName: '', lastName: '', phone: '' };

  constructor() {
    this.load();
  }

  toggleForm(): void {
    this.showForm.update((v) => !v);
    this.error.set(null);
  }

  submit(): void {
    this.saving.set(true);
    this.error.set(null);
    this.livreurService.create(this.form).subscribe({
      next: (livreur) => {
        this.livreurs.set([livreur, ...this.livreurs()]);
        this.saving.set(false);
        this.showForm.set(false);
        this.form.username = '';
        this.form.password = '';
        this.form.firstName = '';
        this.form.lastName = '';
        this.form.phone = '';
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Impossible de créer ce compte livreur.');
      },
    });
  }

  private load(): void {
    this.loading.set(true);
    this.livreurService.listForShop().subscribe({
      next: (livreurs) => {
        this.livreurs.set(livreurs);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
