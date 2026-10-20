import { Routes } from '@angular/router';

import { Placeholder } from './shared/ui/placeholder/placeholder';
import { authGuard, excludeRoleGuard, roleGuard } from './core/auth/auth.guard';

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
        path: 'commandes/paiement-retour',
        canActivate: [authGuard],
        loadComponent: () => import('./features/cart/payment-return/payment-return').then((m) => m.PaymentReturn),
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
        canActivate: [authGuard, excludeRoleGuard('ROLE_LIVREUR', '/livreur')],
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
    path: 'mon-espace',
    loadComponent: () => import('./layout/user-layout/user-layout').then((m) => m.UserLayout),
    canActivate: [authGuard, excludeRoleGuard('ROLE_LIVREUR', '/livreur')],
    children: [
      { path: '', redirectTo: 'tableau-de-bord', pathMatch: 'full' },
      {
        path: 'tableau-de-bord',
        loadComponent: () => import('./features/account/user-dashboard/user-dashboard').then((m) => m.UserDashboard),
      },
      {
        path: 'commandes',
        loadComponent: () => import('./features/account/my-orders/my-orders').then((m) => m.MyOrders),
      },
      {
        path: 'commandes/:id/facture',
        loadComponent: () => import('./features/account/order-invoice/order-invoice').then((m) => m.OrderInvoice),
      },
      {
        path: 'parametres',
        loadComponent: () => import('./features/account/account-settings/account-settings').then((m) => m.AccountSettings),
      },
    ],
  },
  {
    path: 'vendeur',
    loadComponent: () => import('./layout/vendor-layout/vendor-layout').then((m) => m.VendorLayout),
    // Garde l'acces a tout user connecte (pas seulement ROLE_VENDEUR) : un user
    // sans boutique encore validee doit pouvoir suivre le statut de sa demande.
    canActivate: [authGuard, excludeRoleGuard('ROLE_LIVREUR', '/livreur')],
    children: [
      { path: '', redirectTo: 'tableau-de-bord', pathMatch: 'full' },
      {
        path: 'tableau-de-bord',
        loadComponent: () => import('./features/vendor/dashboard/dashboard').then((m) => m.Dashboard),
      },
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
      {
        path: 'commandes',
        loadComponent: () => import('./features/vendor/orders-list/orders-list').then((m) => m.OrdersList),
      },
      {
        path: 'commandes/:id',
        loadComponent: () => import('./features/vendor/order-detail/order-detail').then((m) => m.OrderDetail),
      },
      {
        path: 'statistiques',
        loadComponent: () => import('./features/vendor/statistiques/statistiques').then((m) => m.Statistiques),
      },
      {
        path: 'livreurs',
        loadComponent: () => import('./features/vendor/livreurs/livreurs').then((m) => m.Livreurs),
      },
      {
        path: 'parametres',
        loadComponent: () => import('./features/account/account-settings/account-settings').then((m) => m.AccountSettings),
      },
    ],
  },
  {
    path: 'livreur',
    loadComponent: () => import('./layout/livreur-layout/livreur-layout').then((m) => m.LivreurLayout),
    canActivate: [roleGuard('ROLE_LIVREUR')],
    children: [
      { path: '', redirectTo: 'livraisons', pathMatch: 'full' },
      {
        path: 'livraisons',
        loadComponent: () => import('./features/livreur/my-deliveries/my-deliveries').then((m) => m.MyDeliveries),
      },
      {
        path: 'livraisons/:id',
        loadComponent: () =>
          import('./features/livreur/delivery-confirm/delivery-confirm').then((m) => m.DeliveryConfirm),
      },
      {
        path: 'historique',
        loadComponent: () =>
          import('./features/livreur/delivery-history/delivery-history').then((m) => m.DeliveryHistory),
      },
      {
        path: 'parametres',
        loadComponent: () => import('./features/account/account-settings/account-settings').then((m) => m.AccountSettings),
      },
    ],
  },
  {
    path: 'admin',
    loadComponent: () => import('./layout/admin-layout/admin-layout').then((m) => m.AdminLayout),
    canActivate: [roleGuard('ROLE_ADMIN')],
    children: [
      { path: '', redirectTo: 'tableau-de-bord', pathMatch: 'full' },
      {
        path: 'tableau-de-bord',
        loadComponent: () => import('./features/admin/dashboard/dashboard').then((m) => m.Dashboard),
      },
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
      {
        path: 'utilisateurs',
        loadComponent: () => import('./features/admin/users-list/users-list').then((m) => m.UsersList),
      },
      {
        path: 'commandes',
        loadComponent: () => import('./features/admin/orders-list/orders-list').then((m) => m.OrdersList),
      },
      {
        path: 'commandes/:id',
        loadComponent: () => import('./features/admin/order-detail/order-detail').then((m) => m.OrderDetail),
      },
      {
        path: 'reversements',
        loadComponent: () => import('./features/admin/payouts/payouts').then((m) => m.Payouts),
      },
      {
        path: 'categories',
        loadComponent: () => import('./features/admin/categories/categories').then((m) => m.Categories),
      },
      {
        path: 'commission',
        loadComponent: () => import('./features/admin/commission/commission').then((m) => m.Commission),
      },
      {
        path: 'avis',
        loadComponent: () => import('./features/admin/reviews/reviews').then((m) => m.Reviews),
      },
      {
        path: 'livreurs-kyc',
        loadComponent: () =>
          import('./features/admin/livreurs-kyc-list/livreurs-kyc-list').then((m) => m.LivreursKycList),
      },
      {
        path: 'livreurs-kyc/:id',
        loadComponent: () =>
          import('./features/admin/livreurs-kyc-detail/livreurs-kyc-detail').then((m) => m.LivreursKycDetail),
      },
      {
        path: 'statistiques',
        loadComponent: () => import('./features/admin/statistiques/statistiques').then((m) => m.Statistiques),
      },
      {
        path: 'parametres',
        loadComponent: () => import('./features/account/account-settings/account-settings').then((m) => m.AccountSettings),
      },
    ],
  },
];
