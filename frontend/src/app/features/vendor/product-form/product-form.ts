import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { Stepper } from '../../../shared/ui/stepper/stepper';
import { FieldError } from '../../../shared/ui/field-error/field-error';
import { ProductService } from '../../../core/product/product.service';
import { CategoryService } from '../../../core/category/category.service';
import { CategoryResponse } from '../../../core/category/category.model';
import { ProductResponse } from '../../../core/product/product.model';
import { environment } from '../../../../environments/environment';

const STEP_LABELS = ['Informations générales', 'Photos', 'Livraison', 'Récapitulatif'];

@Component({
  selector: 'app-product-form',
  imports: [ReactiveFormsModule, Stepper, FieldError],
  templateUrl: './product-form.html',
  styleUrl: './product-form.scss',
})
export class ProductForm {
  private readonly fb = inject(FormBuilder);
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly stepLabels = STEP_LABELS;
  readonly currentStepIndex = signal(0);
  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);
  readonly categories = signal<CategoryResponse[]>([]);
  readonly loading = signal(true);

  readonly addingCategory = signal(false);
  readonly newCategoryName = signal('');
  readonly categorySaving = signal(false);
  readonly categoryError = signal<string | null>(null);

  readonly productId = signal<number | null>(null);
  readonly existingImageUrls = signal<string[]>([]);
  readonly newImages = signal<File[]>([]);
  readonly newImagePreviews = signal<string[]>([]);

  readonly apiFilesBase = `${environment.apiBaseUrl}/files/`;

  readonly form = this.fb.nonNullable.group({
    categoryId: [null as number | null, [Validators.required]],
    name: ['', [Validators.required]],
    description: [''],
    unit: ['', [Validators.required]],
    unitPrice: [null as number | null, [Validators.required, Validators.min(1)]],
    stockQuantity: [null as number | null, [Validators.required, Validators.min(0)]],
    retraitBoutique: [true],
    pickupLocation: [''],
    livraisonDomicile: [false],
    deliveryPrice: [null as number | null],
  });

  constructor() {
    this.categoryService.list().subscribe((categories) => this.categories.set(categories));

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      this.productId.set(id);
      this.productService.listMine().subscribe({
        next: (page) => {
          const product = page.content.find((p) => p.id === id);
          if (product) {
            this.patchFromProduct(product);
          }
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
    } else {
      this.loading.set(false);
    }
  }

  get isEdit(): boolean {
    return this.productId() !== null;
  }

  selectedCategoryName(): string {
    const id = this.form.controls.categoryId.value;
    return this.categories().find((c) => c.id === id)?.name ?? '—';
  }

  toggleAddCategory(): void {
    this.addingCategory.update((v) => !v);
    this.categoryError.set(null);
    this.newCategoryName.set('');
  }

  createCategory(): void {
    const name = this.newCategoryName().trim();
    if (!name) {
      return;
    }
    this.categorySaving.set(true);
    this.categoryError.set(null);
    this.categoryService.suggest(name).subscribe({
      next: (category) => {
        this.categories.update((list) => [...list, category]);
        this.form.controls.categoryId.setValue(category.id);
        this.categorySaving.set(false);
        this.addingCategory.set(false);
        this.newCategoryName.set('');
      },
      error: (err: HttpErrorResponse) => {
        this.categorySaving.set(false);
        this.categoryError.set(err.error?.message ?? 'Impossible de créer cette catégorie.');
      },
    });
  }

  private patchFromProduct(product: ProductResponse): void {
    const retrait = product.deliveryOptions.find((o) => o.mode === 'RETRAIT_BOUTIQUE');
    const livraison = product.deliveryOptions.find((o) => o.mode === 'LIVRAISON_DOMICILE');
    this.form.patchValue({
      categoryId: product.categoryId,
      name: product.name,
      description: product.description ?? '',
      unit: product.unit,
      unitPrice: product.unitPrice,
      stockQuantity: product.stockQuantity,
      retraitBoutique: !!retrait,
      pickupLocation: retrait?.pickupLocation ?? '',
      livraisonDomicile: !!livraison,
      deliveryPrice: livraison?.deliveryPrice ?? null,
    });
    this.existingImageUrls.set(product.imageUrls);
  }

  onImagesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files ?? []);
    this.newImages.set(files);
    this.newImagePreviews.set(files.map((f) => URL.createObjectURL(f)));
  }

  canGoNext(): boolean {
    switch (this.currentStepIndex()) {
      case 0:
        return (
          this.form.controls.categoryId.valid &&
          this.form.controls.name.valid &&
          this.form.controls.unit.valid &&
          this.form.controls.unitPrice.valid &&
          this.form.controls.stockQuantity.valid
        );
      case 1:
        return this.isEdit ? this.existingImageUrls().length + this.newImages().length > 0 : this.newImages().length > 0;
      case 2:
        return this.deliveryValid();
      default:
        return true;
    }
  }

  private deliveryValid(): boolean {
    const v = this.form.getRawValue();
    if (!v.retraitBoutique && !v.livraisonDomicile) {
      return false;
    }
    if (v.livraisonDomicile && (v.deliveryPrice == null || v.deliveryPrice < 0)) {
      return false;
    }
    return true;
  }

  submit(): void {
    if (!this.canGoNext()) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const data = new FormData();
    data.append('categoryId', String(v.categoryId));
    data.append('name', v.name);
    if (v.description) data.append('description', v.description);
    data.append('unit', v.unit);
    data.append('unitPrice', String(v.unitPrice));
    data.append('stockQuantity', String(v.stockQuantity));
    data.append('retraitBoutique', String(v.retraitBoutique));
    if (v.pickupLocation) data.append('pickupLocation', v.pickupLocation);
    data.append('livraisonDomicile', String(v.livraisonDomicile));
    if (v.deliveryPrice != null) data.append('deliveryPrice', String(v.deliveryPrice));
    for (const file of this.newImages()) {
      data.append('images', file);
    }

    this.submitting.set(true);
    this.serverError.set(null);

    const request = this.isEdit
      ? this.productService.update(this.productId()!, data)
      : this.productService.create(data);

    request.subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigateByUrl('/vendeur/produits');
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.serverError.set(err.error?.message ?? 'Une erreur est survenue, réessayez.');
      },
    });
  }
}
