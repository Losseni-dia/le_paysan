import { Component, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ProductService } from '../../../core/product/product.service';
import { ProductResponse } from '../../../core/product/product.model';
import { CategoryService } from '../../../core/category/category.service';
import { CategoryResponse } from '../../../core/category/category.model';
import { CI_CITIES } from '../../../core/catalogue/ci-cities';
import { environment } from '../../../../environments/environment';
import { StarRating } from '../../../shared/ui/star-rating/star-rating';

@Component({
  selector: 'app-catalogue-list',
  imports: [ReactiveFormsModule, RouterLink, DecimalPipe, StarRating],
  templateUrl: './catalogue-list.html',
  styleUrl: './catalogue-list.scss',
})
export class CatalogueList {
  private readonly fb = inject(FormBuilder);
  private readonly productService = inject(ProductService);
  private readonly categoryService = inject(CategoryService);

  readonly categories = signal<CategoryResponse[]>([]);
  readonly products = signal<ProductResponse[]>([]);
  readonly loading = signal(true);
  readonly page = signal(0);
  readonly totalPages = signal(0);
  readonly apiFilesBase = `${environment.apiBaseUrl}/files/`;
  readonly cities = CI_CITIES;

  readonly filterForm = this.fb.nonNullable.group({
    search: [''],
    categoryId: [null as number | null],
    deliveryMode: [''],
    city: [''],
  });

  constructor() {
    this.categoryService.list().subscribe((categories) => this.categories.set(categories));

    this.filterForm.valueChanges.pipe(debounceTime(300), takeUntilDestroyed()).subscribe(() => {
      this.page.set(0);
      this.load();
    });

    this.load();
  }

  private load(): void {
    this.loading.set(true);
    const v = this.filterForm.getRawValue();
    const selectedCity = this.cities.find((c) => c.name === v.city);
    this.productService
      .listPublic({
        search: v.search || undefined,
        categoryId: v.categoryId ?? undefined,
        deliveryMode: v.deliveryMode || undefined,
        refLat: selectedCity?.lat,
        refLng: selectedCity?.lng,
        page: this.page(),
      })
      .subscribe({
        next: (result) => {
          this.products.set(result.content);
          this.totalPages.set(result.totalPages);
          this.loading.set(false);
        },
        error: () => this.loading.set(false),
      });
  }

  goToPage(page: number): void {
    if (page < 0 || page >= this.totalPages()) {
      return;
    }
    this.page.set(page);
    this.load();
  }
}
