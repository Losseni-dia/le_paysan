# 07 — Plan d'exécution

Statut : v1.0 — 2026-10-01. À tenir à jour au fil de l'avancement (cocher les tâches, ajouter la date de complétion).

## 1. Principe

Découpage en phases livrables, chacune apportant une valeur testable de bout en bout. Le MVP correspond aux phases 0 à 7.

## 2. Phase 0 — Socle technique
- [ ] Backend : structure des packages (`auth`, `user`, `shop`, `product`, `category`, `cart`, `order`, `payment`, `commission`, `admin`, `common`, `config`).
- [ ] Configuration Postgres (`application.yml`, profils `dev`/`prod`), exécution de [sql/schema.sql](./sql/schema.sql).
- [ ] Frontend : `ng new` dans `frontend/`, structure de dossiers (§6 de [04-architecture-technique.md](./04-architecture-technique.md)), intégration des tokens CSS de la charte graphique.
- [ ] Mise en place i18n Angular (FR par défaut + EN), fichiers de traduction `fr.json`/`en.json`, sélecteur de langue partagé.
- [ ] Composant Stepper/wizard générique (shared UI kit) réutilisé par les formulaires multi-étapes.
- [ ] Layouts back-office (`vendor-layout`, `admin-layout`) avec sidebar de navigation à gauche.
- [ ] Choix et intégration d'une librairie de carte (Leaflet + OpenStreetMap recommandé, gratuit, sans clé API — alternative Google Maps à évaluer si besoin de plus de précision) pour le sélecteur de localisation boutique et l'affichage carte.
- [ ] Centre de notifications in-app (icône cloche, badge non-lues, liste) — composant partagé, consommé dans les deux back-offices et le site public.
- [ ] Mise en place CI basique (build backend + build frontend) dans `.github/workflows`.

## 3. Phase 1 — Authentification & utilisateurs
- [ ] Inscription / connexion par login + mot de passe (email optionnel), hashage BCrypt.
- [ ] Trois cases à cocher obligatoires et distinctes à l'inscription (CGU, CGV, Politique de confidentialité), horodatées et versionnées (`users.cgu_accepted_at`/`cgv_accepted_at`/`privacy_accepted_at`), y compris pour la première connexion via Google, cf. [08-mentions-legales-cgu-rgpd.md](./08-mentions-legales-cgu-rgpd.md).
- [ ] Bandeau de consentement cookies, affiché dès la première visite (avant inscription).
- [ ] Pages légales statiques (Mentions légales, CGU, CGV, Conditions vendeur, Politique de confidentialité, Politique de cookies) + liens en footer + bandeau cookies minimal.
- [ ] JWT (access + refresh), endpoint `/auth/refresh`.
- [ ] OAuth2 Google (backend + intégration frontend).
- [ ] Gestion de profil (`/users/me`).
- [ ] Rôles `ROLE_USER` / `ROLE_VENDEUR` / `ROLE_ADMIN`, guards Angular, `@PreAuthorize` backend.
- [ ] Compte admin de démarrage (seed) : email `losdiakite@gmail.com`.
- [ ] Configuration SMTP (Gmail SMTP, `losdiakite@gmail.com`, mot de passe d'application Google — en variable d'environnement, jamais en dur dans le code).
- [ ] Capture optionnelle du selfie de sécurité à l'inscription (et depuis le profil).
- [ ] Récupération de compte : mot de passe oublié par email (classique) + demande de récupération par selfie pour les comptes sans email (back-office admin de validation).

## 4. Phase 2 — Boutiques
- [ ] Formulaire adaptatif demande de boutique (frontend), en étapes (stepper) : Type (Personne physique / Entreprenant / Personne morale) → Infos boutique (avec sélecteur de localisation carte obligatoire) → Justificatifs → Récapitulatif.
- [ ] Endpoint `POST /shops/requests` (multipart, upload documents, latitude/longitude).
- [ ] Case à cocher obligatoire "J'accepte les Conditions vendeur (CGV)" à la soumission, horodatée et versionnée (`shops.cgv_vendeur_accepted_at`).
- [ ] Back-office admin : liste des demandes, détail, validation/rejet avec motif.
- [ ] Attribution automatique de `ROLE_VENDEUR` à la validation.
- [ ] Espace vendeur : page "Ma boutique" avec carte de localisation.
- [ ] Espace vendeur : gestion du compte Mobile Money de reversement (requis avant publication produit).
- [ ] Notifications in-app (soumission, validation, rejet) + email en complément si renseigné.

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
| Phase 0 | À faire | - | - |
| Phase 1 | À faire | - | - |
| Phase 2 | À faire | - | - |
| Phase 3 | À faire | - | - |
| Phase 4 | À faire | - | - |
| Phase 5 | À faire | - | - |
| Phase 6 | À faire | - | - |
| Phase 7 | À faire | - | - |
| Phase 8 | À faire | - | - |

Ce tableau et les cases à cocher ci-dessus doivent être mis à jour au fur et à mesure de l'avancement réel du projet.
