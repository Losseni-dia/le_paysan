import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../core/auth/auth.service';
import { FieldError } from '../../../shared/ui/field-error/field-error';

function newPasswordsMatchValidator(control: AbstractControl): ValidationErrors | null {
  const newPassword = control.get('newPassword')?.value;
  const confirmNewPassword = control.get('confirmNewPassword')?.value;
  if (!newPassword || !confirmNewPassword || newPassword === confirmNewPassword) {
    return null;
  }
  control.get('confirmNewPassword')?.setErrors({
    ...control.get('confirmNewPassword')?.errors,
    passwordMismatch: true,
  });
  return null;
}

@Component({
  selector: 'app-account-settings',
  imports: [ReactiveFormsModule, FieldError],
  templateUrl: './account-settings.html',
  styleUrl: './account-settings.scss',
})
export class AccountSettings {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  readonly currentUser = this.authService.currentUser;

  readonly savingProfile = signal(false);
  readonly profileSaved = signal(false);
  readonly profileError = signal<string | null>(null);

  readonly changingPassword = signal(false);
  readonly passwordChanged = signal(false);
  readonly passwordError = signal<string | null>(null);

  readonly showCurrentPassword = signal(false);
  readonly showNewPassword = signal(false);
  readonly showConfirmNewPassword = signal(false);

  readonly profileForm = this.fb.nonNullable.group({
    firstName: [this.currentUser()?.firstName ?? '', [Validators.required]],
    lastName: [this.currentUser()?.lastName ?? '', [Validators.required]],
    email: [this.currentUser()?.email ?? '', [Validators.email]],
    phone: [this.currentUser()?.phone ?? ''],
  });

  readonly passwordForm = this.fb.nonNullable.group(
    {
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[0-9])(?=.*[A-Za-z]).+$/)]],
      confirmNewPassword: ['', [Validators.required]],
    },
    { validators: newPasswordsMatchValidator },
  );

  readonly newPasswordMessages = {
    minlength: 'Le mot de passe doit contenir au moins 8 caractères.',
    pattern: 'Le mot de passe doit contenir au moins une lettre et un chiffre.',
  };
  readonly confirmNewPasswordMessages = {
    passwordMismatch: 'Les mots de passe ne correspondent pas.',
  };

  toggleShowCurrentPassword(): void {
    this.showCurrentPassword.update((v) => !v);
  }

  toggleShowNewPassword(): void {
    this.showNewPassword.update((v) => !v);
  }

  toggleShowConfirmNewPassword(): void {
    this.showConfirmNewPassword.update((v) => !v);
  }

  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }
    const v = this.profileForm.getRawValue();
    this.savingProfile.set(true);
    this.profileSaved.set(false);
    this.profileError.set(null);
    this.authService
      .updateProfile({
        firstName: v.firstName,
        lastName: v.lastName,
        email: v.email || undefined,
        phone: v.phone || undefined,
      })
      .subscribe({
        next: () => {
          this.savingProfile.set(false);
          this.profileSaved.set(true);
        },
        error: (err: HttpErrorResponse) => {
          this.savingProfile.set(false);
          this.profileError.set(err.error?.message ?? 'Une erreur est survenue, réessayez.');
        },
      });
  }

  changePassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }
    const v = this.passwordForm.getRawValue();
    this.changingPassword.set(true);
    this.passwordChanged.set(false);
    this.passwordError.set(null);
    this.authService.changePassword(v.currentPassword, v.newPassword).subscribe({
      next: () => {
        this.changingPassword.set(false);
        this.passwordChanged.set(true);
        this.passwordForm.reset();
      },
      error: (err: HttpErrorResponse) => {
        this.changingPassword.set(false);
        this.passwordError.set(
          err.status === 400 ? 'Mot de passe actuel incorrect.' : 'Une erreur est survenue, réessayez.',
        );
      },
    });
  }
}
