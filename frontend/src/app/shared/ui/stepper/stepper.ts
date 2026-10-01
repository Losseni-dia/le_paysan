import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-stepper',
  imports: [],
  templateUrl: './stepper.html',
  styleUrl: './stepper.scss',
})
export class Stepper {
  /** Libellés des étapes (déjà traduits par le composant parent). */
  @Input({ required: true }) steps: string[] = [];
  @Input() currentIndex = 0;
  @Input() canGoNext = true;

  @Output() currentIndexChange = new EventEmitter<number>();
  @Output() completed = new EventEmitter<void>();

  get isFirst(): boolean {
    return this.currentIndex === 0;
  }

  get isLast(): boolean {
    return this.currentIndex === this.steps.length - 1;
  }

  goTo(index: number): void {
    if (index < 0 || index >= this.steps.length) {
      return;
    }
    // On n'autorise le saut en avant que vers une étape déjà atteinte.
    if (index > this.currentIndex) {
      return;
    }
    this.currentIndex = index;
    this.currentIndexChange.emit(this.currentIndex);
  }

  previous(): void {
    this.goTo(this.currentIndex - 1);
  }

  next(): void {
    if (!this.canGoNext) {
      return;
    }
    if (this.isLast) {
      this.completed.emit();
      return;
    }
    this.currentIndex += 1;
    this.currentIndexChange.emit(this.currentIndex);
  }
}
