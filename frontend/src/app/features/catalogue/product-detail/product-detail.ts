import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { ProductService } from '../../../core/product/product.service';
import { ProductResponse } from '../../../core/product/product.model';
import { CartService } from '../../../core/cart/cart.service';
import { DeliveryMode } from '../../../core/cart/cart.model';
import { AuthService } from '../../../core/auth/auth.service';
import { FieldError } from '../../../shared/ui/field-error/field-error';
import { MapPicker } from '../../../shared/ui/map-picker/map-picker';
import { ReviewSection } from '../../../shared/ui/review-section/review-section';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-product-detail',
  imports: [RouterLink, ReactiveFormsModule, FieldError, MapPicker, ReviewSection],
  templateUrl: './product-detail.html',
  styleUrl: './product-detail.scss',
})
export class ProductDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly productService = inject(ProductService);
  private readonly cartService = inject(CartService);
  private readonly fb = inject(FormBuilder);
  readonly authService = inject(AuthService);

  readonly product = signal<ProductResponse | null>(null);
  readonly loading = signal(true);
  readonly notFound = signal(false);
  readonly activeImage = signal(0);
  readonly apiFilesBase = `${environment.apiBaseUrl}/files/`;

  readonly addingToCart = signal(false);
  readonly addedToCart = signal(false);
  readonly cartError = signal<string | null>(null);

  readonly addForm = this.fb.nonNullable.group({
    quantity: [1, [Validators.required, Validators.min(1)]],
    deliveryMode: ['' as DeliveryMode | '', [Validators.required]],
    deliveryAddress: [''],
  });

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.productService.getPublic(id).subscribe({
      next: (p) => {
        this.product.set(p);
        this.loading.set(false);
        if (p.deliveryOptions.length === 1) {
          this.addForm.controls.deliveryMode.setValue(p.deliveryOptions[0].mode);
        }
      },
      error: () => {
        this.notFound.set(true);
        this.loading.set(false);
      },
    });
  }

  selectImage(index: number): void {
    this.activeImage.set(index);
  }

  get needsAddress(): boolean {
    return this.addForm.controls.deliveryMode.value === 'LIVRAISON_DOMICILE';
  }

  addToCart(): void {
    const p = this.product();
    if (!p || this.addForm.invalid) {
      this.addForm.markAllAsTouched();
      return;
    }
    const v = this.addForm.getRawValue();
    if (this.needsAddress && !v.deliveryAddress.trim()) {
      this.addForm.controls.deliveryAddress.setErrors({ required: true });
      this.addForm.controls.deliveryAddress.markAsTouched();
      return;
    }

    this.addingToCart.set(true);
    this.addedToCart.set(false);
    this.cartError.set(null);
    this.cartService
      .addItem({
        productId: p.id,
        quantity: v.quantity,
        deliveryMode: v.deliveryMode as DeliveryMode,
        deliveryAddress: this.needsAddress ? v.deliveryAddress : undefined,
      })
      .subscribe({
        next: () => {
          this.addingToCart.set(false);
          this.addedToCart.set(true);
        },
        error: (err: HttpErrorResponse) => {
          this.addingToCart.set(false);
          this.cartError.set(err.error?.message ?? 'Impossible d’ajouter ce produit au panier.');
        },
      });
  }
}
