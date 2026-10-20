import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../core/auth/auth.service';
import { ContactService } from '../../core/contact/contact.service';
import { FieldError } from '../../shared/ui/field-error/field-error';

@Component({
  selector: 'app-contact',
  imports: [ReactiveFormsModule, FieldError],
  templateUrl: './contact.html',
  styleUrl: './contact.scss',
})
export class Contact {
  private readonly fb = inject(FormBuilder);
  private readonly contactService = inject(ContactService);
  private readonly authService = inject(AuthService);

  readonly submitting = signal(false);
  readonly submitted = signal(false);
  readonly serverError = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    fullName: [this.currentUserFullName(), [Validators.required]],
    email: [this.authService.currentUser()?.email ?? '', [Validators.required, Validators.email]],
    subject: ['', [Validators.required]],
    message: ['', [Validators.required]],
  });

  private currentUserFullName(): string {
    const user = this.authService.currentUser();
    return user ? `${user.firstName} ${user.lastName}` : '';
  }

  submit(): void {
    this.serverError.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.submitting.set(true);
    this.contactService.submit(this.form.getRawValue()).subscribe({
      next: () => {
        this.submitting.set(false);
        this.submitted.set(true);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.serverError.set(err.error?.message ?? 'Une erreur est survenue, veuillez réessayer.');
      },
    });
  }
}
