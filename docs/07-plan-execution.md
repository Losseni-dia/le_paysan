# 07 — Plan d'exécution

Statut : v1.0 — 2026-10-01. À tenir à jour au fil de l'avancement (cocher les tâches, ajouter la date de complétion).

## 1. Principe

Découpage en phases livrables, chacune apportant une valeur testable de bout en bout. Le MVP correspond aux phases 0 à 7.

## 2. Phase 0 — Socle technique
- [x] Backend : structure des packages (`auth`, `user`, `shop`, `product`, `category`, `cart`, `order`, `payment`, `commission`, `admin`, `common`, `config`). *(2026-10-01)*
- [x] Configuration Postgres (`application.yml`, profils `dev`/`prod`), rôle applicatif `le_paysan` créé (propriétaire de la base), [sql/schema.sql](./sql/schema.sql) exécuté (26 tables + index), connexion backend vérifiée (démarrage OK, Hikari connecté). *(2026-10-01)*
- [x] Frontend : `ng new` dans `frontend/` (SCSS, routing, standalone, sans SSR), structure de dossiers (`core`, `shared/ui`, `layout`), intégration des tokens CSS de la charte graphique dans `styles.scss`. *(2026-10-01 — nécessite `npm install --legacy-peer-deps`, cf. [04-architecture-technique.md §11](./04-architecture-technique.md).)*
- [x] Mise en place i18n Angular (`@ngx-translate/core`, FR par défaut + EN), fichiers de traduction `public/i18n/fr.json`/`en.json`, composant `language-switcher` partagé. *(2026-10-01)*
- [x] Composant Stepper/wizard générique (`shared/ui/stepper`) — header d'étapes + navigation précédent/suivant + projection de contenu ; intégration dans les formulaires concrets prévue en Phase 2/3. *(2026-10-01)*
- [x] Layouts back-office (`vendor-layout`, `admin-layout`) avec sidebar de navigation à gauche, repliable, topbar (notifications + langue). *(2026-10-01)*
- [x] Intégration Leaflet + OpenStreetMap (`shared/ui/map-picker`) pour le sélecteur de localisation boutique ; icônes de marqueur chargées depuis un CDN (à auto-héberger avant prod). *(2026-10-01)*
- [x] Centre de notifications in-app (`shared/ui/notification-bell` + `core/notifications/notification.service.ts`) — composant partagé, consommé dans les trois layouts ; branchement aux vrais endpoints API en Phase 1+. *(2026-10-01)*
- [x] Logo intégré (favicon + header public + sidebars back-office) — fichier à optimiser avant prod, cf. [06-charte-graphique.md](./06-charte-graphique.md). *(2026-10-01)*
- [x] Mise en place CI basique (build backend + build frontend) dans `.github/workflows/ci.yml`. *(2026-10-01)*

## 3. Phase 1 — Authentification & utilisateurs
- [x] Inscription / connexion par login + mot de passe (email optionnel), hashage BCrypt. *(2026-10-01 — testé end-to-end backend+frontend.)*
- [x] Trois cases à cocher obligatoires et distinctes à l'inscription (CGU, CGV, Politique de confidentialité), horodatées et versionnées (`users.cgu_accepted_at`/`cgv_accepted_at`/`privacy_accepted_at`). *(2026-10-01 — pour la première connexion via Google : voir OAuth2 Google ci-dessous, non implémenté.)*
- [x] Bandeau de consentement cookies, affiché dès la première visite (avant inscription). *(2026-10-01)*
- [x] Pages légales statiques (CGU, CGV, Mentions légales, Politique de confidentialité, Politique de cookies) + liens en footer. *(2026-10-01 — contenu condensé depuis [08-mentions-legales-cgu-rgpd.md](./08-mentions-legales-cgu-rgpd.md) ; Conditions vendeur à écrire en Phase 2 avec le formulaire boutique.)*
- [x] JWT (access + refresh), endpoint `/auth/refresh`. *(2026-10-01)*
- [ ] OAuth2 Google (backend + intégration frontend). **Non démarré** : en attente de la création du projet Google Cloud et des identifiants Client ID/Secret par l'utilisateur.
- [x] Gestion de profil (`/users/me`), y compris mise à jour (`PUT /users/me`). *(2026-10-01)*
- [x] Rôles `ROLE_USER` / `ROLE_VENDEUR` / `ROLE_ADMIN`, guards Angular (`authGuard`, `roleGuard`), `@PreAuthorize` backend (ex. endpoints admin recovery). *(2026-10-01)*
- [x] Compte admin de démarrage (seed) : email `losdiakite@gmail.com`, mot de passe généré affiché au log au premier démarrage. *(2026-10-01)*
- [x] Configuration SMTP (Gmail SMTP, `losdiakite@gmail.com`) — email de réinitialisation de mot de passe testé (envoi sans erreur). *(2026-10-01)*
- [x] Capture optionnelle du selfie de sécurité à l'inscription, upload via `PUT /users/me/security-selfie` (stockage local `FileStorageService`). *(2026-10-01)*
- [x] Récupération de compte : mot de passe oublié par email (JWT à usage unique, 30 min) + demande de récupération par selfie pour les comptes sans email (création + liste/détail/validation/rejet admin, mot de passe temporaire généré). *(2026-10-01 — back-office admin encore sans interface graphique, testé via API ; UI de modération prévue en Phase 2/6 avec le reste du back-office admin.)*

