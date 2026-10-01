import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ShopService } from '../../../core/shop/shop.service';
import { PayoutAccountResponse, ShopResponse } from '../../../core/shop/shop.model';
import { MapPicker } from '../../../shared/ui/map-picker/map-picker';
import { FieldError } from '../../../shared/ui/field-error/field-error';

const STATUS_LABELS: Record<string, string> = {
  EN_ATTENTE: 'En attente de validation',
  VALIDEE: 'Validée',
  REJETEE: 'Rejetée',
  SUSPENDUE: 'Suspendue',
};

@Component({
  selector: 'app-my-shop',
  imports: [ReactiveFormsModule, RouterLink, MapPicker, FieldError],
  templateUrl: './my-shop.html',
  styleUrl: './my-shop.scss',
})
export class MyShop {
  private readonly fb = inject(FormBuilder);
  private readonly shopService = inject(ShopService);

  readonly loading = signal(true);
  readonly hasShop = signal(false);
  readonly shop = signal<ShopResponse | null>(null);
  readonly statusLabels = STATUS_LABELS;

  readonly payoutAccount = signal<PayoutAccountResponse | null>(null);
  readonly payoutSaving = signal(false);
  readonly payoutSaved = signal(false);

  readonly payoutForm = this.fb.nonNullable.group({
    operator: ['', [Validators.required]],
    phoneNumber: ['', [Validators.required]],
    accountHolderName: ['', [Validators.required]],
  });

  constructor() {
    this.shopService.getMyShop().subscribe({
      next: (shop) => {
        this.shop.set(shop);
        this.hasShop.set(true);
        this.loading.set(false);
        if (shop.status === 'VALIDEE') {
          this.loadPayoutAccount();
        }
      },
      error: () => {
        this.hasShop.set(false);
        this.loading.set(false);
      },
    });
  }

  private loadPayoutAccount(): void {
    this.shopService.getPayoutAccount().subscribe({
      next: (account) => {
        this.payoutAccount.set(account);
        this.payoutForm.patchValue(account);
      },
      error: () => void 0, // pas encore de compte renseigné
    });
  }

  savePayoutAccount(): void {
    if (this.payoutForm.invalid) {
      this.payoutForm.markAllAsTouched();
      return;
    }
    this.payoutSaving.set(true);
    this.payoutSaved.set(false);
    this.shopService.upsertPayoutAccount(this.payoutForm.getRawValue()).subscribe({
      next: (account) => {
        this.payoutAccount.set(account);
        this.payoutSaving.set(false);
        this.payoutSaved.set(true);
      },
      error: (err: HttpErrorResponse) => {
        this.payoutSaving.set(false);
      },
    });
  }
}
