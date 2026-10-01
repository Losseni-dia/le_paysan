# Documentation — Le Paysan

Marketplace de vente de produits agricoles (fermes/boutiques, catalogue validé par un admin, paiement Mobile Money via Fedapay). Backend Spring Boot, frontend Angular, base PostgreSQL `le_paysan`.

Cette documentation est mise à jour au fur et à mesure de l'avancement du projet (voir [07-plan-execution.md](./07-plan-execution.md) pour le suivi).

## Sommaire

1. [Analyse des besoins](./01-analyse-besoins.md) — contexte, acteurs, besoins fonctionnels/non fonctionnels, périmètre MVP.
2. [Spécifications fonctionnelles](./02-specifications-fonctionnelles.md) — rôles, cas d'utilisation détaillés, règles de gestion.
3. [Modèle de données](./03-modele-donnees.md) — dictionnaire de données + [script SQL](./sql/schema.sql).
4. [Architecture technique](./04-architecture-technique.md) — architecture backend/frontend, sécurité, intégration Fedapay.
5. [Spécification API](./05-api-specification.md) — endpoints REST par domaine.
6. [Charte graphique](./06-charte-graphique.md) — palette vert forêt/blanc, typographie, composants UI.
7. [Plan d'exécution](./07-plan-execution.md) — phases, tâches, suivi d'avancement.
8. [Mentions légales, CGU/CGV, Politique de confidentialité](./08-mentions-legales-cgu-rgpd.md) — cadre juridique ivoirien, alignement RGPD volontaire, CGU/CGV, protection des données, cookies.

## Convention de mise à jour

- Chaque document porte un statut et une date en en-tête ; incrémenter/dater à chaque modification notable.
- Les points ouverts sont listés en fin de chaque document ; les lever au fur et à mesure des décisions prises avec l'utilisateur.
- Le plan d'exécution ([07](./07-plan-execution.md)) est la source de vérité de l'avancement réel : cocher les tâches terminées.
