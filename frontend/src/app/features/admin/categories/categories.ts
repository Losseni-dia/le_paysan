import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { CategoryService } from '../../../core/category/category.service';
import { CategoryResponse } from '../../../core/category/category.model';
import { FieldError } from '../../../shared/ui/field-error/field-error';

@Component({
  selector: 'app-categories',
  imports: [ReactiveFormsModule, FieldError],
  templateUrl: './categories.html',
  styleUrl: './categories.scss',
})
export class Categories {
  private readonly fb = inject(FormBuilder);
  private readonly categoryService = inject(CategoryService);

  readonly categories = signal<CategoryResponse[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly serverError = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
  });

  constructor() {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.categoryService.list().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  create(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.serverError.set(null);
    this.categoryService.create(this.form.getRawValue().name).subscribe({
      next: (category) => {
        this.categories.update((list) => [...list, category]);
        this.form.reset();
        this.saving.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.saving.set(false);
        this.serverError.set(err.error?.message ?? 'Une erreur est survenue, réessayez.');
      },
    });
  }
}
