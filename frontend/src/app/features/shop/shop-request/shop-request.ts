import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { Stepper } from '../../../shared/ui/stepper/stepper';
import { MapPicker } from '../../../shared/ui/map-picker/map-picker';
import { FieldError } from '../../../shared/ui/field-error/field-error';
import { ShopService } from '../../../core/shop/shop.service';
import { OwnerType } from '../../../core/shop/shop.model';

const STEP_LABELS = ['Type', 'Informations boutique', 'Justificatifs', 'Récapitulatif'];

@Component({
  selector: 'app-shop-request',
  imports: [ReactiveFormsModule, Stepper, MapPicker, FieldError],
  templateUrl: './shop-request.html',
  styleUrl: './shop-request.scss',
})
export class ShopRequest {
  private readonly fb = inject(FormBuilder);
  private readonly shopService = inject(ShopService);
  private readonly router = inject(Router);

  readonly stepLabels = STEP_LABELS;
  readonly currentStepIndex = signal(0);
  readonly submitting = signal(false);
  readonly serverError = signal<string | null>(null);

  readonly position = signal<{ latitude: number; longitude: number } | null>(null);

  readonly logoFile = signal<File | null>(null);
  readonly coverFile = signal<File | null>(null);
  readonly idDocumentFrontFile = signal<File | null>(null);
  readonly idDocumentBackFile = signal<File | null>(null);
  readonly selfieWithIdFile = signal<File | null>(null);
  readonly recepisseDocumentFile = signal<File | null>(null);
  readonly legalDocumentFile = signal<File | null>(null);
  readonly legalRepIdDocumentFrontFile = signal<File | null>(null);
  readonly legalRepIdDocumentBackFile = signal<File | null>(null);
  readonly legalRepSelfieWithIdFile = signal<File | null>(null);

  readonly form = this.fb.nonNullable.group({
    ownerType: ['' as OwnerType | '', [Validators.required]],
    name: ['', [Validators.required]],
    description: [''],
    category: [''],
    addressCity: ['', [Validators.required]],
    addressDetail: ['', [Validators.required]],
    phone: ['', [Validators.required]],
    acceptCgv: [false, [Validators.requiredTrue]],
    // Personne physique / Entreprenant
    firstName: [''],
    lastName: [''],
    birthDate: [''],
    idDocumentType: [''],
    idDocumentNumber: [''],
    // Entreprenant uniquement
    recepisseNumber: [''],
    recepisseDate: [''],
    // Personne morale
    companyName: [''],
    legalForm: [''],
    rccmNumber: [''],
    nccNumber: [''],
    legalRepName: [''],
    legalRepRole: [''],
    legalRepIdDocumentType: [''],
    legalRepIdDocumentNumber: [''],
  });

  get ownerType(): string {
    return this.form.controls.ownerType.value;
  }

  selectOwnerType(type: OwnerType): void {
    this.form.controls.ownerType.setValue(type);
  }

  onFileSelected(event: Event, target: ReturnType<typeof signal<File | null>>): void {
    const input = event.target as HTMLInputElement;
    target.set(input.files?.[0] ?? null);
  }

  onPositionChange(pos: { latitude: number; longitude: number }): void {
    this.position.set(pos);
  }

  canGoNext(): boolean {
    switch (this.currentStepIndex()) {
      case 0:
        return !!this.ownerType;
      case 1:
        return (
          this.form.controls.name.valid &&
          this.form.controls.addressCity.valid &&
          this.form.controls.addressDetail.valid &&
          this.form.controls.phone.valid &&
          !!this.logoFile() &&
          !!this.position()
        );
      case 2:
        return this.justificatifsValid();
      case 3:
        return this.form.controls.acceptCgv.value === true;
      default:
        return true;
    }
  }

