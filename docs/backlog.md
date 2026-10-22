# Backlog — idées non planifiées

Statut : v1.0 — 2026-10-22. Liste d'améliorations identifiées mais pas encore priorisées ni démarrées.

- [ ] URLs produit avec slug plutôt que l'ID numérique (ex. `/produits/tomates-bio-abidjan` au lieu de `/produits/12`). Nécessite : colonne `slug` sur `products` (génération + unicité), migration/backfill des produits existants, mise à jour des routes web (`app.routes.ts`, liens catalogue/panier/commandes), décision sur la compatibilité des anciennes URLs par ID (redirection ou abandon). *(signalé le 2026-10-22)*
