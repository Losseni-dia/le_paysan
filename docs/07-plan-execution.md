# 07 — Plan d'exécution

Statut : v1.1 — 2026-10-24. À tenir à jour au fil de l'avancement (cocher les tâches, ajouter la date de complétion).

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
- [x] CRUD produit vendeur en étapes (stepper) : Infos générales → Photos → Livraison → Récapitulatif ; la modification repasse le produit en `EN_ATTENTE` de re-validation. *(2026-10-01 — testé de bout en bout dans le navigateur, y compris upload de photos.)*
- [x] Back-office admin : liste filtrable par statut, détail, validation/rejet produit avec motif. *(2026-10-01)*
- [x] Espace vendeur : page "Mes produits" (liste, statut, dépublier/republier un produit validé). *(2026-10-01)*
- [x] Catalogue public : liste, recherche, filtres (catégorie, prix min/max, mode de livraison), pagination, fiche produit, fiche boutique (avec carte de localisation + lien itinéraire OpenStreetMap). *(2026-10-01 — testé de bout en bout, accès anonyme confirmé.)*
- [x] Gestion des catégories (seed + admin : liste + création, pas de modification/suppression côté API). *(2026-10-01 — modification/suppression ajoutées ensuite, cf. commits `e18cb09`/`f99fba2`.)*
- [x] URLs produit/boutique avec slug plutôt que l'id numérique (`/produits/nom-produit-nom-ferme`, `/boutiques/nom-boutique`), rétro-compatible avec les anciens liens par id. *(2026-10-23, cf. commit `b692c55`.)*

**Faille de sécurité corrigée en cours de route** : les photos produit n'étaient accessibles que via l'endpoint `/api/files/**` authentifié, ce qui aurait empêché leur affichage dans le catalogue public (balises `<img>` sans en-tête `Authorization`). Ajout d'une règle `permitAll` dédiée à `GET /api/files/products/**`. En creusant, constat qu'une règle `permitAll` trop large sur `/api/shops/**` exposait aussi par erreur `/api/shops/me` et `/api/shops/me/payout-account` sans authentification (resserrée aux seules routes publiques `/api/shops` et `/api/shops/{id}`), et que le logo/couverture boutique étaient stockés sous la catégorie `shop-kyc` (authentifiée) : séparés dans une nouvelle catégorie `shop-public` rendue publique en lecture, pour que la fiche boutique publique puisse les afficher (cf. `backend/.../config/SecurityConfig.java`, `backend/.../shop/ShopService.java`).

## 6. Phase 4 — Panier & tunnel de commande
- [x] Panier multi-produits (multi-boutiques) : `GET/POST/PUT/DELETE /api/cart`, un panier par utilisateur, prix lu en direct sur le produit (non snapshotté en panier). *(2026-10-01)*
- [x] Choix du mode de livraison par produit + saisie adresse si besoin (validée côté backend : mode proposé par le produit, adresse obligatoire si `LIVRAISON_DOMICILE`). *(2026-10-01)*
- [x] Récapitulatif de commande (sous-totaux, livraison, total) — page panier avec regroupement par boutique + page de confirmation après commande. *(2026-10-01)*
- [x] Création des commandes (une par boutique) en statut `EN_ATTENTE_PAIEMENT` : `POST /api/orders` (checkout), snapshot nom/prix produit, numéro de commande généré, panier vidé après commande. *(2026-10-01 — testé de bout en bout dans le navigateur.)*
- [x] Icône panier avec compteur d'articles dans le header public (à droite, à côté du sélecteur de langue). *(2026-10-01)*

**Décision de scope** : `orders.commission_rate`/`commission_amount` sont NOT NULL en base et doivent donc être figés dès la création de la commande (Phase 4), pas seulement à la confirmation du paiement (Phase 5) comme décrit informellement dans UC-08. Un seul taux de commission global est utilisé par commande (table `commission_settings`, taux par défaut à 10% seedé au démarrage) : le modèle ne porte qu'un taux unique par commande, donc pas de proratisation par catégorie si une commande mélange plusieurs catégories de produits — amélioration possible post-MVP si besoin.

