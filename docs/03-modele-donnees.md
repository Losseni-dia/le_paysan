# 03 — Modèle de données

Statut : v1.0 — 2026-10-01

Base de données : PostgreSQL, nom `le_paysan` (déjà créée).
Script de création : [sql/schema.sql](./sql/schema.sql).

## 1. Vue d'ensemble (MCD simplifié)

```
users (1) ───< user_roles >─── (1) roles
users (1) ──── (0..1) shops
shops (1) ──── (0..1) shop_individual_profile     [si owner_type = PHYSIQUE]
shops (1) ──── (0..1) shop_entreprenant_profile   [si owner_type = ENTREPRENANT]
shops (1) ──── (0..1) shop_company_profile        [si owner_type = MORALE]
shops (1) ───< shop_validations
shops (1) ───< products
categories (1) ───< products
products (1) ───< product_images
products (1) ───< product_delivery_options
products (1) ───< product_validations
users (1) ───< carts (1) ───< cart_items >─── (1) products
users (1) ───< orders
shops (1) ───< orders
shops (1) ──── (0..1) shop_payout_account
orders (1) ───< order_items >─── (1) products
orders (1) ──── (0..1) deliveries
orders (1) ──── (0..1) payments
orders (1) ──── (0..1) vendor_payouts
users (1) ───< product_reviews >─── (1) products
users (1) ───< shop_reviews >─── (1) shops
users (0..1) ───< contact_messages
users (1) ───< account_recovery_requests
users (1) ───< notifications
```

## 2. Dictionnaire des tables