## 4. Phase 2 — Boutiques
- [x] Formulaire adaptatif demande de boutique (frontend), en étapes (stepper) : Type (Personne physique / Entreprenant / Personne morale) → Infos boutique (avec sélecteur de localisation carte obligatoire) → Justificatifs → Récapitulatif. *(2026-10-01 — testé dans le navigateur de bout en bout.)*
- [x] Endpoint `POST /shops/requests` (multipart, upload documents, latitude/longitude). *(2026-10-01)*
- [x] Case à cocher obligatoire "J'accepte les Conditions vendeur (CGV)" à la soumission, horodatée et versionnée (`shops.cgv_vendeur_accepted_at`). *(2026-10-01)*
- [x] Back-office admin : liste des demandes (filtrable par statut), détail (infos + documents KYC via endpoint protégé), validation/rejet avec motif. *(2026-10-01)*
- [x] Attribution automatique de `ROLE_VENDEUR` à la validation. *(2026-10-01)*
- [x] Espace vendeur : page "Ma boutique" avec statut, infos, carte de localisation. *(2026-10-01)*
- [x] Espace vendeur : gestion du compte Mobile Money de reversement (requis avant publication produit). *(2026-10-01)*
- [x] Notifications in-app (soumission, validation, rejet, suspension) ; email en complément reporté (pas encore de template dédié, le canal in-app est le canal obligatoire testé). *(2026-10-01)*

**Bug corrigé en cours de route** : au rechargement complet d'une page protégée, le guard de route s'exécutait avant la fin de la restauration de session (refresh token asynchrone), provoquant une redirection intempestive vers `/connexion`. Corrigé via un `provideAppInitializer` qui attend la restauration avant que le routeur n'évalue les guards (cf. `frontend/src/app/app.config.ts`).

## 5. Phase 3 — Produits & catalogue
- [ ] CRUD produit vendeur en étapes (stepper) : Infos générales → Photos → Livraison → Récapitulatif.
- [ ] Back-office admin : validation/rejet produit avec motif.
- [ ] Catalogue public : liste, recherche, filtres, fiche produit, fiche boutique (avec carte de localisation + lien itinéraire).
- [ ] Gestion des catégories (seed + admin CRUD).

## 6. Phase 4 — Panier & tunnel de commande
- [ ] Panier multi-produits (multi-boutiques).
- [ ] Choix du mode de livraison par produit + saisie adresse si besoin.
- [ ] Récapitulatif de commande (sous-totaux, livraison, total).
- [ ] Création des commandes (une par boutique) en statut `EN_ATTENTE_PAIEMENT`.

## 7. Phase 5 — Paiement Fedapay & reversement automatique
- [ ] Vérifier auprès de Fedapay la disponibilité d'un paiement scindé (split payment) ou d'une API de transfert/payout — trancher Option A vs Option B (cf. [04-architecture-technique.md §5](./04-architecture-technique.md)).
- [ ] Intégration API Fedapay (création transaction).
- [ ] Widget/page de paiement côté frontend.
- [ ] Endpoint webhook + vérification signature + idempotence.
- [ ] Mise à jour des commandes à la confirmation, décrément de stock.
- [ ] Calcul et enregistrement de la commission par commande (snapshot taux + montant).
- [ ] Reversement automatique au vendeur (split payment ou job planifié de payout selon l'option retenue), table `vendor_payouts`.
- [ ] Back-office admin : vue des reversements, relance en cas d'échec.
- [ ] Page de confirmation / échec de paiement côté frontend.

## 8. Phase 6 — Suivi de commande & back-office
- [ ] Espace vendeur : liste des commandes reçues, changement de statut.
- [ ] Espace acheteur : suivi de ses commandes.
- [ ] Back-office admin : vue globale commandes, statistiques (ventes, commissions, boutiques actives).
- [ ] Notifications in-app (canal obligatoire) + email en complément aux étapes clés de la commande.

## 9. Phase 7 — Avis & Support
- [ ] Modèle avis produit/boutique (`product_reviews`, `shop_reviews`), vérification achat livré.
- [ ] Frontend : formulaire de notation (1-5 étoiles + commentaire) sur fiche produit/boutique achetée, affichage moyenne/nombre d'avis.
- [ ] Back-office admin : modération des avis (masquer/réafficher, motif).
- [ ] Formulaire de contact public (`POST /contact`), page "Contact/Support" frontend.
- [ ] Back-office admin : file de messages de support (statuts, réponse par email).

## 10. Phase 8 — Durcissement & mise en production (post-MVP immédiat)
- [ ] Rate limiting sur l'auth et sur le formulaire de contact.
- [ ] Tests automatisés (unitaires + intégration) sur les flux critiques (paiement, validations).
- [ ] Logs structurés, monitoring basique.
- [ ] Dockerisation backend + frontend.
- [ ] Choix hébergement + déploiement.

## 11. Évolutions futures (hors périmètre immédiat)
- Messagerie directe acheteur-vendeur en temps réel (chat).
- Transporteurs tiers / tracking livraison.
- Application mobile.
- Vérification automatisée des numéros RCCM/NCC/récépissé (API CEPICI/DGI) et reconnaissance faciale automatisée pour la récupération de compte.

## 12. Suivi

| Phase | Statut | Date de début | Date de fin |
|---|---|---|---|
| Phase 0 | Terminé | 2026-10-01 | 2026-10-01 |
| Phase 1 | Quasi terminé (OAuth Google en attente) | 2026-10-01 | - |
| Phase 2 | Terminé | 2026-10-01 | 2026-10-01 |
| Phase 3 | À faire | - | - |
| Phase 4 | À faire | - | - |
| Phase 5 | À faire | - | - |
| Phase 6 | À faire | - | - |
| Phase 7 | À faire | - | - |
| Phase 8 | À faire | - | - |

Ce tableau et les cases à cocher ci-dessus doivent être mis à jour au fur et à mesure de l'avancement réel du projet.
