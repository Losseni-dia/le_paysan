import { ApplicationConfig, inject, provideAppInitializer, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideTranslateService } from '@ngx-translate/core';
import { provideTranslateHttpLoader } from '@ngx-translate/http-loader';
import { firstValueFrom, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { routes } from './app.routes';
import { authInterceptor } from './core/auth/auth.interceptor';
import { AuthService } from './core/auth/auth.service';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([authInterceptor])),
    // Attend la restauration de session (refresh token -> access token) avant
    // que le routeur n'evalue les guards, pour eviter qu'un rechargement de
    // page sur une route protegee redirige a tort vers /connexion.
    provideAppInitializer(() => {
      const authService = inject(AuthService);
      const restore = authService.tryRestoreSession();
      if (!restore) {
        return undefined;
      }
      return firstValueFrom(restore.pipe(catchError(() => of(null))));
    }),
    provideTranslateService({
      loader: provideTranslateHttpLoader({ prefix: '/i18n/', suffix: '.json' }),
      fallbackLang: 'fr',
      lang: 'fr'
    })
  ]
};
