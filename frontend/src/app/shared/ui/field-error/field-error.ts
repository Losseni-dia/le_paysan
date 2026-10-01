import { Component, Input } from '@angular/core';
import { AbstractControl } from '@angular/forms';

/**
 * Message d'erreur clair affiche sous un champ de formulaire, uniquement
 * une fois le champ touche. Mapping des cles de validateurs Angular vers
 * des messages en francais.
 */
@Component({
  selector: 'app-field-error',
  imports: [],
  templateUrl: './field-error.html',
  styleUrl: './field-error.scss',
})
export class FieldError {
  @Input({ required: true }) control!: AbstractControl | null;
  /** Message personnalise pour une cle de validateur donnee (ex. {pattern: "..."}). */
  @Input() messages: Record<string, string> = {};

  private readonly defaultMessages: Record<string, string> = {
    required: 'Ce champ est obligatoire.',
    email: 'Adresse email invalide.',
    minlength: 'Trop court.',
    maxlength: 'Trop long.',
    pattern: 'Format invalide.',
    requiredTrue: 'Ce champ doit etre coche.',
  };

  get shouldShow(): boolean {
    return !!this.control && this.control.invalid && (this.control.touched || this.control.dirty);
  }

  get message(): string {
    if (!this.control?.errors) {
      return '';
    }
    const firstKey = Object.keys(this.control.errors)[0];
    return this.messages[firstKey] ?? this.defaultMessages[firstKey] ?? 'Valeur invalide.';
  }
}
