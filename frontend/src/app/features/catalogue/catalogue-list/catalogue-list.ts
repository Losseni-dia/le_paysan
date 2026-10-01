import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { ProductService } from '../../../core/product/product.service';
import { ProductResponse } from '../../../core/product/product.model';
import { CategoryService } from '../../../core/category/category.service';
import { CategoryResponse } from '../../../core/category/category.model';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-catalogue-list',
  imports: [ReactiveFormsModule, RouterLink],
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

  readonly filterForm = this.fb.nonNullable.group({
    search: [''],
    categoryId: [null as number | null],
    minPrice: [null as number | null],
    maxPrice: [null as number | null],
    deliveryMode: [''],
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
    this.productService
      .listPublic({
        search: v.search || undefined,
        categoryId: v.categoryId ?? undefined,
        minPrice: v.minPrice ?? undefined,
        maxPrice: v.maxPrice ?? undefined,
        deliveryMode: v.deliveryMode || undefined,
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
