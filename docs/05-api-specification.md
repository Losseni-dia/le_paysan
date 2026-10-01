# 05 — Spécification API (REST)

Statut : v1.0 — 2026-10-01

Base URL : `/api`. Toutes les réponses en JSON. Authentification par `Authorization: Bearer <access_token>` sauf endpoints publics.

## 1. Auth (`/api/auth`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Inscription (`username` + `password`, `email` optionnel, + `acceptCgu`/`acceptCgv`/`acceptPrivacy` obligatoires à `true`) |
| POST | `/auth/login` | Public | Connexion (`username` + `password`), retourne access+refresh token |
| POST | `/auth/refresh` | Public (refresh token) | Renouvelle l'access token |
| POST | `/auth/logout` | Authentifié | Invalide le refresh token |
| GET | `/auth/oauth2/google` | Public | Redirection vers Google OAuth |
| GET | `/auth/oauth2/google/callback` | Public | Callback Google, crée/lie le compte (consentement CGU/CGV/confidentialité demandé si première connexion), retourne tokens |
| POST | `/users/me/consents` | Authentifié | Enregistrer/compléter les consentements CGU/CGV/confidentialité (cas première connexion Google) |
| POST | `/auth/forgot-password` | Public | Demande de réinitialisation par email (si le compte a un email) |
| POST | `/auth/reset-password` | Public (token email) | Définit un nouveau mot de passe via le lien reçu par email |
| POST | `/auth/account-recovery-requests` | Public | Demande de récupération de compte **sans email**, par selfie (body: `username`, `selfie` en multipart) — cf. UC-01b |
| GET | `/admin/account-recovery-requests?status=NOUVELLE` | Admin | File des demandes de récupération à traiter |
| GET | `/admin/account-recovery-requests/{id}` | Admin | Détail (selfie soumis + selfie de référence côte à côte) |
| POST | `/admin/account-recovery-requests/{id}/approve` | Admin | Approuver : génère un mot de passe temporaire |
| POST | `/admin/account-recovery-requests/{id}/reject` | Admin | Rejeter (body: `reason`) |

## 2. Utilisateurs (`/api/users`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| GET | `/users/me` | Authentifié | Profil courant |
| PUT | `/users/me` | Authentifié | Mise à jour du profil |
| PUT | `/users/me/security-selfie` | Authentifié | Ajouter/mettre à jour le selfie de sécurité (multipart) |
| GET | `/users` | Admin | Liste des utilisateurs (pagination, filtres) |
| PATCH | `/users/{id}/status` | Admin | Activer/désactiver un compte |

## 3. Boutiques (`/api/shops`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| POST | `/shops/requests` | User | Soumettre une demande de boutique (payload adapté PHYSIQUE/ENTREPRENANT/MORALE, inclut `latitude`/`longitude` obligatoires, multipart pour documents) |
| GET | `/shops/me` | Vendeur | Détail de ma boutique |
| PUT | `/shops/me` | Vendeur | Modifier les infos de ma boutique (hors champs validés par admin) |
| GET | `/shops/{id}` | Public | Fiche boutique publique (si `ACTIVE`) |
| GET | `/shops` | Public | Liste/recherche des boutiques actives |
| GET | `/admin/shops?status=EN_ATTENTE` | Admin | Liste des demandes à traiter |
| GET | `/admin/shops/{id}` | Admin | Détail demande (infos + documents) |
| POST | `/admin/shops/{id}/validate` | Admin | Valider la boutique |
| POST | `/admin/shops/{id}/reject` | Admin | Rejeter (body: `reason`) |
| POST | `/admin/shops/{id}/suspend` | Admin | Suspendre une boutique active |
| GET | `/shops/me/payout-account` | Vendeur | Détail du compte Mobile Money de reversement |
| PUT | `/shops/me/payout-account` | Vendeur | Créer/modifier le compte de reversement (operator, phoneNumber, accountHolderName) |
| GET | `/shops/me/payouts` | Vendeur | Historique de mes reversements (`vendor_payouts`) |
| GET | `/admin/payouts?status=ECHEC` | Admin | Vue globale des reversements, filtrable par statut |
| POST | `/admin/payouts/{id}/retry` | Admin | Relancer un reversement en échec |

## 4. Catégories (`/api/categories`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| GET | `/categories` | Public | Liste des catégories |
| POST | `/categories` | Admin | Créer une catégorie |

## 5. Produits (`/api/products`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| GET | `/products` | Public | Catalogue, filtres (catégorie, boutique, prix, mode livraison), pagination |
| GET | `/products/{id}` | Public | Fiche produit (si `VALIDE`) |
| POST | `/vendor/products` | Vendeur | Créer un produit (+ images, modes de livraison) |
| PUT | `/vendor/products/{id}` | Vendeur | Modifier (repasse en `EN_ATTENTE`) |
| PATCH | `/vendor/products/{id}/publish-state` | Vendeur | Dépublier/republier un produit validé |
| GET | `/vendor/products` | Vendeur | Mes produits (tous statuts) |
| GET | `/admin/products?status=EN_ATTENTE` | Admin | Produits à valider |
| POST | `/admin/products/{id}/validate` | Admin | Valider |
| POST | `/admin/products/{id}/reject` | Admin | Rejeter (body: `reason`) |

