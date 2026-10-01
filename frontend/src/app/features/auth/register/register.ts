import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../core/auth/auth.service';
import { FieldError } from '../../../shared/ui/field-error/field-error';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink, FieldError],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);
  readonly selfieFile = signal<File | null>(null);
  readonly selfieFileName = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9._-]{3,30}$/)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[0-9])(?=.*[A-Za-z]).+$/)]],
    email: ['', [Validators.email]],
    firstName: ['', [Validators.required]],
    lastName: ['', [Validators.required]],
    phone: [''],
    acceptCgu: [false, [Validators.requiredTrue]],
    acceptCgv: [false, [Validators.requiredTrue]],
    acceptPrivacy: [false, [Validators.requiredTrue]],
  });

  readonly usernameMessages = {
    pattern: 'Login invalide (3 à 30 caractères : lettres, chiffres, points, tirets).',
  };
  readonly passwordMessages = {
    minlength: 'Le mot de passe doit contenir au moins 8 caractères.',
    pattern: 'Le mot de passe doit contenir au moins une lettre et un chiffre.',
  };

  onSelfieSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.selfieFile.set(file);
    this.selfieFileName.set(file?.name ?? null);
  }

  submit(): void {
    this.serverError.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    this.submitting.set(true);
    this.authService
      .register({
        username: raw.username,
        password: raw.password,
        email: raw.email || undefined,
        firstName: raw.firstName,
        lastName: raw.lastName,
        phone: raw.phone || undefined,
        acceptCgu: raw.acceptCgu,
        acceptCgv: raw.acceptCgv,
        acceptPrivacy: raw.acceptPrivacy,
      })
      .subscribe({
        next: () => this.afterRegister(),
        error: (err: HttpErrorResponse) => {
          this.submitting.set(false);
          this.serverError.set(
            err.status === 409 ? 'Ce login (ou cet email) est déjà utilisé.' : "Une erreur est survenue, réessayez."
          );
        },
      });
  }

  private afterRegister(): void {
    const selfie = this.selfieFile();
    if (!selfie) {
      this.submitting.set(false);
      this.router.navigateByUrl('/');
      return;
    }

    // Upload du selfie de securite en complement, non bloquant pour l'inscription :
    // en cas d'echec, le compte reste cree, l'utilisateur pourra reessayer depuis son profil.
    this.authService.uploadSecuritySelfie(selfie).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigateByUrl('/');
      },
      error: () => {
        this.submitting.set(false);
        this.router.navigateByUrl('/');
      },
    });
  }
}
