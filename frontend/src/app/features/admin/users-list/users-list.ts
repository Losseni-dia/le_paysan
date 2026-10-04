import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { AdminUserService } from '../../../core/admin/admin-user.service';
import { AdminUserResponse } from '../../../core/admin/admin-user.model';

const ROLE_FILTERS = [
  { value: '', label: 'Tous' },
  { value: 'ROLE_USER', label: 'Utilisateurs' },
  { value: 'ROLE_VENDEUR', label: 'Vendeurs' },
  { value: 'ROLE_ADMIN', label: 'Admins' },
];

const ALL_ROLES = ['ROLE_USER', 'ROLE_VENDEUR', 'ROLE_ADMIN'];

@Component({
  selector: 'app-users-list',
  imports: [DatePipe, FormsModule],
  templateUrl: './users-list.html',
  styleUrl: './users-list.scss',
})
export class UsersList {
  private readonly userService = inject(AdminUserService);

  readonly roleFilters = ROLE_FILTERS;
  readonly allRoles = ALL_ROLES;
  readonly selectedRole = signal('');
  readonly search = signal('');
  readonly users = signal<AdminUserResponse[]>([]);
  readonly loading = signal(true);
  readonly editingRolesId = signal<number | null>(null);
  readonly editingRoles = signal<string[]>([]);
  readonly error = signal<string | null>(null);

  constructor() {
    this.load();
  }

  selectRole(role: string): void {
    this.selectedRole.set(role);
    this.load();
  }

  onSearch(value: string): void {
    this.search.set(value);
    this.load();
  }

  toggleEnabled(user: AdminUserResponse): void {
    this.error.set(null);
    this.userService.setEnabled(user.id, !user.enabled).subscribe({
      next: (updated) => this.replace(updated),
      error: (err) => this.error.set(err.error?.message ?? "Impossible d'effectuer cette action."),
    });
  }

  startEditRoles(user: AdminUserResponse): void {
    this.editingRolesId.set(user.id);
    this.editingRoles.set([...user.roles]);
  }

  cancelEditRoles(): void {
    this.editingRolesId.set(null);
  }

  toggleRoleCheckbox(role: string): void {
    const current = this.editingRoles();
    this.editingRoles.set(
      current.includes(role) ? current.filter((r) => r !== role) : [...current, role],
    );
  }

  saveRoles(userId: number): void {
    this.error.set(null);
    this.userService.setRoles(userId, this.editingRoles()).subscribe({
      next: (updated) => {
        this.replace(updated);
        this.editingRolesId.set(null);
      },
      error: (err) => this.error.set(err.error?.message ?? 'Impossible de mettre à jour les rôles.'),
    });
  }

  private replace(updated: AdminUserResponse): void {
    this.users.set(this.users().map((u) => (u.id === updated.id ? updated : u)));
  }

  private load(): void {
    this.loading.set(true);
    this.userService.list(this.search() || undefined, this.selectedRole() || undefined).subscribe({
      next: (page) => {
        this.users.set(page.content);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
