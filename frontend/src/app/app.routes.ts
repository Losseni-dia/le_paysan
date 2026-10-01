import { Routes } from '@angular/router';

import { Placeholder } from './shared/ui/placeholder/placeholder';
import { authGuard, roleGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layout/public-layout/public-layout').then((m) => m.PublicLayout),
    children: [
      {
        path: '',
        loadComponent: () => import('./features/catalogue/catalogue-list/catalogue-list').then((m) => m.CatalogueList),
      },
      {
        path: 'catalogue',
        loadComponent: () => import('./features/catalogue/catalogue-list/catalogue-list').then((m) => m.CatalogueList),
      },
      {
        path: 'produits/:id',
        loadComponent: () => import('./features/catalogue/product-detail/product-detail').then((m) => m.ProductDetail),
      },
      {
        path: 'boutiques/:id',
        loadComponent: () => import('./features/catalogue/shop-detail/shop-detail').then((m) => m.ShopDetail),
      },
      {
        path: 'panier',
        canActivate: [authGuard],
        loadComponent: () => import('./features/cart/cart-page/cart-page').then((m) => m.CartPage),
      },
      {
        path: 'commandes/confirmation',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/cart/order-confirmation/order-confirmation').then((m) => m.OrderConfirmation),
      },
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
      {
        path: 'devenir-vendeur',
        canActivate: [authGuard],
        loadComponent: () => import('./features/shop/shop-request/shop-request').then((m) => m.ShopRequest),
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
    // Garde l'acces a tout user connecte (pas seulement ROLE_VENDEUR) : un user
    // sans boutique encore validee doit pouvoir suivre le statut de sa demande.
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'tableau-de-bord', pathMatch: 'full' },
      { path: 'tableau-de-bord', component: Placeholder, data: { title: 'Tableau de bord vendeur' } },
      {
        path: 'ma-boutique',
        loadComponent: () => import('./features/vendor/my-shop/my-shop').then((m) => m.MyShop),
      },
      {
        path: 'produits',
        loadComponent: () => import('./features/vendor/my-products/my-products').then((m) => m.MyProducts),
      },
      {
        path: 'produits/nouveau',
        loadComponent: () => import('./features/vendor/product-form/product-form').then((m) => m.ProductForm),
      },
      {
        path: 'produits/:id/modifier',
        loadComponent: () => import('./features/vendor/product-form/product-form').then((m) => m.ProductForm),
      },
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
      {
        path: 'demandes-boutiques',
        loadComponent: () =>
          import('./features/admin/shop-requests-list/shop-requests-list').then((m) => m.ShopRequestsList),
      },
      {
        path: 'demandes-boutiques/:id',
        loadComponent: () =>
          import('./features/admin/shop-request-detail/shop-request-detail').then((m) => m.ShopRequestDetail),
      },
      {
        path: 'produits',
        loadComponent: () => import('./features/admin/products-list/products-list').then((m) => m.ProductsList),
      },
      {
        path: 'produits/:id',
        loadComponent: () => import('./features/admin/product-detail/product-detail').then((m) => m.ProductDetail),
      },
      { path: 'utilisateurs', component: Placeholder, data: { title: 'Utilisateurs' } },
      { path: 'commandes', component: Placeholder, data: { title: 'Commandes' } },
      {
        path: 'categories',
        loadComponent: () => import('./features/admin/categories/categories').then((m) => m.Categories),
      },
      { path: 'commission', component: Placeholder, data: { title: 'Taux de commission' } },
      { path: 'statistiques', component: Placeholder, data: { title: 'Statistiques' } },
      { path: 'parametres', component: Placeholder, data: { title: 'Paramètres' } },
    ],
  },
];
