import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { ProductService } from '../../../core/product/product.service';
import { ProductResponse } from '../../../core/product/product.model';
import { FieldError } from '../../../shared/ui/field-error/field-error';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-product-detail',
  imports: [RouterLink, ReactiveFormsModule, FieldError],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.scss',
})
export class ProductDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly productService = inject(ProductService);
  private readonly fb = inject(FormBuilder);

  private readonly id = Number(this.route.snapshot.paramMap.get('id'));

  readonly product = signal<ProductResponse | null>(null);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly showRejectForm = signal(false);
  readonly apiFilesBase = `${environment.apiBaseUrl}/files/`;

  readonly rejectForm = this.fb.nonNullable.group({
    reason: ['', [Validators.required]],
  });

  constructor() {
    this.productService.getAdminDetail(this.id).subscribe({
      next: (p) => {
        this.product.set(p);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  validate(): void {
    this.saving.set(true);
    this.productService.validate(this.id).subscribe({
      next: () => this.router.navigateByUrl('/admin/produits'),
      error: () => this.saving.set(false),
    });
  }

  reject(): void {
    if (this.rejectForm.invalid) {
      this.rejectForm.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.productService.reject(this.id, this.rejectForm.getRawValue().reason).subscribe({
      next: () => this.router.navigateByUrl('/admin/produits'),
      error: (err: HttpErrorResponse) => this.saving.set(false),
    });
  }

  togglePublishState(): void {
    this.saving.set(true);
    this.productService.adminTogglePublishState(this.id).subscribe({
      next: (updated) => {
        this.product.set(updated);
        this.saving.set(false);
      },
      error: () => this.saving.set(false),
    });
  }
}
