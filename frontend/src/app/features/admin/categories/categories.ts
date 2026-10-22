import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { CategoryService } from '../../../core/category/category.service';
import { CategoryResponse } from '../../../core/category/category.model';
import { FieldError } from '../../../shared/ui/field-error/field-error';

@Component({
  selector: 'app-categories',
  imports: [ReactiveFormsModule, FieldError, FormsModule],
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
  readonly searchQuery = signal('');

  readonly filteredCategories = computed(() => {
    const q = this.searchQuery().trim().toLowerCase();
    if (!q) return this.categories();
    return this.categories().filter((c) => c.name.toLowerCase().includes(q));
  });

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required]],
  });

  readonly editingId = signal<number | null>(null);
  readonly editName = signal('');
  readonly editSaving = signal(false);
  readonly editError = signal<string | null>(null);
  readonly deletingId = signal<number | null>(null);

  constructor() {
    this.load();
  }

  private load(): void {
    this.loading.set(true);
    this.categoryService.listForAdmin().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  validate(category: CategoryResponse): void {
    this.categoryService.validate(category.id).subscribe({
      next: (updated) => {
        this.categories.update((list) => list.map((c) => (c.id === updated.id ? updated : c)));
      },
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

  startEdit(category: CategoryResponse): void {
    this.editingId.set(category.id);
    this.editName.set(category.name);
    this.editError.set(null);
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  saveEdit(category: CategoryResponse): void {
    const name = this.editName().trim();
    if (!name) {
      this.editError.set('Le nom est obligatoire.');
      return;
    }
    this.editSaving.set(true);
    this.editError.set(null);
    this.categoryService.update(category.id, name).subscribe({
      next: (updated) => {
        this.categories.update((list) => list.map((c) => (c.id === updated.id ? updated : c)));
        this.editSaving.set(false);
        this.editingId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        this.editSaving.set(false);
        this.editError.set(err.error?.message ?? 'Impossible de modifier cette catégorie.');
      },
    });
  }

  remove(category: CategoryResponse): void {
    if (!confirm(`Supprimer la catégorie "${category.name}" ?`)) {
      return;
    }
    this.deletingId.set(category.id);
    this.serverError.set(null);
    this.categoryService.remove(category.id).subscribe({
      next: () => {
        this.categories.update((list) => list.filter((c) => c.id !== category.id));
        this.deletingId.set(null);
      },
      error: (err: HttpErrorResponse) => {
        this.deletingId.set(null);
        this.serverError.set(err.error?.message ?? 'Impossible de supprimer cette catégorie.');
      },
    });
  }
}