## 6. Panier (`/api/cart`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| GET | `/cart` | User | Contenu du panier |
| POST | `/cart/items` | User | Ajouter un produit (quantity, deliveryMode, deliveryAddress?) |
| PUT | `/cart/items/{productId}` | User | Modifier quantité / mode livraison |
| DELETE | `/cart/items/{productId}` | User | Retirer un produit |
| DELETE | `/cart` | User | Vider le panier |

## 7. Commandes (`/api/orders`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| POST | `/orders/checkout` | User | Transforme le panier (ou achat direct) en commande(s) + initie le paiement Fedapay |
| GET | `/orders` | User | Mes commandes (acheteur) |
| GET | `/orders/{id}` | User/Vendeur (propriétaire) | Détail commande |
| GET | `/vendor/orders` | Vendeur | Commandes reçues par ma boutique |
| PATCH | `/vendor/orders/{id}/status` | Vendeur | Mettre à jour le statut (préparation, prête/expédiée, livrée) |
| GET | `/admin/orders` | Admin | Vue globale des commandes |

## 8. Paiement (`/api/payments`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| POST | `/payments/fedapay/webhook` | Fedapay (signature vérifiée) | Réception du statut de paiement |
| GET | `/payments/{transactionId}` | User (propriétaire) | Statut d'une transaction |

## 9. Avis et notation (`/api/reviews`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| GET | `/products/{id}/reviews` | Public | Liste des avis `VISIBLE` d'un produit (pagination) + moyenne/nombre |
| PUT | `/products/{id}/reviews/me` | User | Créer/modifier mon avis sur ce produit (vérifie achat livré) |
| GET | `/shops/{id}/reviews` | Public | Liste des avis `VISIBLE` d'une boutique + moyenne/nombre |
| PUT | `/shops/{id}/reviews/me` | User | Créer/modifier mon avis sur cette boutique (vérifie achat livré) |
| GET | `/admin/reviews` | Admin | Liste des avis (modération, filtrable par statut) |
| PATCH | `/admin/reviews/{type}/{id}/hide` | Admin | Masquer un avis (body: `reason`) — `type` = `product` ou `shop` |
| PATCH | `/admin/reviews/{type}/{id}/show` | Admin | Réafficher un avis masqué |

## 10. Contact / Support (`/api/contact`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| POST | `/contact` | Public (visiteur) ou Authentifié | Soumettre un message (fullName, email, subject, message) |
| GET | `/admin/contact-messages?status=NOUVEAU` | Admin | Liste des messages de support |
| GET | `/admin/contact-messages/{id}` | Admin | Détail d'un message |
| PATCH | `/admin/contact-messages/{id}/status` | Admin | Changer le statut (`EN_COURS`, `TRAITE`) |
| POST | `/admin/contact-messages/{id}/reply` | Admin | Répondre (enregistre la réponse, envoie un email à l'expéditeur) |

## 11. Notifications (`/api/notifications`)

Canal obligatoire, indépendant de l'email (cf. [02-specifications-fonctionnelles.md UC-13](./02-specifications-fonctionnelles.md)).

| Méthode | URL | Accès | Description |
|---|---|---|---|
| GET | `/notifications` | Authentifié | Liste de mes notifications (pagination), filtrable `?unreadOnly=true` |
| GET | `/notifications/unread-count` | Authentifié | Nombre de notifications non lues (badge) |
| PATCH | `/notifications/{id}/read` | Authentifié | Marquer une notification comme lue |
| PATCH | `/notifications/read-all` | Authentifié | Marquer toutes comme lues |

## 12. Administration / statistiques (`/api/admin`)

| Méthode | URL | Accès | Description |
|---|---|---|---|
| GET | `/admin/stats/overview` | Admin | KPIs globaux (ventes, boutiques actives, commissions) |
| GET | `/admin/commission-settings` | Admin | Taux de commission configurés |
| PUT | `/admin/commission-settings/{id}` | Admin | Modifier un taux |

## 13. Conventions

- Pagination : `?page=0&size=20&sort=createdAt,desc`.
- Erreurs : format uniforme `{ "timestamp", "status", "error", "message", "path" }`.
- Upload de fichiers : `multipart/form-data` pour les endpoints incluant des documents/images.
- Codes HTTP standards (201 création, 204 suppression, 400 validation, 401/403 auth, 404 non trouvé, 409 conflit).

## 14. Points ouverts

- Pagination/tri avancés du catalogue (facettes multiples) à affiner en phase de conception détaillée.
- Rate limiting sur `/auth/*` et `/contact` à prévoir avant mise en production (anti-spam formulaire de contact).