### 2.1 `users`
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| username | VARCHAR(30) | UNIQUE, NOT NULL (identifiant de connexion) |
| email | VARCHAR(255) | UNIQUE, NULL (optionnel à l'inscription ; renseigné via Google OAuth ou ajouté plus tard par le user) |
| password_hash | VARCHAR(255) | NULL (nullable si compte créé via OAuth uniquement) |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| phone | VARCHAR(30) | NULL |
| security_selfie_url | VARCHAR(500) | NULL (selfie de référence, optionnel mais recommandé — sert à la récupération de compte sans email, cf. UC-01b) |
| auth_provider | VARCHAR(20) | NOT NULL, DEFAULT 'LOCAL' (`LOCAL`, `GOOGLE`) |
| provider_id | VARCHAR(255) | NULL (id externe OAuth) |
| enabled | BOOLEAN | NOT NULL DEFAULT TRUE |
| email_verified | BOOLEAN | NOT NULL DEFAULT FALSE |
| cgu_accepted_at | TIMESTAMP | NULL (horodatage d'acceptation des CGU, cf. [08-mentions-legales-cgu-rgpd.md](./08-mentions-legales-cgu-rgpd.md)) |
| cgu_version | VARCHAR(10) | NULL (version du document CGU acceptée) |
| cgv_accepted_at | TIMESTAMP | NULL (horodatage d'acceptation des CGV, applicables dès le premier achat) |
| cgv_version | VARCHAR(10) | NULL (version du document CGV acceptée) |
| privacy_accepted_at | TIMESTAMP | NULL (horodatage de prise de connaissance de la Politique de confidentialité, couvre la politique de cookies) |
| privacy_version | VARCHAR(10) | NULL (version du document Politique de confidentialité acceptée) |
| created_at | TIMESTAMP | NOT NULL DEFAULT now() |
| updated_at | TIMESTAMP | NOT NULL DEFAULT now() |

### 2.2 `roles` / `user_roles`
- `roles(id, name)` : valeurs `ROLE_USER`, `ROLE_VENDEUR`, `ROLE_ADMIN`.
- `user_roles(user_id FK, role_id FK)` : clé composite, N-N entre users et roles.

### 2.3 `shops` (boutiques)
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| owner_user_id | BIGINT | FK users, UNIQUE, NOT NULL (1 boutique / user) |
| owner_type | VARCHAR(15) | NOT NULL (`PHYSIQUE`, `ENTREPRENANT`, `MORALE`) |
| name | VARCHAR(150) | UNIQUE, NOT NULL |
| description | TEXT | NULL |
| category | VARCHAR(100) | NULL |
| address_city | VARCHAR(100) | NOT NULL |
| address_detail | VARCHAR(255) | NOT NULL |
| latitude | NUMERIC(10,7) | NOT NULL (localisation sur carte, obligatoire — permet aux acheteurs de s'y rendre pour le retrait) |
| longitude | NUMERIC(10,7) | NOT NULL |
| phone | VARCHAR(30) | NOT NULL |
| logo_url | VARCHAR(500) | NULL |
| cover_url | VARCHAR(500) | NULL |
| status | VARCHAR(20) | NOT NULL DEFAULT 'EN_ATTENTE' (`EN_ATTENTE`, `VALIDEE`, `REJETEE`, `SUSPENDUE`) |
| cgv_vendeur_accepted_at | TIMESTAMP | NULL (acceptation des Conditions vendeur, cf. [08-mentions-legales-cgu-rgpd.md](./08-mentions-legales-cgu-rgpd.md)) |
| cgv_vendeur_version | VARCHAR(10) | NULL |
| created_at / updated_at | TIMESTAMP | NOT NULL |

### 2.4 `shop_individual_profile` (personne physique, 1-1 avec shops si owner_type = PHYSIQUE)
| Colonne | Type | Contraintes |
|---|---|---|
| shop_id | BIGINT | PK, FK shops |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| birth_date | DATE | NOT NULL |
| id_document_type | VARCHAR(20) | NOT NULL (`CNI`, `ATTESTATION_IDENTITE`, `PASSEPORT`) |
| id_document_number | VARCHAR(50) | NOT NULL |
| id_document_front_url | VARCHAR(500) | NOT NULL |
| id_document_back_url | VARCHAR(500) | NULL |
| selfie_with_id_url | VARCHAR(500) | NOT NULL (selfie du titulaire tenant sa pièce d'identité, preuve de vivacité/anti-usurpation) |

### 2.5 `shop_entreprenant_profile` (statut Entreprenant OHADA, 1-1 avec shops si owner_type = ENTREPRENANT)
| Colonne | Type | Contraintes |
|---|---|---|
| shop_id | BIGINT | PK, FK shops |
| first_name | VARCHAR(100) | NOT NULL |
| last_name | VARCHAR(100) | NOT NULL |
| birth_date | DATE | NOT NULL |
| id_document_type | VARCHAR(20) | NOT NULL (`CNI`, `ATTESTATION_IDENTITE`, `PASSEPORT`) |
| id_document_number | VARCHAR(50) | NOT NULL |
| id_document_front_url | VARCHAR(500) | NOT NULL |
| id_document_back_url | VARCHAR(500) | NULL |
| selfie_with_id_url | VARCHAR(500) | NOT NULL (selfie du titulaire tenant sa pièce d'identité) |
| recepisse_number | VARCHAR(50) | NOT NULL (numéro de récépissé d'entreprenant, délivré par le CEPICI) |
| recepisse_date | DATE | NOT NULL |
| recepisse_document_url | VARCHAR(500) | NOT NULL |

### 2.6 `shop_company_profile` (personne morale, 1-1 avec shops si owner_type = MORALE)
| Colonne | Type | Contraintes |
|---|---|---|
| shop_id | BIGINT | PK, FK shops |
| company_name | VARCHAR(200) | NOT NULL |
| legal_form | VARCHAR(20) | NOT NULL (`SARL`, `SA`, `SAS`, `SUARL`, `AUTRE`) |
| rccm_number | VARCHAR(30) | NOT NULL (format ivoirien, ex. `CI-ABJ-2024-A-1234`) |
| ncc_number | VARCHAR(30) | NOT NULL (Numéro de Compte Contribuable, DGI) |
| legal_document_url | VARCHAR(500) | NOT NULL (RCCM / statuts) |
| legal_rep_name | VARCHAR(150) | NOT NULL |
| legal_rep_role | VARCHAR(100) | NOT NULL |
| legal_rep_id_document_type | VARCHAR(20) | NOT NULL (`CNI`, `ATTESTATION_IDENTITE`, `PASSEPORT`) |
| legal_rep_id_document_number | VARCHAR(50) | NOT NULL |
| legal_rep_id_document_front_url | VARCHAR(500) | NOT NULL |
| legal_rep_id_document_back_url | VARCHAR(500) | NULL |
| legal_rep_selfie_with_id_url | VARCHAR(500) | NOT NULL (selfie du représentant légal tenant sa pièce d'identité — même exigence KYC que pour une personne physique) |

### 2.7 `shop_validations` (historique de décisions admin sur une boutique)
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| shop_id | BIGINT | FK shops, NOT NULL |
| admin_id | BIGINT | FK users, NOT NULL |
| decision | VARCHAR(20) | NOT NULL (`VALIDEE`, `REJETEE`) |
| reason | TEXT | NULL (obligatoire applicativement si REJETEE) |
| created_at | TIMESTAMP | NOT NULL DEFAULT now() |

### 2.8 `categories`
`id, name UNIQUE, slug UNIQUE`

### 2.9 `products`
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| shop_id | BIGINT | FK shops, NOT NULL |
| category_id | BIGINT | FK categories, NOT NULL |
| name | VARCHAR(150) | NOT NULL |
| description | TEXT | NULL |
| unit | VARCHAR(20) | NOT NULL (kg, pièce, sac, litre...) |
| unit_price | NUMERIC(12,2) | NOT NULL, CHECK > 0 |
| stock_quantity | INTEGER | NOT NULL, CHECK >= 0 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'EN_ATTENTE' (`EN_ATTENTE`, `VALIDE`, `REJETE`, `DEPUBLIE`) |
| created_at / updated_at | TIMESTAMP | NOT NULL |

### 2.10 `product_images`
`id, product_id FK, url, position INT, created_at`

### 2.11 `product_delivery_options`
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| product_id | BIGINT | FK products, NOT NULL |
| mode | VARCHAR(20) | NOT NULL (`RETRAIT_BOUTIQUE`, `LIVRAISON_DOMICILE`) |
| pickup_location | VARCHAR(255) | NULL (si RETRAIT_BOUTIQUE) |
| delivery_price | NUMERIC(12,2) | NULL (NOT NULL applicatif si LIVRAISON_DOMICILE) |

Contrainte unique `(product_id, mode)` : un produit ne peut avoir deux fois le même mode.

### 2.12 `product_validations` (historique admin sur produit)
Même structure que `shop_validations`, référence `product_id` + `admin_id`, `decision` (`VALIDE`, `REJETE`), `reason`.

### 2.13 `carts` / `cart_items`
- `carts(id, user_id FK UNIQUE, created_at, updated_at)` — un panier actif par user.
- `cart_items(id, cart_id FK, product_id FK, quantity, delivery_mode VARCHAR(20), delivery_address TEXT NULL, created_at)` — contrainte unique `(cart_id, product_id)`.

### 2.14 `orders` (une commande = les lignes d'une boutique issues d'un même paiement)
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| order_number | VARCHAR(30) | UNIQUE, NOT NULL |
| buyer_user_id | BIGINT | FK users, NOT NULL |
| shop_id | BIGINT | FK shops, NOT NULL |
| status | VARCHAR(25) | NOT NULL DEFAULT 'EN_ATTENTE_PAIEMENT' |
| subtotal_amount | NUMERIC(12,2) | NOT NULL |
| delivery_amount | NUMERIC(12,2) | NOT NULL DEFAULT 0 |
| total_amount | NUMERIC(12,2) | NOT NULL |
| commission_rate | NUMERIC(5,2) | NOT NULL |
| commission_amount | NUMERIC(12,2) | NOT NULL |
| payment_transaction_id | BIGINT | FK payment_transactions, NULL |
| created_at / updated_at | TIMESTAMP | NOT NULL |

### 2.15 `order_items`
`id, order_id FK, product_id FK, product_name_snapshot, unit_price_snapshot, quantity, delivery_mode, delivery_price, delivery_address NULL`

(snapshots du nom/prix pour garder l'historique même si le produit change ensuite)

### 2.16 `payment_transactions`
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| buyer_user_id | BIGINT | FK users, NOT NULL |
| fedapay_transaction_id | VARCHAR(100) | UNIQUE, NOT NULL |
| amount | NUMERIC(12,2) | NOT NULL |
| status | VARCHAR(20) | NOT NULL (`PENDING`, `APPROVED`, `FAILED`, `CANCELED`) |
| raw_payload | JSONB | NULL (payload webhook brut pour audit) |
| created_at / updated_at | TIMESTAMP | NOT NULL |

Une transaction Fedapay peut couvrir plusieurs `orders` (panier multi-boutiques).

### 2.17 `commission_settings`
`id, category_id FK NULL (NULL = taux par défaut), rate NUMERIC(5,2), active BOOLEAN, created_at`

### 2.18 `product_reviews` (avis produit, vérifié achat)
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| product_id | BIGINT | FK products, NOT NULL |
| buyer_user_id | BIGINT | FK users, NOT NULL |
| order_item_id | BIGINT | FK order_items, NOT NULL (preuve d'achat livré) |
| rating | SMALLINT | NOT NULL, CHECK entre 1 et 5 |
| comment | TEXT | NULL |
| status | VARCHAR(10) | NOT NULL DEFAULT 'VISIBLE' (`VISIBLE`, `MASQUE`) |
| moderation_reason | TEXT | NULL (motif si `MASQUE`) |
| created_at / updated_at | TIMESTAMP | NOT NULL |

Contrainte unique `(product_id, buyer_user_id)` : un seul avis par acheteur et par produit (modifiable, pas cumulable).

### 2.19 `shop_reviews` (avis boutique/vendeur, vérifié achat)
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| shop_id | BIGINT | FK shops, NOT NULL |
| buyer_user_id | BIGINT | FK users, NOT NULL |
| order_id | BIGINT | FK orders, NOT NULL (preuve de commande livrée auprès de cette boutique) |
| rating | SMALLINT | NOT NULL, CHECK entre 1 et 5 |
| comment | TEXT | NULL |
| status | VARCHAR(10) | NOT NULL DEFAULT 'VISIBLE' (`VISIBLE`, `MASQUE`) |
| moderation_reason | TEXT | NULL |
| created_at / updated_at | TIMESTAMP | NOT NULL |

Contrainte unique `(shop_id, buyer_user_id)`.

> Note : moyenne et nombre d'avis sont calculés à la volée (`AVG`/`COUNT` sur avis `VISIBLE`) plutôt que dénormalisés au MVP ; une colonne de cache (`average_rating`, `review_count`) sur `products`/`shops` pourra être ajoutée si le volume le justifie.

### 2.20 `contact_messages` (formulaire de contact / support)
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK users, NULL (visiteur non connecté possible) |
| full_name | VARCHAR(150) | NOT NULL |
| email | VARCHAR(255) | NOT NULL (seul moyen de recontacter l'expéditeur) |
| subject | VARCHAR(200) | NOT NULL |
| message | TEXT | NOT NULL |
| status | VARCHAR(15) | NOT NULL DEFAULT 'NOUVEAU' (`NOUVEAU`, `EN_COURS`, `TRAITE`) |
| admin_response | TEXT | NULL |
| handled_by_admin_id | BIGINT | FK users, NULL |
| handled_at | TIMESTAMP | NULL |
| created_at | TIMESTAMP | NOT NULL DEFAULT now() |

### 2.21 `account_recovery_requests` (récupération de compte sans email, par selfie)
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK users, NOT NULL |
| submitted_selfie_url | VARCHAR(500) | NOT NULL (selfie pris en direct au moment de la demande) |
| status | VARCHAR(15) | NOT NULL DEFAULT 'NOUVELLE' (`NOUVELLE`, `EN_COURS`, `APPROUVEE`, `REJETEE`) |
| reviewed_by_admin_id | BIGINT | FK users, NULL |
| review_reason | TEXT | NULL (motif si `REJETEE`) |
| temporary_password_issued_at | TIMESTAMP | NULL (horodatage de génération du mot de passe temporaire si `APPROUVEE`) |
| created_at | TIMESTAMP | NOT NULL DEFAULT now() |
| reviewed_at | TIMESTAMP | NULL |

### 2.22 `shop_payout_accounts` (compte Mobile Money de reversement, 1-1 avec shops)
| Colonne | Type | Contraintes |
|---|---|---|
| shop_id | BIGINT | PK, FK shops |
| operator | VARCHAR(20) | NOT NULL (`ORANGE`, `MTN`, `MOOV`, `WAVE`) |
| phone_number | VARCHAR(30) | NOT NULL |
| account_holder_name | VARCHAR(150) | NOT NULL |
| verified | BOOLEAN | NOT NULL DEFAULT FALSE |
| created_at / updated_at | TIMESTAMP | NOT NULL |

Un vendeur doit avoir un `shop_payout_account` renseigné pour pouvoir publier des produits (règle applicative).

### 2.23 `vendor_payouts` (reversement automatique au vendeur, par commande)
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| order_id | BIGINT | FK orders, UNIQUE, NOT NULL |
| shop_id | BIGINT | FK shops, NOT NULL |
| amount | NUMERIC(12,2) | NOT NULL (montant net : sous-total produits − commission + frais de livraison) |
| status | VARCHAR(15) | NOT NULL DEFAULT 'EN_ATTENTE' (`EN_ATTENTE`, `ENVOYE`, `ECHEC`) |
| fedapay_payout_id | VARCHAR(100) | NULL (référence du transfert côté Fedapay, si Option B) |
| failure_reason | TEXT | NULL |
| created_at | TIMESTAMP | NOT NULL DEFAULT now() |
| processed_at | TIMESTAMP | NULL |

Créé automatiquement à la confirmation du paiement d'une commande ; traité par le job de reversement automatique ou par le split payment selon l'implémentation retenue (cf. [04-architecture-technique.md §5](./04-architecture-technique.md)).

### 2.24 `notifications` (centre de notifications in-app, canal obligatoire)
| Colonne | Type | Contraintes |
|---|---|---|
| id | BIGSERIAL | PK |
| recipient_user_id | BIGINT | FK users, NOT NULL |
| type | VARCHAR(40) | NOT NULL (ex. `SHOP_VALIDATED`, `SHOP_REJECTED`, `PRODUCT_VALIDATED`, `PRODUCT_REJECTED`, `NEW_ORDER`, `ORDER_STATUS_CHANGED`, `PAYMENT_CONFIRMED`, `NEW_REVIEW`, `SUPPORT_REPLY`, `PAYOUT_SENT`, `PAYOUT_FAILED`) |
| title | VARCHAR(200) | NOT NULL |
| message | TEXT | NOT NULL |
| link_url | VARCHAR(500) | NULL (lien interne vers l'objet concerné, ex. la commande) |
| read_at | TIMESTAMP | NULL (NULL = non lue) |
| created_at | TIMESTAMP | NOT NULL DEFAULT now() |

Écrite systématiquement pour tout événement clé, indépendamment du fait que l'utilisateur ait un email renseigné ou non (cf. [02-specifications-fonctionnelles.md UC-13](./02-specifications-fonctionnelles.md)) ; l'envoi d'un email est une action complémentaire et non une condition d'existence de la notification.

## 3. Index recommandés

- `products(status, category_id)` — filtrage catalogue.
- `products(shop_id)`.
- `shops(status)`.
- `orders(buyer_user_id)`, `orders(shop_id)`, `orders(status)`.
- `payment_transactions(fedapay_transaction_id)` déjà unique.
- `product_reviews(product_id, status)`, `shop_reviews(shop_id, status)` — calcul de moyenne/affichage.
- `contact_messages(status)` — file de traitement du support.
- `account_recovery_requests(status)` — file de traitement des récupérations de compte.
- `vendor_payouts(status)` — file de traitement des reversements automatiques.
- `notifications(recipient_user_id, read_at)` — chargement rapide du centre de notifications et du badge non-lues.

## 4. Points ouverts

- Faut-il stocker une adresse de livraison réutilisable par user (`user_addresses`) plutôt que de la ressaisir à chaque commande ? → amélioration post-MVP.
- Gestion de devise : XOF (FCFA) supposé unique pour le MVP, pas de colonne devise au départ.
