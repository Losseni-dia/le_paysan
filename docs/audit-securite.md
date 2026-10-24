# Audit sécurité — Le Paysan

Date : 2026-10-23. Audit du code réel (backend Spring Boot, web Angular, mobile Flutter) — pas une checklist générique.

## 🔴 Critique

Aucun. Pas de secret exposé dans le dépôt, pas de CORS wildcard, pas de JWT hardcodé en prod, pas de `ddl-auto: update`.

## 🟠 Haute

- **Pas de révocation JWT** (`JwtService.java`) : refresh token valide 7 jours même après changement de mot de passe. MVP stateless assumé, mais à corriger avant une vraie mise en prod (table de révocation ou invalidation au changement de mdp).
- **Race condition sur le stock produit** (`OrderService`/`PaymentService`) : décrémentation du stock sans verrou (`@Version` absent sur `Product`) → survente possible si deux paiements concurrents pour le même produit.
- **Accès fichiers privés sans vérification de propriété** (`FileController.java`, TODO explicite dans le code) : un utilisateur authentifié qui obtient une URL de document KYC d'un autre (UUID) peut la charger. Protection actuelle = UUID difficile à deviner, pas un vrai contrôle d'accès.
- **Trafic mobile en HTTP non-HTTPS** : bloquant pour un build release tant que le backend n'est pas servi en HTTPS (Android bloque le cleartext par défaut). *Pas une fuite active, mais empêche toute distribution réelle.*

## 🟡 Moyenne

- Refresh token web stocké en `localStorage` (XSS risk) — l'access token lui reste en mémoire, c'est déjà un bon compromis.
- Pas de Content-Security-Policy (web).
- Rate limiting auth = par IP uniquement, pas de verrouillage de compte ; en mémoire locale (pas multi-instance).
- Pas de fenêtre de validité sur la signature webhook Fedapay (replay possible, impact limité car idempotence déjà correcte côté transaction).
- `idDocumentNumber` stocké en clair (pas chiffré).

## 🟢 Déjà correct (vérifié, pas de faux positifs)

- `.env` jamais tracké par git, bien dans `.gitignore`.
- CORS : origines lues par variable d'env, aucun `@CrossOrigin("*")`.
- Rate limiting sur `/auth/**` déjà en place (`AuthRateLimitFilter`).
- `ddl-auto: none` partout, logs SQL coupés en prod.
- Swagger désactivé hors dev.
- Montants financiers en `BigDecimal`/`NUMERIC`, jamais `double`.
- Isolation vendeur/acheteur correcte sur commandes et paiements (`@PreAuthorize`, vérification de propriété).
- Path traversal : protection correcte dans `FileStorageService`.
- Taille max upload configurée (15MB/40MB).
- Mobile : token stocké via `flutter_secure_storage` (Keystore), pas de `shared_preferences` en clair. Permissions Android minimales et justifiées. Pas de `[innerHTML]`/`bypassSecurityTrust` côté web.
- Webhook Fedapay : signature HMAC vérifiée en temps constant, idempotence correcte.

## Priorité de remédiation recommandée

1. HTTPS backend (bloque toute distribution mobile réelle)
2. Contrôle d'accès fin sur `FileController` (fichiers KYC)
3. Verrou optimiste (`@Version`) sur `Product.stockQuantity`
4. Révocation JWT au changement de mot de passe
5. Le reste (CSP, verrouillage compte, chiffrement idDocumentNumber, fenêtre replay webhook) — amélioration continue, pas bloquant.
