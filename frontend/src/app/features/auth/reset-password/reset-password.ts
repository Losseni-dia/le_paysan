import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../core/auth/auth.service';
import { FieldError } from '../../../shared/ui/field-error/field-error';

@Component({
  selector: 'app-reset-password',
  imports: [ReactiveFormsModule, RouterLink, FieldError],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.scss',
})
export class ResetPassword {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);
  readonly done = signal(false);
  private readonly token = this.route.snapshot.queryParamMap.get('token') ?? '';

  readonly passwordMessages = {
    minlength: 'Le mot de passe doit contenir au moins 8 caractères.',
    pattern: 'Le mot de passe doit contenir au moins une lettre et un chiffre.',
  };

  readonly form = this.fb.nonNullable.group({
    newPassword: [
      '',
      [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[0-9])(?=.*[A-Za-z]).+$/)],
    ],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.authService.resetPassword(this.token, this.form.getRawValue().newPassword).subscribe({
      next: () => {
        this.submitting.set(false);
        this.done.set(true);
        setTimeout(() => this.router.navigateByUrl('/connexion'), 2000);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.serverError.set(
          err.status === 401 ? 'Lien invalide ou expiré, redemandez un email.' : 'Une erreur est survenue.'
        );
      },
    });
  }
}
