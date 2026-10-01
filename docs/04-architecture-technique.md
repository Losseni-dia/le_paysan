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

Objectif : prélèvement de la commission et reversement du solde net au vendeur **sans action manuelle de l'admin** (cf. [02-specifications-fonctionnelles.md (UC-10)](./02-specifications-fonctionnelles.md)). Deux implémentations possibles, le choix définitif dépendant des capacités réelles de l'API Fedapay (point ouvert, §11) :

- **Option A — Paiement scindé (split payment)**, si Fedapay expose un mécanisme de sous-comptes/marketplace : à la création de la transaction (étape 1 du flux Fedapay ci-dessus), le backend indique la répartition (part vendeur / part plateforme) ; Fedapay route les fonds directement aux deux parties dès la validation du paiement par l'acheteur. Le plus simple et le plus robuste si disponible.
- **Option B — Reversement différé automatique**, sinon : à la confirmation du webhook (`APPROVED`), le backend calcule `montant_net_vendeur` et crée un enregistrement `vendor_payouts` en statut `EN_ATTENTE`. Un **job planifié** (ex. toutes les heures ou quotidien, `@Scheduled` Spring) parcourt les paiements en attente et appelle l'API de transfert sortant (payout) de Fedapay vers le compte Mobile Money enregistré du vendeur (`shop_payout_accounts`). Statut mis à jour (`ENVOYE`/`ECHEC`) selon la réponse.

Dans les deux cas :
- Le vendeur doit enregistrer un **compte Mobile Money de reversement** (opérateur + numéro) sur sa boutique avant de pouvoir vendre.
- Chaque commande garde en snapshot le taux de commission appliqué et le montant net calculé (`orders.commission_rate`, `orders.commission_amount`).
- Historique consultable par le vendeur (ses reversements) et par l'admin (vue globale, relance manuelle en cas d'échec uniquement — l'échec est une exception, pas le fonctionnement normal).

## 6. Notifications (email)

Envoi d'emails transactionnels (confirmation boutique/produit, nouvelle commande, réponse support...) via **SMTP standard** : `spring-boot-starter-mail` + `JavaMailSender`. Pas de dépendance à l'API propriétaire d'un fournisseur tiers (SendGrid, Mailgun...) — configuration par variables d'environnement (`SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_FROM`).

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
- **Internationalisation (i18n)** : `@angular/localize` ou `ngx-translate` (à trancher, cf. point ouvert §11) pour gérer **français (par défaut) et anglais** ; sélecteur de langue dans le header (site public) et dans la sidebar (back-office) ; toutes les chaînes d'UI passent par des fichiers de traduction (`fr.json` / `en.json`), aucun texte en dur dans les composants.

Côté backend, les messages renvoyés à l'utilisateur (erreurs de validation, notifications) sont également bilingues : `MessageSource` Spring avec `messages_fr.properties` / `messages_en.properties`, langue déterminée par l'en-tête HTTP `Accept-Language` envoyé par le frontend selon la langue active.

## 9. Environnements & configuration

- `application.yml` par profil Spring (`dev`, `prod`) : connexion DB, secrets JWT, clés Fedapay, config SMTP — via variables d'environnement, jamais en dur.
- `environment.ts` / `environment.prod.ts` côté Angular pour l'URL de l'API.

## 10. Déploiement (cible simple)

- Backend : build Maven → artefact `.jar` → conteneur Docker (à mettre en place).
- Frontend : build Angular → fichiers statiques servis par un serveur web (Nginx) ou hébergement statique.
- DB : instance PostgreSQL managée ou auto-hébergée.
- (Détails CI/CD à définir — point ouvert, cf. §11).

## 11. Points ouverts

- Hébergement cible (VPS, cloud managé) non encore défini.
- Mise en place CI/CD (GitHub Actions présent dans `.github/`, à configurer).
- Choix de la librairie i18n Angular (`@angular/localize` vs `ngx-translate`) à trancher en phase 0.
- Confirmer auprès de la documentation Fedapay si un **paiement scindé (split payment)** ou un **transfert/payout programmatique** vers un compte Mobile Money tiers est disponible (nécessaire pour le reversement automatique aux vendeurs, cf. §5).
- Migrer vers un fournisseur SMTP dédié en production quand le volume d'envoi dépasse les limites de Gmail (tranché pour le démarrage : Gmail SMTP sur `losdiakite@gmail.com`, cf. §6).
