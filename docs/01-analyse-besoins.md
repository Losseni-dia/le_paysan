# 01 — Analyse des besoins

Statut : v1.0 — 2026-10-01

## 1. Contexte

**Le Paysan** est une marketplace en ligne qui met en relation des producteurs agricoles (fermes, exploitations individuelles ou entreprises agricoles) avec des acheteurs. Chaque producteur dispose de sa propre boutique au sein de la plateforme, dans laquelle il publie ses produits. La plateforme garantit la fiabilité des vendeurs et la qualité du catalogue via un double contrôle administrateur : validation du profil vendeur, puis validation de chaque produit avant sa mise en ligne.

## 2. Objectifs du projet

- Permettre à des producteurs agricoles (personnes physiques ou morales) de vendre leurs produits en ligne sans compétence technique.
- Offrir aux acheteurs un catalogue de confiance (vendeurs et produits vérifiés par un administrateur).
- Fournir un parcours d'achat simple : achat direct d'un produit ou panier multi-produits, paiement par Mobile Money via Fedapay.
- Donner à la plateforme un modèle économique via une commission sur les ventes.

## 3. Acteurs

| Acteur | Description |
|---|---|
| **Visiteur** | Utilisateur non authentifié. Peut parcourir le catalogue, voir les boutiques et les fiches produits. |
| **User (acheteur)** | Visiteur inscrit. Peut acheter, gérer son panier, ses commandes, son profil. Peut demander la création d'une boutique. |
| **Vendeur** | User dont la demande de boutique a été validée par un admin. Gère sa boutique et ses produits. Reste aussi un acheteur potentiel (même compte, rôle additionnel). |
| **Admin** | Valide/rejette les demandes de boutique, valide/rejette les produits soumis, supervise la plateforme (utilisateurs, commandes, litiges). |

Un même compte utilisateur peut cumuler les rôles **acheteur** et **vendeur**. Le rôle vendeur est accordé après validation d'une demande de boutique.

## 4. Processus métier clés

