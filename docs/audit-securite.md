# Audit sécurité — Le Paysan

Date : 2026-10-23. Audit du code réel (backend Spring Boot, web Angular, mobile Flutter) — pas une checklist générique.

## 🔴 Critique

Aucun. Pas de secret exposé dans le dépôt, pas de CORS wildcard, pas de JWT hardcodé en prod, pas de `ddl-auto: update`.

## 🟠 Haute

- [x] **Révocation JWT — corrigé le 2026-10-24.** `User.tokenValidFrom`, mis à jour au changement de mot de passe (volontaire ou réinitialisation), vérifié dans `JwtAuthFilter` et `AuthService.refresh` contre le claim `iat` du token. Testé en live : ancien token → 401 après changement, nouveau token → 200.
- [x] **Race condition sur le stock produit — corrigé le 2026-10-24.** `@Version` sur `Product` (verrou optimiste général) + verrou pessimiste (`ProductRepository.findByIdForUpdate`, `SELECT ... FOR UPDATE`) dans `PaymentService.onPaymentApproved`, le chemin concret de décrémentation. *(Non testé en situation de concurrence réelle, revue de code uniquement.)*
- [x] **Accès fichiers privés sans vérification de propriété — corrigé le 2026-10-24.** `FileAccessService` vérifie que l'appelant est propriétaire (ou admin) du fichier précis demandé, pour chaque catégorie privée (`shop-kyc`, `security-selfies`, `livreur-kyc`/`livreur-photos`, `account-recovery`, `delivery-proofs`) via requêtes croisées sur les tables propriétaires. Catégorie inconnue → refusée par défaut. Testé en live : fichier d'un autre compte → 403, fichier public → 200 sans auth.
- [ ] **Trafic mobile en HTTP non-HTTPS** : bloquant pour un build release tant que le backend n'est pas servi en HTTPS (Android bloque le cleartext par défaut). *Pas une fuite active, mais empêche toute distribution réelle. Décision d'hébergement, pas de code.*

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

1. ~~Contrôle d'accès fin sur `FileController` (fichiers KYC)~~ — fait le 2026-10-24.
2. ~~Verrou sur `Product.stockQuantity`~~ — fait le 2026-10-24.
3. ~~Révocation JWT au changement de mot de passe~~ — fait le 2026-10-24.
4. **HTTPS backend** (bloque toute distribution mobile réelle) — dernier point haut restant, décision d'hébergement.
5. Le reste (CSP, verrouillage compte, chiffrement idDocumentNumber, fenêtre replay webhook) — amélioration continue, pas bloquant.
