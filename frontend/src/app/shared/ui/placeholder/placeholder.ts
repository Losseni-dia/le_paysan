import { Component, Input } from '@angular/core';

/** Page provisoire — à remplacer au fur et à mesure des phases du plan d'exécution. */
@Component({
  selector: 'app-placeholder',
  imports: [],
  templateUrl: './placeholder.html',
  styleUrl: './placeholder.scss',
})
export class Placeholder {
  @Input() title = '';
}
