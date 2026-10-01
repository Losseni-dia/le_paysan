# 06 — Charte graphique

Statut : v1.0 — 2026-10-01 (nuances proposées, couleur finale à valider par l'utilisateur)

## 1. Principe

Identité "vert forêt et blanc" : évoque le végétal, l'agriculture, le naturel et la confiance. Le blanc apporte la clarté et la lisibilité du catalogue produits. Plusieurs nuances de vert sont proposées ci-dessous ; une fois le choix fait, remplacer `--color-primary` dans le token CSS (§4).

## 2. Nuances de vert proposées

| Nom | Hex | Usage suggéré |
|---|---|---|
| Vert forêt très foncé | `#0B3D1E` | Texte sur fond clair, header sombre |
| Vert forêt (référence) | `#1B5E3A` | Couleur primaire (boutons, liens, accents) |
| Vert forêt moyen | `#2E7D4F` | Hover, éléments secondaires |
| Vert sauge | `#4C9A6A` | Badges, états positifs (ex: "Validé") |
| Vert tendre | `#7FB896` | Fonds de section, highlights légers |
| Vert pâle | `#D7ECDF` | Fonds de carte, zones discrètes |
| Blanc | `#FFFFFF` | Fond principal |
| Blanc cassé | `#F7FAF8` | Fond alternatif (sections, cartes) |

Couleurs fonctionnelles (indépendantes du choix de vert) :

| Usage | Hex |
|---|---|
| Succès (déjà couvert par vert sauge) | `#4C9A6A` |
| Avertissement | `#C98A1E` |
| Erreur | `#B3261E` |
| Info | `#2E6E9E` |
| Texte principal | `#1A1F1C` |
| Texte secondaire | `#5B6560` |
| Bordures | `#E1E8E3` |

> L'utilisateur choisit la nuance exacte de "vert primaire" parmi la palette ci-dessus (ou une variante proche) ; le reste de l'échelle s'ajuste en conséquence pour garder un contraste AA suffisant texte/fond.

## 3. Typographie

- Police principale : **Inter** ou **Poppins** (sans-serif, lisible, moderne) pour les titres et le corps.
- Échelle : H1 32px / H2 24px / H3 20px / Corps 16px / Petit texte 14px.
- Poids : 700 titres, 500 boutons/labels, 400 corps de texte.

## 4. Design tokens (CSS variables — à placer dans le frontend Angular)

```css
:root {
  --color-primary: #1B5E3A;
  --color-primary-dark: #0B3D1E;
  --color-primary-hover: #2E7D4F;
  --color-accent: #4C9A6A;
  --color-tint: #7FB896;
  --color-surface-tint: #D7ECDF;
  --color-bg: #FFFFFF;
  --color-bg-alt: #F7FAF8;
  --color-text: #1A1F1C;
  --color-text-muted: #5B6560;
  --color-border: #E1E8E3;
  --color-success: #4C9A6A;
  --color-warning: #C98A1E;
  --color-danger: #B3261E;
  --color-info: #2E6E9E;

  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
  --shadow-card: 0 1px 3px rgba(11, 61, 30, 0.08), 0 1px 2px rgba(11, 61, 30, 0.06);
}
```

Mode sombre : à définir ultérieurement si besoin (hors MVP).

## 5. Composants UI de base à prévoir dans le shared UI kit Angular

- Boutons : primaire (fond vert, texte blanc), secondaire (contour vert, texte vert), tertiaire (texte seul).
- Badges de statut : `EN_ATTENTE` (ambre), `VALIDE`/`VALIDEE` (vert sauge), `REJETE`/`REJETEE` (rouge), `SUSPENDUE` (gris).
- Carte produit (image, nom, prix, boutique, badge disponibilité).
- Carte boutique (logo, nom, catégorie, statut).
- **Stepper / wizard générique** réutilisable pour tout formulaire multi-étapes : demande de boutique (Type → Infos boutique → Justificatifs → Récapitulatif), création de produit (Infos → Photos → Livraison → Récapitulatif), tunnel de commande (Panier → Livraison → Paiement → Confirmation). Indicateur d'étapes en haut (numéros + labels), navigation précédent/suivant, validation par étape avant de pouvoir avancer.
- Formulaire adaptatif (sélecteur Personne physique / Entreprenant / Personne morale) avec sections conditionnelles, intégré dans le stepper ci-dessus.
- Barre de recherche + filtres catalogue.
- Sélecteur de langue (FR / EN), composant réutilisable (header public + sidebar back-office).

## 6. Structure visuelle du back-office (espace vendeur et admin)

Layout en **sidebar fixe à gauche** + zone de contenu à droite (pattern "tiroir" de navigation latéral, non un tiroir qui se superpose en overlay — toujours visible sur desktop, repliable en icônes seules ou masquable/`drawer` en overlay sur mobile).

```
┌───────────────┬──────────────────────────────────────────┐
│  Sidebar       │  Topbar : fil d'ariane + langue + profil │
│  (gauche)      ├──────────────────────────────────────────┤
│                │                                           │
│  [Logo]        │                                           │
│                │              Zone de contenu              │
│  Tableau de    │              (liste, formulaire,          │
│    bord        │               détail...)                  │
│  Boutiques     │                                           │
│  Produits      │                                           │
│  Commandes     │                                           │
│  Utilisateurs  │                                           │
│  Statistiques  │                                           │
│  Paramètres    │                                           │
│                │                                           │
│  ⬅ Replier     │                                           │
└───────────────┴──────────────────────────────────────────┘
```

- **Sidebar admin** : Tableau de bord, Demandes de boutiques, Produits à valider, Utilisateurs, Commandes, Catégories, Taux de commission, Statistiques, Paramètres.
- **Sidebar vendeur** : Tableau de bord, Ma boutique, Mes produits, Commandes reçues, Statistiques de vente, Paramètres du compte.
- Item de sidebar actif mis en évidence avec `--color-primary` / `--color-surface-tint` en fond.
- Sidebar repliable (icônes seules) pour laisser plus de place au contenu ; en dessous d'un breakpoint mobile, elle se comporte comme un panneau (`drawer`) ouvert/fermé par un bouton hamburger dans la topbar.
- Topbar commune aux deux back-offices : fil d'ariane, sélecteur de langue (FR/EN), menu profil (déconnexion, paramètres).

## 7. Logo

Pas de logo fourni à ce stade — point ouvert, à définir avec l'utilisateur (texte "Le Paysan" en typographie + pictogramme feuille/épi possible).

## 8. Points ouverts

- Validation finale de la nuance de vert primaire par l'utilisateur.
- Création du logo.
