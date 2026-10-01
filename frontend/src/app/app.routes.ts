import { Routes } from '@angular/router';

import { Placeholder } from './shared/ui/placeholder/placeholder';
import { roleGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/public-layout/public-layout').then((m) => m.PublicLayout),
    children: [
      { path: '', component: Placeholder, data: { title: 'Catalogue' } },
      { path: 'catalogue', component: Placeholder, data: { title: 'Catalogue' } },
      { path: 'panier', component: Placeholder, data: { title: 'Panier' } },
      {
        path: 'connexion',
        loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
      },
      {
        path: 'inscription',
        loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
      },
      {
        path: 'mot-de-passe-oublie',
        loadComponent: () =>
          import('./features/auth/forgot-password/forgot-password').then((m) => m.ForgotPassword),
      },
      {
        path: 'reinitialiser-mot-de-passe',
        loadComponent: () =>
          import('./features/auth/reset-password/reset-password').then((m) => m.ResetPassword),
      },
      {
        path: 'recuperation-compte',
        loadComponent: () =>
          import('./features/auth/account-recovery/account-recovery').then((m) => m.AccountRecovery),
      },
      { path: 'contact', component: Placeholder, data: { title: 'Contact' } },
      {
        path: 'legal/:slug',
        loadComponent: () => import('./features/legal/legal-page/legal-page').then((m) => m.LegalPage),
      },
    ],
  },
  {
    path: 'vendeur',
    loadComponent: () => import('./layout/vendor-layout/vendor-layout').then((m) => m.VendorLayout),
    canActivate: [roleGuard('ROLE_VENDEUR')],
    children: [
      { path: '', redirectTo: 'tableau-de-bord', pathMatch: 'full' },
      { path: 'tableau-de-bord', component: Placeholder, data: { title: 'Tableau de bord vendeur' } },
      { path: 'ma-boutique', component: Placeholder, data: { title: 'Ma boutique' } },
      { path: 'produits', component: Placeholder, data: { title: 'Mes produits' } },
      { path: 'commandes', component: Placeholder, data: { title: 'Commandes reçues' } },
      { path: 'statistiques', component: Placeholder, data: { title: 'Statistiques de vente' } },
      { path: 'parametres', component: Placeholder, data: { title: 'Paramètres du compte' } },
    ],
  },
  {
    path: 'admin',
    loadComponent: () => import('./layout/admin-layout/admin-layout').then((m) => m.AdminLayout),
    canActivate: [roleGuard('ROLE_ADMIN')],
    children: [
      { path: '', redirectTo: 'tableau-de-bord', pathMatch: 'full' },
      { path: 'tableau-de-bord', component: Placeholder, data: { title: 'Tableau de bord admin' } },
      { path: 'demandes-boutiques', component: Placeholder, data: { title: 'Demandes de boutiques' } },
      { path: 'produits', component: Placeholder, data: { title: 'Produits à valider' } },
      { path: 'utilisateurs', component: Placeholder, data: { title: 'Utilisateurs' } },
      { path: 'commandes', component: Placeholder, data: { title: 'Commandes' } },
      { path: 'categories', component: Placeholder, data: { title: 'Catégories' } },
      { path: 'commission', component: Placeholder, data: { title: 'Taux de commission' } },
      { path: 'statistiques', component: Placeholder, data: { title: 'Statistiques' } },
      { path: 'parametres', component: Placeholder, data: { title: 'Paramètres' } },
    ],
  },
];
