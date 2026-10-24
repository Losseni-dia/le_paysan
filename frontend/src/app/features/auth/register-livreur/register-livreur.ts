import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../core/auth/auth.service';
import { CI_CITIES } from '../../../core/catalogue/ci-cities';
import { FieldError } from '../../../shared/ui/field-error/field-error';

function passwordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (!password || !confirmPassword || password === confirmPassword) {
    return null;
  }
  control.get('confirmPassword')?.setErrors({ ...control.get('confirmPassword')?.errors, passwordMismatch: true });
  return null;
}

@Component({
  selector: 'app-register-livreur',
  imports: [ReactiveFormsModule, RouterLink, FieldError],
  templateUrl: './register-livreur.html',
  styleUrl: './register-livreur.scss',
})
export class RegisterLivreur {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly cities = CI_CITIES;
  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);
  readonly showPassword = signal(false);
  readonly showConfirmPassword = signal(false);

  readonly form = this.fb.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.pattern(/^[a-zA-Z0-9._-]{3,30}$/)]],
      password: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[0-9])(?=.*[A-Za-z]).+$/)]],
      confirmPassword: ['', [Validators.required]],
      firstName: ['', [Validators.required]],
      lastName: ['', [Validators.required]],
      phone: [''],
      city: ['', [Validators.required]],
      acceptCgu: [false, [Validators.requiredTrue]],
      acceptCgv: [false, [Validators.requiredTrue]],
      acceptPrivacy: [false, [Validators.requiredTrue]],
    },
    { validators: passwordsMatchValidator },
  );

  readonly usernameMessages = {
    pattern: 'Login invalide (3 à 30 caractères : lettres, chiffres, points, tirets).',
  };
  readonly passwordMessages = {
    minlength: 'Le mot de passe doit contenir au moins 8 caractères.',
    pattern: 'Le mot de passe doit contenir au moins une lettre et un chiffre.',
  };
  readonly confirmPasswordMessages = {
    passwordMismatch: 'Les mots de passe ne correspondent pas.',
  };

  toggleShowPassword(): void {
    this.showPassword.update((v) => !v);
  }

  toggleShowConfirmPassword(): void {
    this.showConfirmPassword.update((v) => !v);
  }

  submit(): void {
    this.serverError.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();
    const city = this.cities.find((c) => c.name === raw.city);
    if (!city) {
      this.serverError.set('Choisissez une ville dans la liste.');
      return;
    }

    this.submitting.set(true);
    this.authService
      .registerLivreur({
        username: raw.username,
        password: raw.password,
        firstName: raw.firstName,
        lastName: raw.lastName,
        phone: raw.phone || undefined,
        city: city.name,
        cityLat: city.lat,
        cityLng: city.lng,
        acceptCgu: raw.acceptCgu,
        acceptCgv: raw.acceptCgv,
        acceptPrivacy: raw.acceptPrivacy,
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.router.navigateByUrl('/livreur');
        },
        error: (err: HttpErrorResponse) => {
          this.submitting.set(false);
          this.serverError.set(err.status === 409 ? 'Ce login est déjà utilisé.' : 'Une erreur est survenue, réessayez.');
        },
      });
  }
}
