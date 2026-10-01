# 08 — Mentions légales, CGU/CGV, Politique de confidentialité

Statut : v1.0 — 2026-10-01

> ⚠️ **Avertissement important** : ce document est une **base de travail structurée et professionnelle**, rédigée pour couvrir les sujets juridiques standards d'une marketplace e-commerce opérant en Côte d'Ivoire. Ce n'est **pas un avis juridique**. Avant toute mise en production, il doit être **relu et validé par un avocat ou juriste ivoirien spécialisé en droit du numérique**, notamment pour confirmer la conformité au cadre légal en vigueur et les éventuelles obligations déclaratives. Tous les champs entre `[...]` sont des placeholders à compléter une fois la société éditrice constituée.

## 1. Cadre juridique applicable

Contrairement à une marketplace opérant dans l'Union européenne, **Le Paysan n'est pas directement soumis au RGPD** (règlement européen). Le cadre pertinent pour une plateforme opérant **en Côte d'Ivoire** est principalement :

| Texte | Objet |
|---|---|
| **Loi n° 2013-450 du 19 juin 2013** relative à la protection des données à caractère personnel | Équivalent ivoirien du RGPD : droits des personnes, obligations du responsable de traitement, déclaration/autorisation auprès de l'autorité de contrôle. |
| **ARTCI** (Autorité de Régulation des Télécommunications/TIC de Côte d'Ivoire) | Autorité de contrôle compétente pour la protection des données personnelles ; interlocuteur pour déclaration de traitement et réclamations des utilisateurs. |
| **Ordonnance n° 2023-699 du 13 septembre 2023 portant Code du numérique** (et ses décrets d'application) | Cadre consolidé ivoirien couvrant notamment commerce électronique, signature/contrats électroniques, cybersécurité et protection des données — *à vérifier avec un juriste pour les articles précis applicables, le cadre réglementaire ivoirien évoluant*. |
| **Loi n° 2013-546** relative aux transactions électroniques | Validité juridique des contrats conclus en ligne (acceptation des CGU/CGV, signature électronique). |
| **Loi n° 2016-412** relative à la consommation | Protection du consommateur : information précontractuelle, garanties, réclamations. |
| **Réglementation BCEAO** (Banque Centrale des États de l'Afrique de l'Ouest), notamment l'**Instruction n° 01/2015** relative à l'émission de monnaie électronique | Encadre les émetteurs de monnaie électronique et prestataires de services de paiement dans l'espace UEMOA. **Fedapay** opère comme prestataire de paiement agréé ; Le Paysan ne manipule ni ne stocke directement les fonds Mobile Money. |
| **Actes uniformes OHADA** (droit commercial général) | Déjà couvert pour le statut Entreprenant et les sociétés commerciales, cf. [02-specifications-fonctionnelles.md](./02-specifications-fonctionnelles.md). |

**Majorité légale** : en Côte d'Ivoire, la majorité civile est fixée à 18 ans. La création d'un compte (acheteur ou vendeur) est réservée aux personnes majeures capables de contracter.

### 1.1 Et le RGPD dans tout ça ?

Le RGPD (règlement UE 2016/679) ne s'applique **légalement** à Le Paysan que dans des cas précis :
- la plateforme vise explicitement des utilisateurs résidant dans l'UE (ex. diaspora ivoirienne en France achetant/vendant via la plateforme, communication marketing ciblée vers l'UE) ;
- ou la plateforme traite des données de personnes se trouvant physiquement dans l'UE au moment de la collecte ;
- ou la société éditrice s'implante un jour dans l'UE.

Au lancement (Côte d'Ivoire uniquement, utilisateurs ivoiriens), **ce n'est a priori pas le cas** : le texte qui s'applique réellement est la loi ivoirienne n° 2013-450 (§1 ci-dessus), contrôlée par l'ARTCI.

**Décision retenue pour ce projet** : appliquer **volontairement les standards RGPD** (droits renforcés, transparence, minimisation des données, sécurité, registre des traitements, etc.) **en plus** de la conformité à la loi ivoirienne, pour trois raisons :
1. Avoir un niveau de protection des données reconnu internationalement (utile pour la confiance des utilisateurs, des investisseurs, et des partenaires).
2. Faciliter une expansion future dans d'autres pays ou auprès d'utilisateurs de la diaspora.
3. La loi ivoirienne n° 2013-450 est elle-même structurée de façon très proche du RGPD (elle s'en est largement inspirée) : appliquer les deux en parallèle ne crée pas de contradiction, seulement des garanties légèrement plus strictes sur certains points (ex. durées de conservation, documentation du registre des traitements, étude d'impact pour les traitements sensibles comme le KYC).

En pratique, ce document applique donc une politique de confidentialité **« RGPD-aligned »**, avec la loi ivoirienne n° 2013-450 comme base légale de référence et l'ARTCI comme autorité de contrôle effective. Les sections suivantes utilisent le vocabulaire RGPD (base légale, responsable de traitement, sous-traitant, DPO...) tout en restant rattachées au droit ivoirien applicable.

## 2. Documents juridiques à produire

La plateforme doit publier, en français (langue officielle) et en anglais (cf. i18n, [04-architecture-technique.md](./04-architecture-technique.md)), les documents suivants, accessibles en pied de page et au moment de l'inscription/de la demande de boutique :

1. **Mentions légales** — identité de l'éditeur, de l'hébergeur.
2. **CGU** (Conditions Générales d'Utilisation) — règles d'usage de la plateforme pour tous les comptes.
3. **CGV** (Conditions Générales de Vente) — relation acheteur ↔ vendeur ↔ plateforme pour les transactions.
4. **Conditions vendeur** (charte spécifique boutique) — obligations KYC, exactitude des informations, conformité des produits, commission et reversement.
5. **Politique de confidentialité / protection des données personnelles**.
6. **Politique de cookies**.

## 3. Mentions légales — structure

```
Éditeur du site :        [Raison sociale de la société éditrice — à constituer]
Forme juridique :        [SARL / SAS... une fois immatriculée]
RCCM :                   [Numéro RCCM de la société éditrice]
NCC :                    [Numéro de Compte Contribuable]
Siège social :            [Adresse complète, Côte d'Ivoire]
Directeur de publication : [Nom, qualité]
Contact :                 [email / formulaire de contact — cf. UC-12]
Hébergeur :                [Nom de l'hébergeur, adresse, contact — à définir, cf. point ouvert 04-architecture-technique.md]
```

> Tant que la société n'est pas juridiquement constituée, le porteur de projet (personne physique) doit a minima être identifié comme responsable de la publication, avec un contact valide. Un passage en société (SARL/SAS) est recommandé avant exploitation commerciale réelle, notamment pour la responsabilité et la fiscalité.

## 4. CGU — plan type et points clés

1. **Objet** : accès et utilisation de la plateforme Le Paysan (marketplace de mise en relation entre producteurs agricoles et acheteurs).
2. **Acceptation** : l'inscription (UC-01) vaut acceptation pleine et entière des CGU et de la Politique de confidentialité ; case à cocher obligatoire, horodatée et versionnée (cf. §9).
3. **Comptes et rôles** : conditions de création de compte, rôles (acheteur, vendeur, admin), responsabilité de l'utilisateur sur la confidentialité de ses identifiants.
4. **Rôle d'intermédiaire de la plateforme** : Le Paysan est un **intermédiaire technique** mettant en relation vendeurs et acheteurs ; les contrats de vente se forment **entre le vendeur et l'acheteur**, la plateforme n'étant pas elle-même vendeuse des produits (sauf mention contraire future). Cette qualification conditionne le régime de responsabilité.
5. **Comportements interdits** : fraude, fausses informations (notamment lors du KYC boutique), contenu illicite, atteinte aux droits de tiers.
6. **Modération et sanctions** : suspension/rejet de boutique ou de produit, suspension de compte, en cas de non-respect — cf. workflows admin déjà modélisés (UC-03, UC-05).
7. **Propriété intellectuelle** : marque, logo, contenu éditorial de la plateforme ; les vendeurs restent propriétaires des photos/descriptions de leurs produits mais concèdent à la plateforme un droit d'affichage/diffusion nécessaire au service.
8. **Responsabilité et garanties** : limitation de responsabilité de la plateforme sur la qualité/conformité des produits (responsabilité du vendeur), disponibilité du service (obligation de moyens, pas de résultat), disponibilité du service de paiement tiers (Fedapay).
9. **Résiliation** : conditions de suppression de compte, conservation des données résiduelles légalement requises (facturation, lutte anti-fraude).
10. **Droit applicable et juridiction** : droit ivoirien, tribunaux compétents de Côte d'Ivoire (ou clause de médiation préalable recommandée vu le contexte régional).

## 5. CGV — plan type et points clés

1. **Champ d'application** : toute commande passée sur la plateforme entre un acheteur et un vendeur (boutique validée).
2. **Formation de la commande** : récapitulatif, paiement Mobile Money via Fedapay, confirmation par email/notification in-app (cf. UC-07, UC-08).
3. **Prix** : prix affichés en FCFA (XOF), toutes taxes applicables mentionnées le cas échéant, frais de livraison distincts et transparents avant paiement.
4. **Paiement** : exclusivement via Fedapay (Mobile Money) ; la plateforme ne stocke aucune donnée de paiement, renvoi vers les CGU/politique de confidentialité de Fedapay pour le traitement du paiement lui-même.
5. **Livraison / retrait** : deux modalités selon le choix du vendeur et de l'acheteur — retrait en boutique (adresse + **localisation carte**, cf. UC-02) ou livraison à domicile (prix fixé par le vendeur) ; délais indicatifs à la charge du vendeur, pas garantis par la plateforme.
6. **Droit de rétractation — exclusion pour denrées périssables** : conformément aux pratiques standards du commerce de produits alimentaires frais/agricoles périssables, **le droit de rétractation ne s'applique pas** une fois la commande confirmée/en préparation, sauf non-conformité ou défaut du produit à la livraison. À confirmer/adapter selon le droit de la consommation ivoirien avec un juriste.
7. **Réclamations et litiges** : canal unique via le formulaire de contact/support (UC-12) dans un premier temps ; procédure de médiation par la plateforme avant toute action judiciaire ; en cas de litige persistant, compétence des juridictions ivoiriennes.
8. **Avis vérifiés** : les avis (UC-11) ne peuvent être laissés qu'après achat livré, la plateforme se réserve le droit de modérer les avis abusifs.

## 6. Conditions vendeur (charte boutique)

En complément des CGU/CGV générales, le vendeur accepte, lors de la soumission de sa demande de boutique (UC-02), des conditions spécifiques :

- **Exactitude des informations et documents KYC** fournis (identité, RCCM/NCC/récépissé d'entreprenant, selfie) — toute fausse déclaration entraîne le rejet ou la suspension de la boutique, sans préjudice d'éventuelles poursuites.
- **Conformité des produits** : exactitude des descriptions, respect des normes sanitaires applicables aux produits agricoles et alimentaires (responsabilité du vendeur).
- **Commission et reversement** : acceptation du taux de commission en vigueur (cf. [01-analyse-besoins.md](./01-analyse-besoins.md) et [02-specifications-fonctionnelles.md UC-10](./02-specifications-fonctionnelles.md)) et du mécanisme de reversement automatique sur le compte Mobile Money déclaré.
- **Obligation de disposer d'un compte Mobile Money de reversement valide** à son nom (ou celui de l'entité déclarée).
- **Droit de suspension** de la boutique par l'admin en cas de manquement, avec motif communiqué.

## 7. Politique de confidentialité / protection des données personnelles

### 7.1 Responsable de traitement et point de contact protection des données
`[Société éditrice — à compléter]` est responsable de traitement. Un point de contact dédié à la protection des données (rôle de type **DPO/référent protection des données**, même si sa désignation formelle n'est pas juridiquement obligatoire à ce stade compte tenu de la taille de la structure) est joignable via le formulaire de contact (UC-12) ou un email dédié `[privacy@... — à créer]`. Ce point de contact répond pour toute demande relevant à la fois de la loi ivoirienne n° 2013-450 et, par alignement volontaire, des standards RGPD (cf. §1.1).

### 7.2 Données collectées et finalités

| Catégorie de données | Exemples | Finalité | Base légale (sens large) |
|---|---|---|---|
| Données de compte | login, email (optionnel), téléphone, mot de passe (haché) | Gestion de compte, authentification | Exécution du contrat (CGU) |
| Selfie de sécurité | photo de référence | Récupération de compte sans email (UC-01b) | Consentement / intérêt légitime (sécurité du compte) |
| KYC vendeur | pièce d'identité, selfie avec pièce, RCCM/NCC/récépissé | Vérification d'identité, conformité légale, lutte anti-fraude | Obligation légale / intérêt légitime |
| Géolocalisation boutique | latitude/longitude | Affichage carte, permettre le retrait en boutique | Exécution du contrat |
| Données de commande | produits achetés, adresse de livraison | Exécution de la vente | Exécution du contrat |
| Avis et notes | commentaires, notation | Information des acheteurs | Intérêt légitime |
| Messages de support | nom, email, message | Traitement des demandes | Exécution du contrat / intérêt légitime |
| Données techniques | logs, cookies de session | Sécurité, bon fonctionnement | Intérêt légitime |

**Important** : les données de paiement Mobile Money (numéro, PIN, etc.) ne sont **jamais collectées ni stockées** par Le Paysan — elles sont traitées exclusivement par Fedapay.

### 7.3 Destinataires / sous-traitants
- **Fedapay** (traitement des paiements).
- **Hébergeur** du backend/base de données (à définir, cf. point ouvert technique).
- **Fournisseur SMTP** pour l'envoi d'emails (à définir).
- Aucune donnée n'est vendue à des tiers à des fins commerciales.
- Aucun transfert hors de la zone UEMOA/Côte d'Ivoire n'est prévu au MVP ; si un hébergement international était retenu, une clause de transfert de données devra être ajoutée.

### 7.4 Durées de conservation (indicatives, à valider juridiquement)
- Compte actif : durée de la relation contractuelle + délai de prescription applicable après clôture.
- Documents KYC (pièces d'identité, RCCM/NCC, selfies) : durée nécessaire à la relation + obligations légales/comptables (ordre de grandeur 5 à 10 ans, à confirmer avec un conseil juridique selon les obligations fiscales/comptables OHADA).
- Logs techniques : 6 à 12 mois.
- Messages de support traités : conservation raisonnable à des fins de preuve/qualité de service.

### 7.5 Droits des personnes concernées
Toute personne dispose, dans les conditions prévues par la loi ivoirienne n° 2013-450 et, par alignement volontaire, les standards RGPD, des droits suivants :
- **Droit d'accès** à ses données.
- **Droit de rectification** des données inexactes.
- **Droit d'effacement** ("droit à l'oubli"), sous réserve des obligations légales de conservation (ex. documents KYC).
- **Droit d'opposition** à certains traitements (ex. prospection).
- **Droit à la limitation** du traitement.
- **Droit à la portabilité** des données (export dans un format structuré, ex. JSON) — standard RGPD repris volontairement même si non expressément prévu par la loi ivoirienne.

Ces droits s'exercent via le formulaire de contact (UC-12) ou l'email dédié (§7.1), avec un délai de réponse cible d'un mois. Un droit de réclamation peut être introduit auprès de l'**ARTCI** (autorité ivoirienne compétente) ; pour les personnes relevant effectivement du RGPD (cf. §1.1), un droit de réclamation complémentaire existe auprès de l'autorité de protection des données de leur pays de résidence dans l'UE.

### 7.6 Sécurité
Renvoi vers les mesures techniques décrites dans [04-architecture-technique.md](./04-architecture-technique.md) : mots de passe hachés (BCrypt), JWT, accès restreint aux documents sensibles (pièces d'identité, KYC), HTTPS en production.

### 7.7 Registre des traitements et analyse d'impact (bonnes pratiques RGPD appliquées volontairement)
- **Registre des traitements** : document interne (non public) recensant chaque traitement de données (finalité, catégories de données, destinataires, durée de conservation, mesures de sécurité) — à tenir à jour en interne dès le lancement, même si non strictement exigé par la loi ivoirienne pour une structure de cette taille ; c'est une exigence RGPD reprise comme bonne pratique.
- **Analyse d'impact (PIA/DPIA)** recommandée pour le traitement **KYC vendeur** (pièces d'identité, selfies) : il s'agit du traitement le plus sensible de la plateforme (données d'identification + biométrie faible via selfie) et mérite une analyse de risques dédiée avant le lancement en production, même en l'absence d'obligation légale stricte en Côte d'Ivoire.

## 8. Politique de cookies

- **Cookies strictement nécessaires** : session/authentification (ex. refresh token en cookie httpOnly) — pas de consentement requis, mais information transparente.
- **Cookies de mesure d'audience / autres cookies non essentiels** (si ajoutés plus tard, ex. analytics) : nécessitent un **bandeau de consentement** avec choix accepter/refuser avant dépôt, conformément aux bonnes pratiques (même en l'absence d'obligation RGPD stricte, c'est une bonne pratique alignée sur le Code du numérique ivoirien et la confiance utilisateur).
- Au MVP, seuls des cookies/storage techniques (JWT) sont prévus → bandeau non bloquant requis, juste une information synthétique.

## 9. Traçabilité du consentement (implémentation)

Pour pouvoir prouver l'acceptation des documents légaux en cas de litige, **trois consentements distincts sont requis dès l'inscription** (UC-01, y compris pour un compte créé via Google à sa première connexion), par des cases à cocher séparées, non pré-cochées, chacune horodatée et versionnée en base :

| Case à cocher | Document lié | Colonnes (`users`) |
|---|---|---|
| "J'accepte les CGU" | Conditions Générales d'Utilisation (§4) | `cgu_accepted_at`, `cgu_version` |
| "J'accepte les CGV" | Conditions Générales de Vente — achat/vente sur la plateforme (§5) | `cgv_accepted_at`, `cgv_version` |
| "J'ai pris connaissance de la Politique de confidentialité" | Politique de confidentialité, incluant la politique de cookies (§7, §8) | `privacy_accepted_at`, `privacy_version` |

En complément, un **consentement spécifique vendeur** est requis à la soumission de la demande de boutique (UC-02) : case à cocher "J'accepte les Conditions vendeur" → `shops.cgv_vendeur_accepted_at`, `shops.cgv_vendeur_version` (charte spécifique boutique, §6, en plus des CGV générales déjà acceptées en tant qu'user).

Le **consentement cookies** (bandeau, cookies non essentiels uniquement) est géré séparément de ces trois cases : il s'affiche dès la première visite, avant même l'inscription, et est mémorisé côté navigateur (pas nécessairement en base au MVP, sauf si l'on souhaite une preuve plus robuste pour un user connecté).

Toute mise à jour substantielle d'un document légal doit incrémenter sa version et, idéalement, redemander l'acceptation à la prochaine connexion si le changement est significatif.

Détail du modèle de données dans [03-modele-donnees.md](./03-modele-donnees.md).

## 10. Emplacement dans le produit (UX)

- **Footer** (site public, toutes pages) : liens Mentions légales, CGU, CGV, Politique de confidentialité, Politique de cookies, Contact.
- **Inscription** (UC-01, y compris première connexion via Google) : **trois cases à cocher obligatoires et distinctes**, non pré-cochées, avec lien vers chaque document : CGU, CGV, Politique de confidentialité.
- **Demande de boutique** (UC-02, étape finale) : case à cocher obligatoire supplémentaire "Conditions vendeur".
- **Bandeau cookies** : affiché dès la première visite (avant même l'inscription), choix clair accepter/refuser, mémorisation du choix.

## 11. Langues

Les documents légaux doivent être disponibles en **français** (version de référence, le français étant la langue officielle en Côte d'Ivoire et la langue des textes de loi cités) et en **anglais** (traduction, cf. stratégie i18n [04-architecture-technique.md](./04-architecture-technique.md)). En cas de litige d'interprétation, la version française fait foi.

## 12. Points ouverts

- Validation juridique complète par un avocat/juriste ivoirien avant mise en production (obligatoire, ce document n'a pas valeur d'avis juridique).
- Constitution effective de la société éditrice (forme juridique, RCCM, NCC) avant exploitation commerciale réelle — à date, le projet n'a pas encore de structure juridique constituée.
- Vérifier si une **déclaration ou autorisation de traitement de données personnelles auprès de l'ARTCI** est requise avant le lancement, et dans quel délai.
- Confirmer l'applicabilité exacte du droit de rétractation / garanties légales du droit de la consommation ivoirien aux produits agricoles périssables.
- Choix de l'hébergeur (impact sur la localisation des données, à documenter dans la politique de confidentialité une fois tranché).
- Rédaction finale multilingue (FR réf. + EN) par un professionnel, ce document n'étant qu'une structure de contenu.
- Déterminer si la plateforme vise effectivement des utilisateurs résidant dans l'UE à court/moyen terme (diaspora) : si oui, formaliser la conformité RGPD au sens strict (et non plus seulement "alignée"), ce qui peut impliquer des obligations supplémentaires (désignation d'un représentant UE, etc.).