  private justificatifsValid(): boolean {
    const v = this.form.getRawValue();
    const identityOk =
      !!v.firstName && !!v.lastName && !!v.birthDate && !!v.idDocumentType && !!v.idDocumentNumber &&
      !!this.idDocumentFrontFile() && !!this.selfieWithIdFile();

    if (this.ownerType === 'PHYSIQUE') {
      return identityOk;
    }
    if (this.ownerType === 'ENTREPRENANT') {
      return identityOk && !!v.recepisseNumber && !!v.recepisseDate && !!this.recepisseDocumentFile();
    }
    if (this.ownerType === 'MORALE') {
      return (
        !!v.companyName && !!v.legalForm && !!v.rccmNumber && !!v.nccNumber && !!this.legalDocumentFile() &&
        !!v.legalRepName && !!v.legalRepRole && !!v.legalRepIdDocumentType && !!v.legalRepIdDocumentNumber &&
        !!this.legalRepIdDocumentFrontFile() && !!this.legalRepSelfieWithIdFile()
      );
    }
    return false;
  }

  submit(): void {
    if (!this.canGoNext()) {
      this.form.markAllAsTouched();
      return;
    }
    const pos = this.position();
    const logo = this.logoFile();
    if (!pos || !logo) {
      return;
    }

    const v = this.form.getRawValue();
    const data = new FormData();
    data.append('ownerType', v.ownerType);
    data.append('name', v.name);
    if (v.description) data.append('description', v.description);
    if (v.category) data.append('category', v.category);
    data.append('addressCity', v.addressCity);
    data.append('addressDetail', v.addressDetail);
    data.append('latitude', String(pos.latitude));
    data.append('longitude', String(pos.longitude));
    data.append('phone', v.phone);
    data.append('logo', logo);
    if (this.coverFile()) data.append('cover', this.coverFile()!);
    data.append('acceptCgv', String(v.acceptCgv));

    if (v.ownerType === 'PHYSIQUE' || v.ownerType === 'ENTREPRENANT') {
      data.append('firstName', v.firstName);
      data.append('lastName', v.lastName);
      data.append('birthDate', v.birthDate);
      data.append('idDocumentType', v.idDocumentType);
      data.append('idDocumentNumber', v.idDocumentNumber);
      data.append('idDocumentFront', this.idDocumentFrontFile()!);
      if (this.idDocumentBackFile()) data.append('idDocumentBack', this.idDocumentBackFile()!);
      data.append('selfieWithId', this.selfieWithIdFile()!);
    }
    if (v.ownerType === 'ENTREPRENANT') {
      data.append('recepisseNumber', v.recepisseNumber);
      data.append('recepisseDate', v.recepisseDate);
      data.append('recepisseDocument', this.recepisseDocumentFile()!);
    }
    if (v.ownerType === 'MORALE') {
      data.append('companyName', v.companyName);
      data.append('legalForm', v.legalForm);
      data.append('rccmNumber', v.rccmNumber);
      data.append('nccNumber', v.nccNumber);
      data.append('legalDocument', this.legalDocumentFile()!);
      data.append('legalRepName', v.legalRepName);
      data.append('legalRepRole', v.legalRepRole);
      data.append('legalRepIdDocumentType', v.legalRepIdDocumentType);
      data.append('legalRepIdDocumentNumber', v.legalRepIdDocumentNumber);
      data.append('legalRepIdDocumentFront', this.legalRepIdDocumentFrontFile()!);
      if (this.legalRepIdDocumentBackFile()) {
        data.append('legalRepIdDocumentBack', this.legalRepIdDocumentBackFile()!);
      }
      data.append('legalRepSelfieWithId', this.legalRepSelfieWithIdFile()!);
    }

    this.submitting.set(true);
    this.serverError.set(null);
    this.shopService.submitRequest(data).subscribe({
      next: () => {
        this.submitting.set(false);
        this.router.navigateByUrl('/vendeur/ma-boutique');
      },
      error: (err: HttpErrorResponse) => {
        this.submitting.set(false);
        this.serverError.set(err.error?.message ?? 'Une erreur est survenue, réessayez.');
      },
    });
  }
}
