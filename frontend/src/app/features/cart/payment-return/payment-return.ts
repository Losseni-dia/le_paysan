import { Component, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-payment-return',
  imports: [RouterLink],
  templateUrl: './payment-return.html',
  styleUrl: './payment-return.scss',
})
export class PaymentReturn {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly status: string | null;

  constructor() {
    this.status = this.route.snapshot.queryParamMap.get('status');
    if (this.status === 'approved') {
      this.router.navigateByUrl('/mon-espace/commandes', { replaceUrl: true });
    }
  }
}
