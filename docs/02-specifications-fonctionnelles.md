# 02 — Spécifications fonctionnelles

Statut : v1.0 — 2026-10-01

Voir le contexte global dans [01-analyse-besoins.md](./01-analyse-besoins.md).

## 1. Rôles et permissions

| Fonctionnalité | Visiteur | User | Vendeur | Admin |
|---|:---:|:---:|:---:|:---:|
| Parcourir catalogue / boutiques | ✅ | ✅ | ✅ | ✅ |
| Créer un compte / se connecter | ✅ | - | - | - |
| Gérer son profil | - | ✅ | ✅ | ✅ |
| Soumettre une demande de boutique | - | ✅ | - | - |
| Gérer sa boutique et ses produits | - | - | ✅ | - |
| Acheter (panier, commande, paiement) | - | ✅ | ✅ | - |
| Valider/rejeter une demande de boutique | - | - | - | ✅ |
| Valider/rejeter un produit | - | - | - | ✅ |
| Gérer utilisateurs / rôles | - | - | - | ✅ |
| Voir statistiques / commissions | - | - | ✅ (ses ventes) | ✅ (global) |

Rôles techniques : `ROLE_USER`, `ROLE_VENDEUR`, `ROLE_ADMIN`. Un compte peut avoir `ROLE_USER` + `ROLE_VENDEUR` simultanément.

## 2. Use cases détaillés