### 4.1 Création de boutique
1. Un visiteur crée un compte user (identifiant/login + mot de passe, email optionnel ; ou OAuth Google).
2. Le user soumet une **demande de création de boutique** via un formulaire qui s'adapte selon le type de détenteur (documents conformes à la réglementation ivoirienne, cf. détail dans [02-specifications-fonctionnelles.md](./02-specifications-fonctionnelles.md)) :
   - **Personne physique** (producteur individuel non enregistré formellement) : nom, prénom, pièce d'identité (CNI, attestation d'identité ou passeport).
   - **Entreprenant** (statut OHADA simplifié, courant chez les petits producteurs ivoiriens) : pièce d'identité + récépissé d'entreprenant délivré par le CEPICI.
   - **Personne morale** (société formelle) : raison sociale, numéro RCCM, NCC (Numéro de Compte Contribuable, issu de la Déclaration Fiscale d'Existence), document statutaire, représentant légal.
   - Champs communs : nom de la boutique, description, adresse, contact, logo/photo de couverture.
3. L'admin examine la demande (vérifie les pièces justificatives) et **valide ou rejette** (avec motif) le profil vendeur.
4. Si validée, le compte obtient le rôle vendeur et une boutique est créée en statut actif.

### 4.2 Publication d'un produit
1. Le vendeur crée une fiche produit (nom, description, prix, quantité, catégorie, photos, mode(s) de livraison disponibles).
2. Le produit est soumis en statut **en attente de validation**.
3. L'admin valide ou rejette (avec motif) le produit.
4. Un produit validé devient visible dans le catalogue public ; un produit rejeté retourne au vendeur pour correction.

### 4.3 Livraison
Chaque produit définit, au moment de sa publication, les modes de livraison qu'il propose (configurable par le vendeur, au moins un requis) :
- **Retrait chez le vendeur** : le vendeur définit le lieu de retrait.
- **Livraison à domicile** : le vendeur définit un prix de livraison ; l'acheteur renseigne l'adresse de livraison lors de la commande.

### 4.4 Achat et paiement
1. L'acheteur ajoute un ou plusieurs produits au panier, ou achète directement un produit.
2. Pour chaque ligne/produit, l'acheteur choisit le mode de livraison (retrait ou domicile) parmi les options proposées par le produit, et renseigne l'adresse si besoin.
3. Récapitulatif de commande (sous-total, frais de livraison, total).
4. Paiement par Mobile Money via **Fedapay**.
5. Confirmation de commande et notification au(x) vendeur(s) concerné(s).
6. La plateforme **prélève automatiquement** une commission sur chaque vente dès la confirmation du paiement ; le solde net est reversé au vendeur de façon automatisée (pas d'intervention manuelle de l'admin), sur le compte Mobile Money qu'il a enregistré pour sa boutique.

## 5. Besoins fonctionnels (synthèse)

- Authentification : inscription/connexion par identifiant (login) + mot de passe — email optionnel à l'inscription — et OAuth (Google), gestion de session via JWT, gestion des rôles (USER, VENDEUR, ADMIN).
- Gestion de profil utilisateur.
- Demande, instruction et validation/rejet des boutiques (workflow avec statuts et motifs).
- Gestion de boutique par le vendeur (infos, horaires, logo, **localisation sur carte**, etc.).
- Gestion des produits par le vendeur (CRUD, photos, stock, prix, modes de livraison).
- Validation/rejet des produits par l'admin (workflow avec statuts et motifs).
- Catalogue public (recherche, filtres par catégorie/boutique/prix, fiche produit, fiche boutique).
- Panier multi-produits / achat direct.
- Tunnel de commande avec choix du mode de livraison par produit.
- Intégration paiement Fedapay (Mobile Money), gestion des webhooks de confirmation.
- Gestion des commandes (suivi statut : en attente paiement, payée, en préparation, expédiée/prête, livrée, annulée).
- Calcul et suivi de la commission plateforme par commande.
- Avis et notation des vendeurs et des produits par les acheteurs (uniquement après achat livré), avec modération admin.
- Formulaire de contact vers le support (visiteur ou user), avec back-office admin pour traiter les messages.
- Back-office admin : gestion utilisateurs, boutiques, produits, commandes, litiges, statistiques, messages de support.
- **Notifications in-app obligatoires** (centre de notifications dans l'application, indépendant de l'email) aux étapes clés (validation boutique/produit, nouvelle commande, paiement confirmé, réponse support) — l'email reste envoyé en complément uniquement si l'utilisateur en a renseigné un. Le in-app est le canal garanti pour tous, car une grande partie des utilisateurs n'a pas d'adresse email.
- **Géolocalisation de la boutique** : chaque boutique doit indiquer sa position sur une carte (en plus de l'adresse texte) lors de la demande de création, afin que les acheteurs puissent s'y rendre physiquement (retrait en boutique).

## 6. Besoins non fonctionnels

- **Sécurité** : chiffrement des mots de passe, JWT signé, protection CSRF/CORS, validation des entrées, stockage sécurisé des documents d'identité/justificatifs.
- **Conformité paiement** : aucune donnée de carte/mobile money stockée en local — tout transite via Fedapay.
- **Performance** : catalogue consultable rapidement même avec un grand nombre de produits (pagination, index DB).
- **Disponibilité** : architecture déployable simplement (monolithe modulaire), logs exploitables.
- **Internationalisation (i18n)** : application disponible en **français** (langue par défaut) et **anglais** dès le MVP ; architecture de traduction prévue pour accueillir d'autres langues plus tard sans refonte.
- **Responsive** : le front Angular doit être utilisable sur mobile (Mobile Money implique usage mobile fréquent).
- **Ergonomie des formulaires longs** : tout formulaire à plusieurs sections (ex. demande de boutique) est découpé en **étapes successives** (wizard/stepper) plutôt que présenté en un seul long formulaire, avec sauvegarde de la progression entre les étapes.
- **Traçabilité** : historique des validations/rejets (boutique, produit) avec horodatage et motif.
- **Évolutivité** : architecture en packages par domaine permettant d'extraire des modules en services séparés plus tard si besoin.

## 7. Périmètre MVP vs évolutions futures

**MVP (voir [07-plan-execution.md](./07-plan-execution.md))** : inscription/auth, demande boutique, gestion produits avec validation admin, catalogue, panier, commande, paiement Fedapay, commission simple, back-office admin basique, **avis et notation des vendeurs/produits**, **formulaire de contact vers le support**.

**Hors MVP / pistes futures** :
- Messagerie directe acheteur-vendeur en temps réel (chat) — au MVP, le seul canal de contact est le formulaire de contact vers le support, pas de messagerie directe entre acheteur et vendeur.
- Gestion fine des stocks multi-entrepôts.
- Livraison via transporteurs tiers / tracking.
- Application mobile native.
- Multi-devises / multi-pays.

## 8. Points ouverts

- Liste exacte et finale des champs du formulaire personne physique / personne morale (structure posée dans [02-specifications-fonctionnelles.md](./02-specifications-fonctionnelles.md), à affiner avec l'utilisateur).
- Taux de commission exact : **recommandation 5 à 8%** (taux usuel des marketplaces agricoles — comparable Twiga Foods, Complete Farmer — nettement plus bas que les marketplaces généralistes 8-15% ou le food delivery 15-30%, afin de rester attractif pour de petits producteurs à faible marge). Configurable par catégorie dès le MVP via `commission_settings`. **Taux final à valider par l'utilisateur.**
- Règle de reversement : **prélèvement et reversement automatiques** dès confirmation du paiement (pas de virement manuel par l'admin), cf. détail technique dans [04-architecture-technique.md](./04-architecture-technique.md) — sous réserve de confirmer les capacités réelles de paiement scindé (split payment) ou de transfert programmatique de l'API Fedapay (point à vérifier techniquement).
- ~~Fournisseur d'email transactionnel à choisir~~ → **tranché pour le démarrage** : envoi par **SMTP standard** (`spring-boot-starter-mail` / `JavaMailSender`) via **Gmail SMTP** sur `losdiakite@gmail.com`, utilisé en dev et en prod pour le moment (à migrer vers un fournisseur SMTP dédié quand le volume augmente, cf. [04-architecture-technique.md](./04-architecture-technique.md)).
- ~~Mécanisme de récupération de compte pour les users sans email renseigné~~ → **tranché** : récupération par **selfie de vérification** comparé à une photo de référence enregistrée, avec validation par le support. Détail dans [02-specifications-fonctionnelles.md (UC-01b)](./02-specifications-fonctionnelles.md).
