import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { DashboardService } from '../../../core/admin/dashboard.service';
import { DashboardResponse } from '../../../core/admin/dashboard.model';

@Component({
  selector: 'app-admin-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly dashboardService = inject(DashboardService);

  readonly data = signal<DashboardResponse | null>(null);
  readonly loading = signal(true);

  constructor() {
    this.dashboardService.getDashboard().subscribe({
      next: (data) => {
        this.data.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
