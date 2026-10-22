import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { LivreurService } from '../../../core/livreur/livreur.service';
import { CreateLivreurRequest, LivreurResponse, UpdateLivreurRequest } from '../../../core/livreur/livreur.model';
import { AvatarThumb } from '../../../shared/ui/avatar-thumb/avatar-thumb';

@Component({
  selector: 'app-livreurs',
  imports: [FormsModule, AvatarThumb],
  templateUrl: './livreurs.html',
  styleUrl: './livreurs.scss',
})
export class Livreurs {
  private readonly livreurService = inject(LivreurService);

  readonly livreurs = signal<LivreurResponse[]>([]);
  readonly loading = signal(true);
  readonly showForm = signal(false);
  readonly searchQuery = signal('');

  readonly filteredLivreurs = computed(() => {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) return this.livreurs();
    return this.livreurs().filter(
      (l) =>
        `${l.firstName} ${l.lastName}`.toLowerCase().includes(q) ||
        l.username.toLowerCase().includes(q) ||
        (l.city ?? '').toLowerCase().includes(q),
    );
  });
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);

  readonly resettingId = signal<number | null>(null);
  readonly newPassword = signal('');
  readonly resetError = signal<string | null>(null);

  readonly editingId = signal<number | null>(null);
  readonly editSaving = signal(false);
  readonly editError = signal<string | null>(null);
  readonly editForm: UpdateLivreurRequest = {
    firstName: '',
    lastName: '',
    phone: '',
    city: '',
    photo: null,
    idDocumentFront: null,
    idDocumentBack: null,
    selfieWithId: null,
  };

  readonly form: CreateLivreurRequest = {
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    city: '',
    photo: null,
    idDocumentFront: null,
    idDocumentBack: null,
    selfieWithId: null,
  };

  constructor() {
    this.load();
  }

  toggleForm(): void {
    this.showForm.update((v) => !v);
    this.error.set(null);
  }

  onPhotoSelected(event: Event): void {
    this.form.photo = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  onIdDocumentFrontSelected(event: Event): void {
    this.form.idDocumentFront = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  onIdDocumentBackSelected(event: Event): void {
    this.form.idDocumentBack = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  onSelfieWithIdSelected(event: Event): void {
    this.form.selfieWithId = (event.target as HTMLInputElement).files?.[0] ?? null;
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
        this.form.city = '';
        this.form.photo = null;
        this.form.idDocumentFront = null;
        this.form.idDocumentBack = null;
        this.form.selfieWithId = null;
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Impossible de créer ce compte livreur.');
      },
    });
  }

  toggleEnabled(livreur: LivreurResponse): void {
    this.livreurService.setEnabled(livreur.id, !livreur.enabled).subscribe({
      next: (updated) => {
        this.livreurs.set(this.livreurs().map((l) => (l.id === updated.id ? updated : l)));
      },
    });
  }

  startResetPassword(livreur: LivreurResponse): void {
    this.resettingId.set(livreur.id);
    this.newPassword.set('');
    this.resetError.set(null);
  }

  cancelResetPassword(): void {
    this.resettingId.set(null);
  }

  confirmResetPassword(livreur: LivreurResponse): void {
    if (this.newPassword().length < 8) {
      this.resetError.set('Le mot de passe doit contenir au moins 8 caractères.');
      return;
    }
    this.resetError.set(null);
    this.livreurService.resetPassword(livreur.id, this.newPassword()).subscribe({
      next: () => {
        this.resettingId.set(null);
        this.newPassword.set('');
      },
      error: () => this.resetError.set('Impossible de réinitialiser ce mot de passe.'),
    });
  }

  startEdit(livreur: LivreurResponse): void {
    this.editingId.set(livreur.id);
    this.editError.set(null);
    this.editForm.firstName = livreur.firstName;
    this.editForm.lastName = livreur.lastName;
    this.editForm.phone = livreur.phone ?? '';
    this.editForm.city = livreur.city ?? '';
    this.editForm.photo = null;
    this.editForm.idDocumentFront = null;
    this.editForm.idDocumentBack = null;
    this.editForm.selfieWithId = null;
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  onEditPhotoSelected(event: Event): void {
    this.editForm.photo = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  onEditIdDocumentFrontSelected(event: Event): void {
    this.editForm.idDocumentFront = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  onEditIdDocumentBackSelected(event: Event): void {
    this.editForm.idDocumentBack = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  onEditSelfieWithIdSelected(event: Event): void {
    this.editForm.selfieWithId = (event.target as HTMLInputElement).files?.[0] ?? null;
  }

  confirmEdit(livreur: LivreurResponse): void {
    if (!this.editForm.firstName.trim() || !this.editForm.lastName.trim()) {
      this.editError.set('Le prénom et le nom sont obligatoires.');
      return;
    }
    this.editSaving.set(true);
    this.editError.set(null);
    this.livreurService.update(livreur.id, this.editForm).subscribe({
      next: (updated) => {
        this.livreurs.set(this.livreurs().map((l) => (l.id === updated.id ? updated : l)));
        this.editSaving.set(false);
        this.editingId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        this.editSaving.set(false);
        this.editError.set(err.error?.message ?? 'Impossible de modifier ce livreur.');
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