**Bug corrigé en cours de route** : juste après l'ajout d'un article, la réponse de `POST /api/cart/items` renvoyait un panier vide alors que l'article était bien persisté en base. Cause : la collection `Cart.items` (chargement paresseux Hibernate) restait "déjà initialisée à vide" dans le contexte de persistance de la transaction en cours, car l'article avait été inséré via `CartItemRepository` directement (sans passer par `cart.getItems().add(...)`), donc Hibernate ne la rechargeait pas. Corrigé en lisant toujours les lignes du panier via une requête directe (`CartItemRepository.findByCartIdOrderByIdAsc`) plutôt qu'en naviguant `cart.getItems()`, y compris dans `OrderService.checkout()`.

## 7. Phase 5 — Paiement Fedapay & reversement automatique
- [x] Vérifié auprès de la documentation Fedapay (`docs.fedapay.com`) : aucun mécanisme de split payment/sous-comptes. **Option B retenue** (reversement différé via job planifié) — détails et endpoints confirmés dans [04-architecture-technique.md §5](./04-architecture-technique.md). *(2026-10-04)*
- [x] Intégration API Fedapay : `FedapayClient` (création transaction, génération du lien de paiement, création de payout), `POST /api/payments` (checkout vers Fedapay, exige un téléphone renseigné sur le profil acheteur). *(2026-10-04 — testé contre le sandbox réel, lien de paiement généré avec succès.)*
- [x] Page de paiement côté frontend : bouton "Payer maintenant" sur la confirmation de commande et sur "Mes commandes" (retry si paiement abandonné/échoué), redirection vers le lien Fedapay. *(2026-10-04)*
- [x] Endpoint webhook (`POST /api/payments/fedapay/webhook`, public) + vérification de signature + idempotence (ignore si la transaction n'est plus `PENDING`). *(2026-10-04 — testé avec un vrai webhook sandbox via ngrok : format de signature confirmé `X-FEDAPAY-SIGNATURE: t=<timestamp>,s=<hmac_sha256_hex>` sur `"<t>.<corps_brut>"`, différent de l'hypothèse initiale.)*
- [x] Mise à jour des commandes à la confirmation (`PAYEE`), décrément de stock. *(2026-10-04 — vérifié : commande passée à PAYEE, stock décrémenté de la quantité achetée.)*
- [x] Calcul et enregistrement de la commission par commande (fait dès la Phase 4, cf. note ci-dessus). *(2026-10-01)*
- [x] Reversement automatique au vendeur : job planifié (`PayoutService`, toutes les 15 min) crée un `vendor_payouts` `EN_ATTENTE` à l'approbation du paiement, puis appelle `POST /v1/payouts` Fedapay. *(2026-10-04 — création du `vendor_payouts` vérifiée avec le bon montant net (sous-total − commission + livraison) ; envoi réel au vendeur non confirmé en sandbox faute de numéro de test reconnu pour le compte Mobile Money vendeur.)*
- [x] Back-office admin : page "Reversements" (`/admin/reversements`), filtrable par statut, relance manuelle en cas d'échec. *(2026-10-04)*
- [x] Page de retour de paiement côté frontend (`/commandes/paiement-retour`, URL de callback Fedapay) : affiche le statut de la commande (le webhook, pas ce retour, fait foi). *(2026-10-04)*

**Bug corrigé en cours de route** : `ObjectMapper` (Jackson) n'était pas un bean Spring disponible à l'injection — `spring-boot-starter-webmvc` (Spring Boot 4.1.1) ne déclare la dépendance Jackson qu'en portée `runtime`, invisible au code applicatif qui en a besoin directement (ici pour parser le payload webhook). Corrigé en ajoutant `jackson-databind` en dépendance directe + un bean `ObjectMapper` explicite (`JacksonConfig`).

