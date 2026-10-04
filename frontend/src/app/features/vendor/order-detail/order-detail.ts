import { Component, ElementRef, computed, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { OrderService } from '../../../core/order/order.service';
import { AdminOrderResponse } from '../../../core/order/order.model';
import { LivreurService } from '../../../core/livreur/livreur.service';
import { LivreurResponse } from '../../../core/livreur/livreur.model';
import { OrderTimeline } from '../../../shared/ui/order-timeline/order-timeline';
import { SignaturePad } from '../../../shared/ui/signature-pad/signature-pad';

type ProofMode = 'NONE' | 'SIGNATURE' | 'PHOTO';

@Component({
  selector: 'app-vendor-order-detail',
  imports: [RouterLink, DatePipe, FormsModule, OrderTimeline, SignaturePad],
  templateUrl: './order-detail.html',
  styleUrl: './order-detail.scss',
})
export class OrderDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly orderService = inject(OrderService);
  private readonly livreurService = inject(LivreurService);

  private readonly signaturePad = viewChild<SignaturePad>('signaturePad');
  private readonly photoInput = viewChild<ElementRef<HTMLInputElement>>('photoInput');

  readonly order = signal<AdminOrderResponse | null>(null);
  readonly loading = signal(true);
  readonly updating = signal(false);
  readonly error = signal<string | null>(null);
  readonly livreurs = signal<LivreurResponse[]>([]);
  readonly selectedLivreurId = signal<number | null>(null);
  readonly proofMode = signal<ProofMode>('NONE');
  readonly photoFile = signal<File | null>(null);
  readonly photoPreviewUrl = signal<string | null>(null);

  readonly willBeExpediee = computed(() => {
    const order = this.order();
    if (!order || order.status !== 'EN_PREPARATION') return false;
    return order.items.some((item) => item.deliveryMode === 'LIVRAISON_DOMICILE');
  });

  readonly isDeliveryStep = computed(() => {
    const order = this.order();
    return order?.status === 'PRETE' || order?.status === 'EXPEDIEE';
  });

  readonly nextActionLabel = () => {
    const order = this.order();
    if (!order) return null;
    switch (order.status) {
      case 'PAYEE':
        return 'Marquer en préparation';
      case 'EN_PREPARATION':
        return this.willBeExpediee() ? 'Marquer expédiée' : 'Marquer prête (retrait)';
      case 'PRETE':
      case 'EXPEDIEE':
        return 'Livraison effectuée';
      default:
        return null;
    }
  };

  constructor() {
    this.load();
    this.livreurService.listForShop().subscribe({ next: (livreurs) => this.livreurs.set(livreurs) });
  }

  setProofMode(mode: ProofMode): void {
    this.proofMode.set(mode);
    this.error.set(null);
  }

  onPhotoSelected(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0] ?? null;
    this.photoFile.set(file);
    this.photoPreviewUrl.set(file ? URL.createObjectURL(file) : null);
  }

  triggerPhotoInput(): void {
    this.photoInput()?.nativeElement.click();
  }

  async markNextStatus(): Promise<void> {
    const order = this.order();
    if (!order) return;

    this.error.set(null);

    let proof: Blob | undefined;
    const mode = this.proofMode();
    if (mode === 'SIGNATURE') {
      const pad = this.signaturePad();
      if (pad?.hasSignature()) {
        proof = (await pad.toBlob()) ?? undefined;
      }
    } else if (mode === 'PHOTO') {
      proof = this.photoFile() ?? undefined;
    }

    this.updating.set(true);
    this.orderService
      .markNextStatus(order.id, {
        livreurId: this.selectedLivreurId() ?? undefined,
        proofType: mode !== 'NONE' && proof ? mode : undefined,
        proof,
      })
      .subscribe({
        next: (updated) => {
          this.order.set(updated);
          this.selectedLivreurId.set(null);
          this.proofMode.set('NONE');
          this.photoFile.set(null);
          this.photoPreviewUrl.set(null);
          this.updating.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.updating.set(false);
          this.error.set(err.error?.message ?? 'Impossible de mettre à jour le statut.');
        },
      });
  }

  private load(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.orderService.getForVendor(id).subscribe({
      next: (order) => {
        this.order.set(order);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }
}
