import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ShopService } from '../../../core/shop/shop.service';
import { PayoutAccountResponse, ShopDeliveryTier, ShopResponse } from '../../../core/shop/shop.model';
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

  readonly deliveryTiers = signal<ShopDeliveryTier[]>([]);
  readonly tiersSaving = signal(false);
  readonly tiersSaved = signal(false);
  readonly tiersError = signal<string | null>(null);

  constructor() {
    this.shopService.getMyShop().subscribe({
      next: (shop) => {
        this.shop.set(shop);
        this.hasShop.set(true);
        this.loading.set(false);
        if (shop.status === 'VALIDEE') {
          this.loadPayoutAccount();
          this.loadDeliveryTiers();
        }
      },
      error: () => {
        this.hasShop.set(false);
        this.loading.set(false);
      },
    });
  }

  private loadDeliveryTiers(): void {
    this.shopService.getMyDeliveryTiers().subscribe({ next: (tiers) => this.deliveryTiers.set(tiers) });
  }

  addTierRow(): void {
    this.deliveryTiers.update((tiers) => [...tiers, { maxDistanceKm: 10, price: 0 }]);
  }

  removeTierRow(index: number): void {
    this.deliveryTiers.update((tiers) => tiers.filter((_, i) => i !== index));
  }

  updateTierDistance(index: number, value: string): void {
    const n = Number(value);
    this.deliveryTiers.update((tiers) => tiers.map((t, i) => (i === index ? { ...t, maxDistanceKm: n } : t)));
  }

  updateTierPrice(index: number, value: string): void {
    const n = Number(value);
    this.deliveryTiers.update((tiers) => tiers.map((t, i) => (i === index ? { ...t, price: n } : t)));
  }

  saveDeliveryTiers(): void {
    this.tiersSaving.set(true);
    this.tiersSaved.set(false);
    this.tiersError.set(null);
    this.shopService.saveMyDeliveryTiers(this.deliveryTiers()).subscribe({
      next: (tiers) => {
        this.deliveryTiers.set(tiers);
        this.tiersSaving.set(false);
        this.tiersSaved.set(true);
      },
      error: (err: HttpErrorResponse) => {
        this.tiersSaving.set(false);
        this.tiersError.set(err.error?.message ?? 'Impossible d’enregistrer les paliers de livraison.');
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
