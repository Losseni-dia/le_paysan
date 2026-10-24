# Backlog — idées non planifiées

Statut : v1.0 — 2026-10-22. Liste d'améliorations identifiées mais pas encore priorisées ni démarrées.

- [x] URLs produit avec slug plutôt que l'ID numérique (ex. `/produits/tomates-fraiches-13`). Fait le 2026-10-23 : colonne `slug` unique (slugify(nom) + id, stable), backfill appliqué, route web `/produits/:slug`, endpoint `/api/v1/products/{slugOrId}` compatible avec les anciens liens par id numérique.
