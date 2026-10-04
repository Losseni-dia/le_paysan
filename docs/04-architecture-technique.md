# 04 — Architecture technique

Statut : v1.0 — 2026-10-01

## 1. Vue d'ensemble

```
┌─────────────────┐        HTTPS/REST (JSON)        ┌──────────────────────────┐
│   Frontend       │ ───────────────────────────────▶│   Backend (Spring Boot)  │
│   Angular (SPA)  │ ◀───────────────────────────────│   monolithe modulaire    │
└─────────────────┘                                   └───────────┬──────────────┘
                                                                    │ JDBC
                                                                    ▼
                                                        ┌──────────────────────┐
                                                        │ PostgreSQL: le_paysan │
                                                        └──────────────────────┘
                                                                    │
                                                 Webhook / API  ▼
                                                        ┌──────────────────────┐
                                                        │   Fedapay (Mobile    │
                                                        │   Money payment)     │
                                                        └──────────────────────┘
                                             Google OAuth ◀────────┘ (login)
```

- **Frontend** : Angular (SPA), consomme l'API REST du backend, responsive (mobile-first vu l'usage Mobile Money).
- **Backend** : Spring Boot (Java 25), monolithe organisé en packages par domaine métier, expose une API REST stateless sécurisée par JWT.
- **Base de données** : PostgreSQL, base `le_paysan`.
- **Paiement** : Fedapay (Mobile Money), intégration via API Fedapay + réception de webhooks de confirmation.
- **Authentification** : JWT (access + refresh token) pour l'auth classique, OAuth2 (Google) pour la connexion sociale.
- **Stockage fichiers** (logos, photos produits, documents justificatifs) : système de fichiers / object storage (cf. §5).

## 2. Organisation du backend (monolithe modulaire)

```
shop.lepaysan
├── config/              # Sécurité, CORS, OpenAPI, beans Fedapay, etc.
├── common/              # Exceptions, utilitaires, pagination, audit
├── auth/                # Inscription, connexion, JWT, OAuth2 Google, refresh token
├── user/                # Profil utilisateur, rôles
├── shop/                # Demande de boutique, profils physique/morale, validation admin
├── product/             # Catalogue, produits, images, modes de livraison, validation admin
├── category/            # Catégories
├── cart/                # Panier
├── order/               # Commandes, lignes de commande, statuts
├── payment/             # Intégration Fedapay, webhooks, transactions
├── commission/          # Règles et calcul de commission
└── admin/               # Endpoints transverses back-office (stats, gestion users)
```

Chaque package suit en interne une structure classique : `controller`, `service`, `repository`, `entity`/`model`, `dto`, `mapper`.

**Pourquoi ce découpage** : permet de garder un déploiement simple (un seul artefact, une seule base) tout en gardant des frontières de domaine claires, ce qui facilite une extraction future en services séparés (ex : `payment` ou `product` isolés) si la charge le justifie.

## 3. Sécurité

- **Spring Security** + **JWT** :
  - `POST /api/auth/register` (`username` + `password`, `email` optionnel), `POST /api/auth/login` (`username` + `password`) → émettent access token (courte durée, ex. 15 min) + refresh token (ex. 7 jours).
  - `POST /api/auth/refresh` → renouvelle l'access token.
  - Filtre JWT sur chaque requête authentifiée, rôles portés dans les claims du token.
