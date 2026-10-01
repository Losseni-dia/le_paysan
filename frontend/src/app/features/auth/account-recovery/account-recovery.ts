import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../core/auth/auth.service';
import { FieldError } from '../../../shared/ui/field-error/field-error';

/**
 * UC-01b (sans email) : demande de recuperation par selfie, comparee
 * manuellement par un admin au selfie de reference enregistre.
 */
@Component({
  selector: 'app-account-recovery',
  imports: [ReactiveFormsModule, RouterLink, FieldError],
  templateUrl: './account-recovery.html',
  styleUrl: './account-recovery.scss',
})
export class AccountRecovery {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);
  readonly done = signal(false);
  readonly selfieFile = signal<File | null>(null);
  readonly selfieFileName = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
  });

  onSelfieSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selfieFile.set(file);
    this.selfieFileName.set(file?.name ?? null);
  }

  submit(): void {
    this.serverError.set(null);
    if (this.form.invalid || !this.selfieFile()) {
      this.form.markAllAsTouched();
      if (!this.selfieFile()) {
        this.serverError.set('Veuillez prendre ou sélectionner un selfie.');
      }
      return;
    }

    this.submitting.set(true);
    this.authService.accountRecoveryRequest(this.form.getRawValue().username, this.selfieFile()!).subscribe({
      next: () => {
        this.submitting.set(false);
        this.done.set(true);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.serverError.set(err.status === 404 ? 'Aucun compte trouvé pour ce login.' : 'Une erreur est survenue.');
      },
    });
  }
}
