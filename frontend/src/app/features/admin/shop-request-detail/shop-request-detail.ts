import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { ShopService } from '../../../core/shop/shop.service';
import { ShopAdminDetailResponse } from '../../../core/shop/shop.model';
import { FieldError } from '../../../shared/ui/field-error/field-error';
import { MapPicker } from '../../../shared/ui/map-picker/map-picker';
import { SecureDocument } from '../../../shared/ui/secure-document/secure-document';

@Component({
  selector: 'app-shop-request-detail',
  imports: [RouterLink, ReactiveFormsModule, FieldError, MapPicker, SecureDocument],
  templateUrl: './shop-request-detail.html',
  styleUrl: './shop-request-detail.scss',
})
export class ShopRequestDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly shopService = inject(ShopService);
  private readonly fb = inject(FormBuilder);

  private readonly id = Number(this.route.snapshot.paramMap.get('id'));

  readonly detail = signal<ShopAdminDetailResponse | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly showRejectForm = signal(false);

  readonly rejectForm = this.fb.nonNullable.group({
    reason: ['', [Validators.required]],
  });

  constructor() {
    this.shopService.getAdminDetail(this.id).subscribe({
      next: (d) => {
        this.detail.set(d);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  validate(): void {
    this.saving.set(true);
    this.shopService.validate(this.id).subscribe({
      next: () => this.router.navigateByUrl('/admin/demandes-boutiques'),
      error: () => this.saving.set(false),
    });
  }

  reject(): void {
    if (this.rejectForm.invalid) {
      this.rejectForm.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.shopService.reject(this.id, this.rejectForm.getRawValue().reason).subscribe({
      next: () => this.router.navigateByUrl('/admin/demandes-boutiques'),
      error: (err: HttpErrorResponse) => this.saving.set(false),
    });
  }
}
