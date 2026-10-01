import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';

import { LanguageSwitcher } from '../../shared/ui/language-switcher/language-switcher';
import { NotificationBell } from '../../shared/ui/notification-bell/notification-bell';

@Component({
  selector: 'app-public-layout',
  imports: [RouterOutlet, RouterLink, TranslatePipe, LanguageSwitcher, NotificationBell],
  templateUrl: './public-layout.html',
  styleUrl: './public-layout.scss',
})
export class PublicLayout {}