## 8. Phase 6 — Suivi de commande & back-office
- [x] Espace vendeur : liste des commandes reçues, changement de statut. *(backend `OrderVendorController`, web `features/vendor/orders-list`, mobile `vendor_orders_screen.dart`.)*
- [x] Espace acheteur : suivi de ses commandes. *(backend `OrderController`, web `features/account/my-orders`, mobile `orders_screen.dart`, timeline de statuts partagée web `order-timeline`.)*
- [x] Back-office admin : vue globale commandes, statistiques (ventes, commissions, boutiques actives). *(backend `OrderAdminController`, web `features/admin/dashboard`, mobile `admin_dashboard_screen.dart`.)*
- [x] Notifications in-app aux étapes clés de la commande. *(6 points d'envoi dans `OrderService.java`.)* — [ ] **Email en complément : non fait.** `EmailService.java` ne couvre que la réinitialisation de mot de passe, aucun template lié aux commandes.
- [x] **Hors plan initial, ajouté le 2026-10-23/24** : refonte complète du système de livraison — livreur en inscription volontaire (compte dédié ou passage de rôle depuis un compte existant), diffusion d'une commande expédiée aux livreurs disponibles à proximité (25km), acceptation atomique (premier arrivé), le tout backend + web + mobile (cf. commit `337fa88`).

## 9. Phase 7 — Avis & Support
- [x] Modèle avis produit/boutique (`product_reviews`, `shop_reviews`), vérification achat livré.
- [x] Formulaire de notation (1-5 étoiles + commentaire) sur fiche produit/boutique achetée, affichage moyenne/nombre d'avis. *(Web : `shared/ui/review-section`. Mobile ajouté le 2026-10-24 : `core/review/`, `product_review_section.dart`, `write_review_screen.dart` — accès direct depuis une commande livrée sans passer par la fiche produit.)*
- [x] Back-office admin : modération des avis (masquer/réafficher, motif). *(backend `ReviewAdminController`, mobile `admin_reviews_screen.dart` ; page web à reconfirmer.)*
- [x] Formulaire de contact public (`POST /contact`), page "Contact/Support" frontend. *(backend `ContactController`, web `features/contact`.)* — [ ] **Mobile : pas fait**, aucun écran contact.
- [ ] Back-office admin : file de messages de support (statuts, réponse par email). **Pas fait** : la table `contact_messages` et le service existent côté backend, mais aucun `ContactAdminController`, aucune page web ni écran mobile pour la traiter.

## 10. Phase 8 — Durcissement & mise en production (post-MVP immédiat)

**Audit sécurité complet réalisé le 2026-10-23, cf. [audit-securite.md](./audit-securite.md). Aucun point critique ; 4 points hauts à corriger avant prod (HTTPS backend, contrôle d'accès fin sur les fichiers KYC, verrou optimiste sur le stock produit, révocation JWT au changement de mot de passe).**

- [x] Rate limiting sur l'auth (`AuthRateLimitFilter`, 20 req/min/IP sur `/api/v1/auth/**`). — [ ] **Formulaire de contact : pas couvert** (le filtre ne matche que `/auth/**`).
- [ ] Tests automatisés (unitaires + intégration) sur les flux critiques (paiement, validations). **Quasi aucun** : 1 fichier de test backend (démarrage auto-généré), 1 `.spec.ts` frontend, 1 fichier mobile.
- [ ] Logs structurés, monitoring basique. **Pas fait** : pas de `spring-boot-starter-actuator`/Micrometer dans le `pom.xml`.
- [ ] Dockerisation backend + frontend. **Pas fait** : aucun `Dockerfile` ni `docker-compose.yml`.
- [ ] Choix hébergement + déploiement. **Décision humaine en attente** — actuellement testé via tunnel ngrok vers le backend local, pas d'hébergement permanent.

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
| Phase 3 | Terminé (+ slugs produit/boutique) | 2026-10-01 | 2026-10-23 |
| Phase 4 | Terminé | 2026-10-01 | 2026-10-01 |
| Phase 5 | Quasi terminé (reversement sortant réel non confirmé en sandbox) | 2026-10-04 | 2026-10-04 |
| Phase 6 | Quasi terminé (email aux étapes commande manquant ; système de livraison ajouté hors plan) | 2026-10-23 | - |
| Phase 7 | Quasi terminé (contact mobile manquant ; pas de back-office support) | 2026-10-24 | - |
| Phase 8 | Démarré (audit sécurité fait, rien d'autre) | 2026-10-23 | - |

Ce tableau et les cases à cocher ci-dessus doivent être mis à jour au fur et à mesure de l'avancement réel du projet.
