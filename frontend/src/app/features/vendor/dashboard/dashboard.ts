import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { VendorDashboardService } from '../../../core/vendor/vendor-dashboard.service';
import { VendorDashboardResponse } from '../../../core/vendor/vendor-dashboard.model';

@Component({
  selector: 'app-vendor-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly dashboardService = inject(VendorDashboardService);

  readonly data = signal<VendorDashboardResponse | null>(null);
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
