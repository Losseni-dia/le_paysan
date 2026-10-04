import { Component, ElementRef, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { LivreurService } from '../../../core/livreur/livreur.service';
import { AdminOrderResponse } from '../../../core/order/order.model';
import { SignaturePad } from '../../../shared/ui/signature-pad/signature-pad';

type ProofMode = 'SIGNATURE' | 'PHOTO';

@Component({
  selector: 'app-delivery-confirm',
  imports: [RouterLink, SignaturePad],
  templateUrl: './delivery-confirm.html',
  styleUrl: './delivery-confirm.scss',
})
export class DeliveryConfirm {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly livreurService = inject(LivreurService);

  private readonly signaturePad = viewChild<SignaturePad>('signaturePad');
  private readonly photoInput = viewChild<ElementRef<HTMLInputElement>>('photoInput');

  readonly order = signal<AdminOrderResponse | null>(null);
  readonly loading = signal(true);
  readonly mode = signal<ProofMode>('SIGNATURE');
  readonly photoFile = signal<File | null>(null);
  readonly photoPreviewUrl = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  constructor() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.livreurService.getDelivery(id).subscribe({
      next: (order) => {
        this.order.set(order);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  setMode(mode: ProofMode): void {
    this.mode.set(mode);
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

  async submit(): Promise<void> {
    const order = this.order();
    if (!order) return;

    this.error.set(null);

    let proof: Blob | null = null;
    if (this.mode() === 'SIGNATURE') {
      const pad = this.signaturePad();
      if (!pad || !pad.hasSignature()) {
        this.error.set('Faites signer le client avant de valider.');
        return;
      }
      proof = await pad.toBlob();
    } else {
      proof = this.photoFile();
      if (!proof) {
        this.error.set('Prenez une photo du colis avant de valider.');
        return;
      }
    }

    if (!proof) {
      this.error.set("Impossible de récupérer la preuve, réessayez.");
      return;
    }

    this.submitting.set(true);
    this.livreurService.confirmDelivery(order.id, this.mode(), proof).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigateByUrl('/livreur/livraisons');
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.error.set(err.error?.message ?? 'Impossible de confirmer la remise.');
      },
    });
  }
}