### UC-01 — Inscription / Connexion
- **Acteur** : Visiteur
- **Flux** :
  1. Inscription locale par **identifiant (login) + mot de passe**. L'email est **optionnel** à l'inscription (champ facultatif, peut être ajouté/modifié plus tard dans le profil). Possibilité alternative : inscription **via Google OAuth** (l'email provient alors automatiquement du compte Google).
  2. À l'inscription locale, le user est invité (fortement recommandé mais non bloquant) à prendre un **selfie de sécurité** — une photo de son visage, sans pièce d'identité associée à ce stade — qui servira uniquement de référence pour la récupération de compte en l'absence d'email (cf. UC-01b). Il peut aussi l'ajouter plus tard depuis son profil.
  3. Avant de pouvoir valider l'inscription (locale **ou** via Google, première connexion), le user doit donner son **consentement explicite** via des cases à cocher **obligatoires et distinctes** (non pré-cochées) :
     - "J'accepte les **Conditions Générales d'Utilisation (CGU)**"
     - "J'accepte les **Conditions Générales de Vente (CGV)**" (applicables dès qu'il effectue un achat sur la plateforme)
     - "J'ai pris connaissance de la **Politique de confidentialité**" (couvre la protection des données et la politique de cookies, cf. [08-mentions-legales-cgu-rgpd.md](./08-mentions-legales-cgu-rgpd.md))
     Chaque lien ouvre le document correspondant ; l'inscription est bloquée tant que les trois cases ne sont pas cochées.
  4. En parallèle, dès la première visite du site (avant même l'inscription), un **bandeau de consentement cookies** s'affiche pour les cookies non strictement nécessaires (cf. [08-mentions-legales-cgu-rgpd.md §8](./08-mentions-legales-cgu-rgpd.md)) — distinct du consentement CGU/CGV/confidentialité, car il concerne tout visiteur, inscrit ou non.
  5. Connexion → soit **login + mot de passe**, soit **"Se connecter avec Google"**. Émission d'un access token JWT (courte durée) + refresh token (longue durée, httpOnly cookie ou storage sécurisé).
- **Règles** :
  - Login : unique en base, format restreint (ex. alphanumérique + `.`/`_`/`-`, 3 à 30 caractères, sans espace), obligatoire.
  - Mot de passe : min. 8 caractères, au moins 1 chiffre et 1 lettre. Obligatoire pour un compte local (optionnel/absent pour un compte créé uniquement via Google).
  - Email : optionnel à l'inscription locale ; s'il est renseigné (à l'inscription ou plus tard), il doit être unique en base et sert aux notifications et à la récupération de mot de passe par email.
  - Selfie de sécurité : optionnel mais fortement recommandé, notamment pour les users sans email (contexte où beaucoup d'utilisateurs n'ont pas d'adresse email) — c'est le seul autre moyen de prouver son identité pour récupérer l'accès à son compte.
  - L'acceptation CGU/CGV/confidentialité est **horodatée et versionnée** en base (`users.cgu_accepted_at`/`cgu_version`, `users.cgv_accepted_at`/`cgv_version`, `users.privacy_accepted_at`/`privacy_version`) pour preuve en cas de litige, cf. [03-modele-donnees.md](./03-modele-donnees.md).
  - Pour un compte créé via Google (première connexion), les mêmes cases de consentement sont présentées avant finalisation du compte, même si l'identifiant/mot de passe ne sont pas demandés.
  - Si une nouvelle version d'un document légal est publiée, le user doit réaccepter à sa prochaine connexion si la mise à jour est substantielle (cf. [08-mentions-legales-cgu-rgpd.md §9](./08-mentions-legales-cgu-rgpd.md)).
  - Si connexion via Google et email Google déjà associé à un compte local existant → lier les comptes (même email) plutôt que d'en créer un nouveau.

### UC-01b — Récupération de compte (mot de passe oublié)
- **Acteur** : User
- **Flux** :
  1. **Cas avec email renseigné** : procédure classique — saisie de l'email, envoi d'un lien de réinitialisation à durée limitée, choix d'un nouveau mot de passe.
  2. **Cas sans email renseigné** (fréquent) : le user saisit son login, puis prend un **nouveau selfie en direct** (webcam/caméra, pas d'upload de fichier existant, pour limiter la fraude). La demande est envoyée au support (réutilise le canal `contact_messages` / back-office support, cf. UC-12) avec le selfie fraîchement pris et la référence du compte.
  3. Un admin compare visuellement le selfie soumis au **selfie de sécurité** enregistré sur le profil (ou, si le user est aussi vendeur, à son selfie KYC de boutique) pour vérifier qu'il s'agit bien de la même personne.
  4. Si la vérification est concluante, l'admin approuve la demande : un mot de passe temporaire est généré et communiqué au user par le canal disponible le plus fiable (téléphone si renseigné, sinon remise en main propre / via un agent, à définir selon le contexte opérationnel). Le user doit le changer à sa première connexion.
  5. Si la vérification échoue ou le selfie est jugé non concluant, la demande est rejetée avec motif ; le user peut refaire une tentative.
- **Règles** :
  - Un user sans email **et** sans selfie de sécurité enregistré ne peut pas s'auto-récupérer : il doit contacter le support qui traite au cas par cas (vérification manuelle renforcée, ex. questions de contrôle, autres justificatifs).
  - Le rapprochement visuel est **manuel par un admin** au MVP ; une vérification automatisée (service de reconnaissance faciale / liveness detection) est une évolution future possible, mais pas retenue au MVP pour limiter la complexité et les coûts.
  - Toute demande de récupération est historisée (qui, quand, décision, admin ayant traité).
  - Le mot de passe temporaire force un changement de mot de passe à la connexion suivante.

### UC-02 — Soumettre une demande de boutique
- **Acteur** : User
- **Pré-condition** : être connecté, ne pas avoir déjà une boutique en attente ou validée.
- **Flux** :
  0. Le formulaire est présenté en **plusieurs étapes successives** (wizard), pas en un seul long formulaire : Étape 1 "Type de détenteur", Étape 2 "Informations boutique" (champs communs), Étape 3 "Justificatifs" (champs/documents spécifiques au type choisi), Étape 4 "Récapitulatif et soumission". L'utilisateur peut revenir en arrière entre les étapes ; la progression peut être sauvegardée en brouillon (évolution possible si jugé utile).
  1. Étape 1 : le user choisit le type de détenteur parmi **trois options**, alignées sur les statuts juridiques en vigueur en Côte d'Ivoire (droit OHADA) : **Personne physique**, **Entreprenant**, **Personne morale**. Les étapes suivantes s'adaptent dynamiquement à ce choix.
  2. Étape 2 — champs communs à toutes les boutiques :
     - Nom de la boutique (unique)
     - Description
     - Catégorie principale (ex : maraîchage, élevage, produits transformés...)
     - Adresse (ville, commune, quartier/adresse précise)
     - **Localisation sur carte** (sélection d'un point géographique — latitude/longitude — sur une carte interactive, en plus de l'adresse texte ; obligatoire, pour permettre aux acheteurs de s'y rendre en cas de retrait en boutique)
     - Téléphone de contact
     - Logo (image)
     - Photo de couverture (optionnel)
  3. Étape 3 (variante selon le type choisi) — champs spécifiques **Personne physique** (producteur individuel sans statut formel) :
     - Nom, prénom
     - Date de naissance
     - Type de pièce d'identité (**CNI**, **Attestation d'identité** — valable pendant la délivrance de la CNI biométrique —, ou **Passeport**)
     - Numéro de pièce
     - Photo recto/verso de la pièce (upload)
  4. Étape 3 (variante) — champs spécifiques **Entreprenant** (statut simplifié de l'Acte uniforme OHADA, courant pour les petits producteurs ivoiriens qui veulent une activité déclarée sans créer de société) :
     - Nom, prénom
     - Date de naissance
     - Type et numéro de pièce d'identité (CNI, Attestation d'identité ou Passeport) + photo recto/verso (mêmes règles que Personne physique)
     - Numéro de récépissé d'entreprenant (délivré par le CEPICI, guichet unique de création d'entreprise)
     - Date de délivrance du récépissé
     - Upload du récépissé d'entreprenant
  5. Étape 3 (variante) — champs spécifiques **Personne morale** (société formelle : SARL, SA, SAS...) :
     - Raison sociale
     - Forme juridique (SARL, SA, SAS, SUARL...)
     - Numéro RCCM (format ivoirien, ex. `CI-ABJ-2024-A-1234`, délivré via le CEPICI)
     - NCC — Numéro de Compte Contribuable (délivré par la DGI à l'issue de la Déclaration Fiscale d'Existence - DFE)
     - Document RCCM / statutaire (upload)
     - Nom et fonction du représentant légal
     - Type et numéro de pièce d'identité du représentant légal (CNI, Attestation d'identité ou Passeport)
     - Photo recto/verso de la pièce d'identité du représentant légal (upload)
     - Selfie du représentant légal tenant sa pièce d'identité (upload) — même exigence KYC que pour une personne physique
  6. Étape 4 : récapitulatif de toutes les informations saisies, puis soumission → statut boutique = `EN_ATTENTE`.
  7. Notification à l'admin (nouvelle demande à traiter).
- **Règles** :
  - Tous les champs obligatoires du type choisi doivent être renseignés avant soumission.
  - Les documents uploadés sont stockés de façon sécurisée (accès restreint à l'admin et au propriétaire).
  - Un user ne peut avoir qu'une seule boutique.
  - La localisation sur carte (latitude/longitude) est **obligatoire** ; par défaut le sélecteur de carte se centre sur la ville saisie, le user ajuste le point précis.
  - La vérification de l'authenticité des numéros RCCM/NCC/récépissé d'entreprenant reste manuelle (visuelle, par l'admin) au MVP ; une vérification automatisée via un service tiers (ex. API CEPICI/DGI si disponible) est une évolution future.

### UC-03 — Valider / rejeter une demande de boutique
- **Acteur** : Admin
- **Flux** :
  1. L'admin consulte la liste des demandes `EN_ATTENTE`, ouvre le détail (infos + documents).
  2. Décision : **Valider** → statut `VALIDEE`, le user reçoit `ROLE_VENDEUR`, la boutique devient `ACTIVE`.
     **Rejeter** → statut `REJETEE` avec motif obligatoire ; le user peut corriger et resoumettre.
  3. Notification (email) au user du résultat.
- **Règles** :
  - Le motif de rejet est obligatoire et visible par le vendeur.
  - Historique des décisions conservé (qui, quand, motif).

### UC-04 — Publier / gérer un produit
- **Acteur** : Vendeur
- **Pré-condition** : boutique `ACTIVE`.
- **Flux** :
  1. Le vendeur crée une fiche produit via un formulaire en **étapes** : Étape 1 "Informations générales" (nom, description, catégorie, prix unitaire, unité (kg, pièce, sac, litre...), quantité disponible), Étape 2 "Photos" (upload multiple), Étape 3 "Livraison" (mode(s) proposés), Étape 4 "Récapitulatif et soumission".
     - Mode(s) de livraison proposés (au moins un) :
       - **Retrait boutique** : lieu de retrait (hérité de l'adresse boutique ou spécifique)
       - **Livraison à domicile** : prix de livraison fixé par le vendeur
  2. Soumission → statut produit = `EN_ATTENTE`.
  3. Le vendeur peut éditer un produit tant qu'il n'est pas validé (repasse en `EN_ATTENTE`) ou le dépublier/republier après validation.
- **Règles** :
  - Prix et quantité > 0.
  - Au moins un mode de livraison actif.
  - Si livraison à domicile activée, le prix de livraison est obligatoire.

### UC-05 — Valider / rejeter un produit
- **Acteur** : Admin
- **Flux** : identique à UC-03, appliqué au produit. Statut produit : `EN_ATTENTE` → `VALIDE` (visible catalogue) ou `REJETE` (motif, invisible, éditable par le vendeur).

### UC-06 — Parcourir le catalogue
- **Acteur** : Visiteur / User
- **Flux** : liste des produits `VALIDE` et boutiques `ACTIVE`, recherche texte, filtres (catégorie, prix, boutique, mode de livraison), fiche produit détaillée, fiche boutique (avec ses produits et une **carte interactive affichant sa localisation**, avec un lien "itinéraire" ouvrant une application de navigation externe sur mobile).

### UC-07 — Panier et achat
- **Acteur** : User (connecté — le panier nécessite un compte pour le MVP)
- **Flux** :
  1. Ajout au panier depuis une fiche produit (quantité), ou "Acheter maintenant" (saute directement au récapitulatif avec ce produit seul).
  2. Le panier peut contenir des produits de plusieurs boutiques différentes.
  3. Pour chaque produit du panier, l'acheteur choisit son mode de livraison (parmi ceux proposés par le produit) et saisit l'adresse si "livraison à domicile".
  4. Récapitulatif : sous-total produits + frais de livraison (somme des frais par produit/boutique) = total.
  5. Passage au paiement.
- **Règles** :
  - La quantité demandée ne peut pas dépasser le stock disponible.
  - Le panier peut générer **plusieurs commandes**, une par boutique (chaque vendeur gère sa propre commande), rattachées à une même transaction de paiement.

### UC-08 — Paiement Mobile Money via Fedapay
- **Acteur** : User
- **Flux** :
  1. À la confirmation du récapitulatif, le backend crée une transaction Fedapay (montant total) et redirige/affiche le widget Fedapay.
  2. L'acheteur saisit son numéro Mobile Money et valide sur son téléphone.
  3. Fedapay notifie le backend via **webhook** (succès/échec).
  4. Sur succès : commandes passent en statut `PAYEE`, décrément du stock, notification aux vendeurs, commission plateforme calculée et enregistrée.
  5. Sur échec : commandes restent `EN_ATTENTE_PAIEMENT`, l'utilisateur peut réessayer.
- **Règles** :
  - Aucune donnée bancaire/mobile money stockée côté plateforme.
  - Idempotence du traitement webhook (une transaction Fedapay ne doit être traitée qu'une fois).

### UC-09 — Suivi de commande
- **Acteur** : User (côté acheteur), Vendeur (côté vendeur)
- **Statuts commande** : `EN_ATTENTE_PAIEMENT` → `PAYEE` → `EN_PREPARATION` → `PRETE` (retrait) / `EXPEDIEE` (livraison) → `LIVREE`. Branches : `ANNULEE`.
- Le vendeur met à jour le statut de sa commande (préparation, prête/expédiée, livrée). L'acheteur suit l'avancement.

### UC-10 — Commission plateforme et reversement automatique au vendeur
- **Pré-condition** : le vendeur a enregistré un **compte Mobile Money de reversement** sur sa boutique (opérateur + numéro) ; requis pour pouvoir publier des produits (vérification à la validation de la boutique ou avant le premier reversement).
- **Flux** :
  1. À la confirmation du paiement (webhook Fedapay `APPROVED`), pour chaque commande : `montant_commission = sous_total_produits * taux_commission` (taux configurable, par défaut ou par catégorie, cf. [03-modele-donnees.md](./03-modele-donnees.md)) ; `montant_net_vendeur = sous_total_produits - montant_commission + frais_livraison` (les frais de livraison reviennent intégralement au vendeur, la commission ne porte que sur le prix des produits).
  2. Le **prélèvement de la commission et le reversement du solde net au vendeur sont automatiques**, sans action manuelle de l'admin, selon l'une de ces deux implémentations (à trancher techniquement, cf. [04-architecture-technique.md](./04-architecture-technique.md)) :
     - **Paiement scindé (split payment)** si l'API Fedapay le permet : la commission et le solde vendeur sont routés séparément dès la transaction initiale.
     - **Reversement programmatique différé** sinon : le solde net est calculé immédiatement, puis transféré automatiquement au vendeur via un job planifié (ex. quotidien) qui appelle l'API de transfert/paiement sortant de Fedapay vers le compte Mobile Money du vendeur.
  3. Chaque reversement est historisé (`vendor_payouts`) avec son statut (`EN_ATTENTE`, `ENVOYE`, `ECHEC`).
  4. En cas d'échec de reversement (ex. numéro Mobile Money invalide), le montant reste `EN_ATTENTE`, le vendeur et l'admin sont notifiés, une nouvelle tentative ou une correction du compte de reversement est possible.
- **Règles** :
  - Le taux de commission appliqué à une commande est figé au moment du paiement (snapshot sur la commande), même si le taux de configuration change ensuite.
  - Le vendeur peut consulter l'historique de ses reversements et commissions prélevées depuis son espace.
  - L'admin garde une vue globale des reversements (back-office) et peut déclencher un nouvel essai en cas d'échec.

### UC-11 — Avis et notation des vendeurs/produits
- **Acteur** : User (acheteur)
- **Pré-condition** : avoir au moins une commande au statut `LIVREE` contenant le produit (pour un avis produit) ou issue de la boutique (pour un avis boutique/vendeur) concerné — **avis vérifié achat uniquement**.
- **Flux** :
  1. Depuis sa page "Mes commandes" (commandes livrées) ou depuis la fiche produit/boutique déjà achetée, l'acheteur laisse une note (1 à 5 étoiles) et un commentaire optionnel, pour le **produit** et/ou pour la **boutique**.
  2. L'avis est publié immédiatement (visible sur la fiche produit/boutique) ; l'admin peut le modérer a posteriori.
  3. La note moyenne et le nombre d'avis sont affichés sur la fiche produit et sur la fiche boutique.
- **Règles** :
  - Un seul avis par (acheteur, produit) et un seul avis par (acheteur, boutique) — modifiable/éditable tant que l'acheteur le souhaite (écrase le précédent, pas d'accumulation).
  - Note obligatoire (1 à 5), commentaire optionnel (texte libre, longueur limitée).
  - L'admin peut masquer un avis abusif/injurieux (statut `MASQUE`), motif facultatif, sans le supprimer définitivement (traçabilité).
  - Le vendeur ne peut pas noter ses propres produits/sa propre boutique (vérification : l'acheteur de la commande ne peut pas être le propriétaire de la boutique concernée).

### UC-12 — Formulaire de contact / support
- **Acteur** : Visiteur ou User
- **Flux** :
  1. Un visiteur (non connecté) ou un user (connecté, formulaire pré-rempli avec ses coordonnées) accède à une page "Contact" / "Support" et soumet : nom complet, email (obligatoire pour pouvoir être recontacté), sujet, message.
  2. Le message est enregistré en statut `NOUVEAU` et une notification est envoyée à l'admin.
  3. L'admin consulte la liste des messages dans le back-office, peut changer leur statut (`NOUVEAU` → `EN_COURS` → `TRAITE`) et répondre (réponse enregistrée et envoyée par email à l'expéditeur si email valide).
- **Règles** :
  - Email obligatoire sur ce formulaire précis (c'est le seul moyen de recontacter l'expéditeur), même si l'email du compte user est par ailleurs optionnel ([02](./02-specifications-fonctionnelles.md#uc-01--inscription--connexion)).
  - Si l'expéditeur est un user connecté, le message est rattaché à son compte (`user_id`) en plus des coordonnées saisies.
  - Pas de messagerie directe acheteur-vendeur au MVP : ce formulaire est le seul canal, à destination du support (admin), pas d'un vendeur en particulier.

### UC-13 — Notifications in-app (canal obligatoire)
- **Acteur** : User, Vendeur, Admin
- **Contexte** : une grande partie des utilisateurs n'a pas d'email (cf. [01-analyse-besoins.md](./01-analyse-besoins.md)). Le centre de notifications **in-app est donc le canal garanti et obligatoire** pour tout événement important ; l'email n'est qu'un canal complémentaire, envoyé en plus si l'utilisateur en a renseigné un.
- **Flux** :
  1. Chaque événement clé génère systématiquement une notification in-app pour le(s) utilisateur(s) concerné(s), **indépendamment du fait qu'ils aient un email ou non** : soumission/validation/rejet de boutique, soumission/validation/rejet de produit, nouvelle commande reçue (vendeur), changement de statut de commande (acheteur), paiement confirmé, nouvel avis reçu (vendeur), réponse à un message de support, reversement envoyé/échoué (vendeur).
  2. L'utilisateur consulte ses notifications via une icône "cloche" dans la topbar (badge avec le nombre de non-lues), liste déroulante ou page dédiée, marquage lu/non lu.
  3. Si un email est renseigné, le même événement déclenche en plus un envoi email (SMTP, cf. [04-architecture-technique.md](./04-architecture-technique.md)) — c'est un bonus, pas une dépendance.
- **Règles** :
  - La création de la notification in-app ne doit jamais échouer silencieusement ni dépendre de la présence d'un email — elle est toujours écrite en base, consultée au prochain accès de l'utilisateur (web, pas nécessairement en temps réel au MVP).
  - Rétention raisonnable des notifications (ex. purge/archivage après plusieurs mois), à affiner.
  - Évolution possible (hors MVP) : canal SMS pour les événements critiques, pour toucher les users sans accès internet permanent — nécessite un fournisseur de passerelle SMS en Côte d'Ivoire, non retenu au MVP (coût/complexité), cf. point ouvert.

### UC-14 — Back-office Admin
- Gestion des demandes de boutique (UC-03), des produits (UC-05), des utilisateurs (activation/désactivation, changement de rôle), des commandes (vue globale, litiges), des avis (modération, UC-11), des messages de support (UC-12), des notifications système, consultation des statistiques (ventes, commissions, nombre de boutiques actives).

## 3. Statuts (enums) récapitulatifs

```
StatutBoutique   : EN_ATTENTE | VALIDEE | REJETEE | SUSPENDUE
StatutProduit    : EN_ATTENTE | VALIDE | REJETE | DEPUBLIE
StatutCommande   : EN_ATTENTE_PAIEMENT | PAYEE | EN_PREPARATION | PRETE | EXPEDIEE | LIVREE | ANNULEE
TypeDetenteur    : PHYSIQUE | ENTREPRENANT | MORALE
ModeLivraison    : RETRAIT_BOUTIQUE | LIVRAISON_DOMICILE
StatutAvis       : VISIBLE | MASQUE
StatutMessageContact : NOUVEAU | EN_COURS | TRAITE
StatutRecuperationCompte : NOUVELLE | EN_COURS | APPROUVEE | REJETEE
```

## 4. Règles de gestion transverses

- RG-01 : Un produit n'est visible dans le catalogue public que si `produit.statut = VALIDE` ET `boutique.statut = ACTIVE`.
- RG-02 : Un vendeur ne peut pas valider ses propres produits (seul l'admin valide).
- RG-03 : La suspension d'une boutique par l'admin rend tous ses produits invisibles sans les repasser en `REJETE`.
- RG-04 : Toute action de validation/rejet (boutique, produit) est historisée avec auteur, date, motif.
- RG-05 : Le stock est décrémenté uniquement après confirmation du paiement (pas à l'ajout au panier), pour éviter les réservations fantômes au MVP. Option future : réservation temporaire du stock pendant le paiement.
- RG-06 : Un panier peut éclater en plusieurs commandes (une par boutique) lors du paiement.

## 5. Points ouverts

- Faut-il autoriser l'achat invité (sans compte) ? → Non retenu pour le MVP (le panier nécessite un compte).
- Gestion des retours/remboursements : à spécifier dans une itération ultérieure.
