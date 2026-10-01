import { Routes } from '@angular/router';

import { Placeholder } from './shared/ui/placeholder/placeholder';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/public-layout/public-layout').then((m) => m.PublicLayout),
    children: [
      { path: '', component: Placeholder, data: { title: 'Catalogue' } },
      { path: 'catalogue', component: Placeholder, data: { title: 'Catalogue' } },
      { path: 'panier', component: Placeholder, data: { title: 'Panier' } },
      { path: 'connexion', component: Placeholder, data: { title: 'Connexion' } },
      { path: 'inscription', component: Placeholder, data: { title: 'Inscription' } },
      { path: 'contact', component: Placeholder, data: { title: 'Contact' } },
      { path: 'legal/mentions-legales', component: Placeholder, data: { title: 'Mentions légales' } },
      { path: 'legal/cgu', component: Placeholder, data: { title: 'CGU' } },
      { path: 'legal/cgv', component: Placeholder, data: { title: 'CGV' } },
      { path: 'legal/confidentialite', component: Placeholder, data: { title: 'Politique de confidentialité' } },
      { path: 'legal/cookies', component: Placeholder, data: { title: 'Politique de cookies' } },
    ],
  },
  {
    path: 'vendeur',
    loadComponent: () => import('./layout/vendor-layout/vendor-layout').then((m) => m.VendorLayout),
    // TODO phase 1/2 : protéger avec un roleGuard (ROLE_VENDEUR).
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
    // TODO phase 1/2 : protéger avec un roleGuard (ROLE_ADMIN).
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
