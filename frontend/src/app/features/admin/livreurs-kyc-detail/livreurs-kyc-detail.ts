import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { LivreurService } from '../../../core/livreur/livreur.service';
import { LivreurAdminResponse } from '../../../core/livreur/livreur.model';
import { FieldError } from '../../../shared/ui/field-error/field-error';
import { SecureDocument } from '../../../shared/ui/secure-document/secure-document';

@Component({
  selector: 'app-livreurs-kyc-detail',
  imports: [RouterLink, ReactiveFormsModule, FieldError, SecureDocument],
  templateUrl: './livreurs-kyc-detail.html',
  styleUrl: './livreurs-kyc-detail.scss',
})
export class LivreursKycDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly livreurService = inject(LivreurService);
  private readonly fb = inject(FormBuilder);

  private readonly id = Number(this.route.snapshot.paramMap.get('id'));

  readonly detail = signal<LivreurAdminResponse | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal<string | null>(null);
  readonly showRejectForm = signal(false);

  readonly rejectForm = this.fb.nonNullable.group({
    reason: ['', [Validators.required]],
  });

  constructor() {
    this.livreurService.getAdminDetail(this.id).subscribe({
      next: (d) => {
        this.detail.set(d);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  validate(): void {
    this.saving.set(true);
    this.error.set(null);
    this.livreurService.validateKyc(this.id).subscribe({
      next: () => this.router.navigateByUrl('/admin/livreurs-kyc'),
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Impossible de valider ce livreur.');
      },
    });
  }

  reject(): void {
    if (this.rejectForm.invalid) {
      this.rejectForm.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);
    this.livreurService.rejectKyc(this.id, this.rejectForm.getRawValue().reason).subscribe({
      next: () => this.router.navigateByUrl('/admin/livreurs-kyc'),
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Impossible de rejeter ce livreur.');
      },
    });
  }
}