- **OAuth2 Google** : flux `authorization_code` géré côté Angular (ou redirection backend), le backend crée/lie le compte local à partir de l'email Google vérifié (si un compte local avec cet email existe déjà, les comptes sont liés). Pour un compte créé via Google, un `username` est généré automatiquement (dérivé de l'email ou du nom) et modifiable ensuite par l'utilisateur.
- **Autorisations** : annotations `@PreAuthorize` par rôle (`ROLE_ADMIN`, `ROLE_VENDEUR`, `ROLE_USER`) au niveau des contrôleurs/services.
- **CORS** : whitelist du domaine du frontend Angular.
- **Mots de passe** : hashés avec BCrypt.
- **Documents sensibles** (pièces d'identité, RCCM...) : accès restreint par URL signée / endpoint protégé, jamais exposés publiquement.

## 4. Intégration Fedapay

Flux recommandé (API Fedapay + webhook) :
1. Le backend crée une transaction Fedapay (`POST /v1/transactions`) avec le montant total du paiement (qui peut couvrir plusieurs commandes/boutiques d'un même panier) et une référence interne.
2. Le backend génère un lien/token de paiement Fedapay et le restitue au frontend, qui ouvre le widget/la page de paiement Fedapay (Mobile Money : Orange Money, MTN Money, Moov Money, Wave, selon disponibilité en Côte d'Ivoire).
3. L'acheteur valide le paiement sur son téléphone.
4. Fedapay appelle le **webhook** backend (`POST /api/payments/fedapay/webhook`) avec le statut final.
5. Le backend vérifie la signature du webhook, retrouve la transaction via `fedapay_transaction_id`, met à jour `payment_transactions.status`, puis déclenche en cascade la mise à jour des `orders` liées (`PAYEE`), le décrément de stock, le calcul de commission et les notifications.
6. **Idempotence** : le traitement du webhook vérifie que la transaction n'est pas déjà `APPROVED` avant de ré-appliquer les effets.

Variables de configuration (à externaliser, jamais committées) : `FEDAPAY_API_KEY`, `FEDAPAY_ENV` (sandbox/live), `FEDAPAY_WEBHOOK_SECRET`.

## 5. Commission automatique et reversement vendeur

Objectif : prélèvement de la commission et reversement du solde net au vendeur **sans action manuelle de l'admin** (cf. [02-specifications-fonctionnelles.md (UC-10)](./02-specifications-fonctionnelles.md)).

**Décision (Phase 5, 2026-10-01)** : Fedapay n'expose **aucun mécanisme de split payment / sous-comptes / marketplace** (vérifié via `docs.fedapay.com`, API Payouts dédiée aux transferts sortants mais pas de répartition automatique à la création d'une transaction). **Option B retenue** :

- **Option B — Reversement différé automatique** : à la confirmation du webhook (`APPROVED`), le backend calcule `montant_net_vendeur` et crée un enregistrement `vendor_payouts` en statut `EN_ATTENTE`. Un **job planifié** (`@Scheduled(fixedDelay = 15 min)` Spring, cf. `PayoutService`) parcourt les paiements en attente et appelle l'API Payouts de Fedapay (`POST /v1/payouts`, `mode: "mobile_money"`) vers le compte Mobile Money enregistré du vendeur (`shop_payout_accounts`). Statut mis à jour (`ENVOYE`/`ECHEC`) selon la réponse ; relance manuelle possible par l'admin en cas d'échec.

Détails d'intégration confirmés en Phase 5 (testés contre le sandbox Fedapay réel) :
- `POST /v1/transactions` (collecte) : `description`, `amount` (entier, FCFA), `currency.iso: "XOF"`, `callback_url`, `customer: {firstname, lastname, phone_number: {number, country: "CI"}}`. Pas d'email requis.
- `POST /v1/transactions/{id}/token` renvoie `{token, url}` : `url` est le lien de paiement à ouvrir côté acheteur.
- Webhook `X-FEDAPAY-SIGNATURE` : format confirmé `t=<timestamp_unix>,s=<hmac_sha256_hex>` — signature calculée sur `"<t>.<corps_brut>"` avec le secret webhook (convention identique à Stripe). Payload : `{"name": "transaction.created|approved|declined...", "entity": {"id", "status", ...}}`.
- `POST /v1/payouts` : `amount`, `currency.iso: "XOF"`, `mode: "mobile_money"`, `customer: {firstname, lastname, phone_number}`.

Dans les deux cas :
- Le vendeur doit enregistrer un **compte Mobile Money de reversement** (opérateur + numéro) sur sa boutique avant de pouvoir vendre.
- Chaque commande garde en snapshot le taux de commission appliqué et le montant net calculé (`orders.commission_rate`, `orders.commission_amount`).
- Historique consultable par le vendeur (ses reversements) et par l'admin (vue globale, relance manuelle en cas d'échec uniquement — l'échec est une exception, pas le fonctionnement normal).

## 6. Notifications (email)

Envoi d'emails transactionnels (confirmation boutique/produit, nouvelle commande, réponse support...) via **SMTP standard** : `spring-boot-starter-mail` + `JavaMailSender`. Pas de dépendance à l'API propriétaire d'un fournisseur tiers (SendGrid, Mailgun...) — configuration par variables d'environnement (`SMTP_HOST`, `SMTP_PORT`, `EMAIL_USERNAME`, `EMAIL_PASSWORD`).

- **Dev et prod (pour le moment)** : **Gmail SMTP** (`smtp.gmail.com:587`, mot de passe d'application Google) sur le compte `losdiakite@gmail.com`, utilisé à la fois comme expéditeur SMTP et comme **email admin par défaut** (compte admin de démarrage, cf. [07-plan-execution.md](./07-plan-execution.md)). Convient pour démarrer, mais à surveiller : Gmail impose des **limites de volume d'envoi** (quota quotidien) et une délivrabilité moins robuste qu'un service SMTP transactionnel dédié — à migrer vers un fournisseur SMTP dédié dès que le volume d'emails (confirmations, notifications) augmente en production.

Templates d'email en français et anglais selon la langue préférée du destinataire (cf. i18n, §8).

Pour les users sans email renseigné, les notifications applicatives (nouvelle commande, changement de statut...) restent visibles dans l'interface (centre de notifications in-app) même sans envoi d'email — point à garder en tête dans la conception UI.

## 7. Stockage des fichiers (images, documents)

Pour le MVP : stockage sur disque local du serveur (dossier dédié hors du contrôle de version) avec chemin enregistré en base (`*_url`), servi via un endpoint contrôlé par Spring. Évolution possible vers un object storage (S3-compatible) sans impact sur le modèle de données (seule l'URL change).

## 8. Frontend Angular — structure proposée

```
src/app/
├── core/                 # Services transverses : auth, http interceptors (JWT), guards, i18n
├── shared/               # Composants/pipes/directives réutilisables, UI kit (palette verte), composant Stepper
├── features/
│   ├── auth/             # Login, register, Google OAuth callback
│   ├── catalog/          # Liste produits, fiche produit, fiche boutique, recherche/filtres
│   ├── cart/              # Panier, tunnel de commande (en étapes)
│   ├── orders/            # Suivi de commandes (acheteur)
│   ├── shop-request/       # Formulaire adaptatif demande de boutique, présenté en étapes (wizard)
│   ├── vendor/             # Espace vendeur (layout sidebar) : boutique, produits, commandes reçues
│   └── admin/              # Back-office admin (layout sidebar) : validations boutiques/produits, users, stats
└── layout/
    ├── public-layout/      # Header + footer, navigation du site public/catalogue
    ├── vendor-layout/       # Layout back-office vendeur : sidebar de navigation à gauche + zone de contenu
    └── admin-layout/        # Layout back-office admin : sidebar de navigation à gauche + zone de contenu
```

- Routing avec lazy loading par feature.
- Guards de rôle (`authGuard`, `roleGuard`) pour protéger `vendor/*` et `admin/*`.
- Interceptor HTTP pour injecter le JWT et gérer le refresh automatique.
- Formulaires réactifs (Reactive Forms), **découpés en étapes (stepper/wizard)** pour tout formulaire à plusieurs sections (demande de boutique, création de produit) — jamais un unique long formulaire (cf. [02-specifications-fonctionnelles.md](./02-specifications-fonctionnelles.md)).
- **Back-office (espace vendeur et back-office admin)** : layout avec **menu de navigation latéral à gauche (sidebar)**, repliable/collapsible, listant les sections (ex. admin : Tableau de bord, Demandes de boutiques, Produits à valider, Utilisateurs, Commandes, Statistiques, Paramètres) ; zone de contenu principale à droite avec un en-tête contextuel. Détail visuel dans [06-charte-graphique.md](./06-charte-graphique.md).
- **Internationalisation (i18n)** : **`@ngx-translate/core`** (tranché) — choisi plutôt que `@angular/localize` car il permet un **changement de langue au runtime** sans rebuild/redéploiement séparé par locale, ce qui convient mieux à un sélecteur de langue dynamique dans le header/la sidebar ; gère **français (par défaut) et anglais** via fichiers de traduction (`fr.json` / `en.json`), aucun texte en dur dans les composants.

Côté backend, les messages renvoyés à l'utilisateur (erreurs de validation, notifications) sont également bilingues : `MessageSource` Spring avec `messages_fr.properties` / `messages_en.properties`, langue déterminée par l'en-tête HTTP `Accept-Language` envoyé par le frontend selon la langue active.

## 9. Environnements & configuration

- `application.yml` par profil Spring (`dev`, `prod`) : connexion DB, secrets JWT, clés Fedapay, config SMTP — via variables d'environnement, jamais en dur.
- `environment.ts` / `environment.prod.ts` côté Angular pour l'URL de l'API.
- **Chargement local via `.env`** : en dev, `backend/.env` (non committé, cf. `.gitignore`) peut définir `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USERNAME`/`DB_PASSWORD` (et autres variables) sans avoir à les positionner manuellement dans chaque session de terminal. Chargé automatiquement par un `EnvironmentPostProcessor` maison (`shop.le_paysan.backend.config.DotenvEnvironmentPostProcessor`, enregistré via `META-INF/spring.factories`) — la librairie tierce `spring-dotenv` a été testée mais ne se charge pas correctement avec Spring Boot 4.1.1 (mécanisme d'enregistrement obsolète). De vraies variables d'environnement OS, si définies, restent prioritaires sur `.env`.
- Base de données locale de développement : rôle applicatif dédié `le_paysan` (pas le superuser `postgres`), propriétaire de la base `le_paysan`, créé via :
  ```sql
  CREATE ROLE le_paysan WITH LOGIN PASSWORD '...';
  ALTER DATABASE le_paysan OWNER TO le_paysan;
  GRANT ALL PRIVILEGES ON DATABASE le_paysan TO le_paysan;
  ```
  Le mot de passe réel est stocké uniquement dans `backend/.env` (non committé), jamais dans la documentation versionnée.

## 10. Déploiement (cible simple)

- Backend : build Maven → artefact `.jar` → conteneur Docker (à mettre en place).
- Frontend : build Angular → fichiers statiques servis par un serveur web (Nginx) ou hébergement statique.
- DB : instance PostgreSQL managée ou auto-hébergée.
- (Détails CI/CD à définir — point ouvert, cf. §11).

## 11. Points ouverts

- Hébergement cible (VPS, cloud managé) non encore défini.
- Mise en place CI/CD (GitHub Actions présent dans `.github/`, à configurer).
- **Note d'installation frontend** : `npm install` seul échoue sur cette config Angular (bug connu de l'arborist npm 10.9.x sur la résolution des peer dependencies de `vitest`/`@vitejs/devtools*`) → utiliser `npm install --legacy-peer-deps`. À surveiller : une future mise à jour npm pourra lever ce contournement.
- Migrer vers un fournisseur SMTP dédié en production quand le volume d'envoi dépasse les limites de Gmail (tranché pour le démarrage : Gmail SMTP sur `losdiakite@gmail.com`, cf. §6).
