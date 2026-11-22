# Backend — Local Services Marketplace

API REST Spring Boot 3.5 (Java 21) + MongoDB, sécurisée par JWT.

## Prérequis
- **JDK 21** (Java 8 ne marche pas avec Spring Boot 3). Si Java 8 est le défaut du PC : `. .\scripts\java21.ps1` depuis la racine du projet.
- Pas besoin d'installer Maven : le wrapper `mvnw` le télécharge.
- MongoDB sur `localhost:27017` : `docker compose up -d --wait` à la racine du projet.

## Lancer
```bash
cd backend
./mvnw spring-boot:run        # Windows : .\mvnw.cmd spring-boot:run
```
- Santé : http://localhost:8080/api/health
- Swagger : http://localhost:8080/swagger-ui.html. Clique sur **Authorize** et colle l'`accessToken` obtenu via `POST /api/auth/login`.

## Tests
```bash
./mvnw test                   # Windows : .\mvnw.cmd test
```
- Les tests utilisent le profil `test` et la base **`testhakni_test`**, vidée avant chaque test. Ta base de dev n'est jamais touchée.
- Tests unitaires (Mockito, sans base, ports mockés) : `AuthServiceTest`, `JwtServiceTest`, `ServiceOfferingServiceTest`, `MarketplaceServicesTest`.
- Tests API (MockMvc + MongoDB) : `AuthControllerTest`, `ServiceControllerTest`, `RequestControllerTest`, `HealthControllerTest`, `MarketplaceWorkflowTest`.
- Les tests sont rangés dans les mêmes paquets que le code qu'ils vérifient.

## Profils et variables d'environnement
| Profil | Quand | Effet |
|---|---|---|
| `dev` | par défaut | données de démo (seed) si la base est vide, logs DEBUG |
| `prod` | pile de production | secret JWT externe obligatoire, sans seed |
| `test` | tests automatiques | base `testhakni_test`, pas de seed |

| Variable | Défaut | Rôle |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/testhakni` | Connexion MongoDB |
| `PORT` | `8080` | Port de l'API |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origines autorisées (frontend) |
| `JWT_SECRET` | clé de dev | Clé HMAC en Base64 (≥ 256 bits). **Obligatoire en production** |
| `JWT_ACCESS_TTL` / `JWT_REFRESH_TTL` | `15m` / `7d` | Durée de vie des tokens |
| `JWT_ISSUER` / `JWT_AUDIENCE` | `testhakni-backend` / `testhakni-web` | Claims `iss` / `aud` vérifiés à chaque requête |
| `RATE_LIMIT_ENABLED` | `true` | Limitation anti force brute sur `/api/auth` (à couper seulement pour une démo répétée) |
| `SEED_ENABLED` / `SEED_PASSWORD` | `true` / `Password123!` | Données de démo (profil dev) |
| `MONGODB_TEST_URI` | `mongodb://localhost:27017/testhakni_test` | Base des tests |

## Endpoints de compte, catalogue et demandes
| Méthode | URL | Accès | Rôle |
|---|---|---|---|
| POST | `/api/auth/register/customer` | public | Inscription client |
| POST | `/api/auth/register/provider` | public | Inscription prestataire + profil pro |
| POST | `/api/auth/login` | public | Connexion → access + refresh token |
| POST | `/api/auth/refresh` | public | Nouveau couple de tokens (l'ancien refresh token devient inutilisable) |
| POST | `/api/auth/logout` | public | Révoque le refresh token et sa chaîne (204, idempotent) |
| GET / PUT | `/api/users/me` | connecté | Lire / modifier mon compte |
| PUT | `/api/users/me/password` | connecté | Changer mon mot de passe |
| GET / PUT | `/api/providers/me/profile` | PROVIDER | Mon profil professionnel |
| GET | `/api/providers/{id}` | public | Profil public + services actifs |
| GET | `/api/categories` | public | Catégories de services |
| GET | `/api/services` | public | Recherche : `q`, `category`, `city`, `minPrice`, `maxPrice`, `sort`, `page`, `size` |
| GET | `/api/services/{id}` | public | Détail d'un service |
| GET | `/api/services/mine` | PROVIDER | Mes services |
| POST | `/api/services` | PROVIDER | Publier un service |
| PUT | `/api/services/{id}` | PROVIDER (propriétaire) | Modifier |
| PATCH | `/api/services/{id}/status` | PROVIDER (propriétaire) | Activer / désactiver |
| DELETE | `/api/services/{id}` | PROVIDER (propriétaire) | Supprimer |
| POST | `/api/requests` | CUSTOMER | Créer une demande |
| GET | `/api/requests/mine?status=` | CUSTOMER | Mes demandes |
| GET | `/api/requests/{id}` | CUSTOMER (propriétaire) | Détail |
| PATCH | `/api/requests/{id}/cancel` | CUSTOMER (propriétaire) | Annuler une demande ouverte |

Codes d'erreur : `400` données invalides (détail dans `fieldErrors`), `401` non connecté ou token invalide, `403` mauvais rôle ou ressource d'un autre utilisateur, `404` introuvable, `409` conflit (email déjà pris, demande plus ouverte).

## Photos et messagerie

| Méthode | URL | Accès | Rôle |
|---|---|---|---|
| GET | `/api/media/{id}` | public | Lire une image |
| PUT / DELETE | `/api/users/me/avatar` | connecté | Changer / supprimer sa photo de profil (`multipart/form-data`, champ `file`) |
| POST | `/api/providers/me/photos` | PROVIDER | Ajouter une photo de réalisation (6 max) |
| DELETE | `/api/providers/me/photos/{id}` | PROVIDER | Supprimer une de ses photos |
| PATCH | `/api/bookings/{id}/customer-status` | CUSTOMER (propriétaire) | Le client déclare la prestation `IN_PROGRESS`, `COMPLETED` ou l'annule (`CANCELLED`, impossible une fois terminée). La demande liée suit, le prestataire est notifié |
| GET | `/api/conversations` | CUSTOMER, PROVIDER | Mes conversations (avec nombre de non-lus) |
| GET | `/api/conversations/unread` | CUSTOMER, PROVIDER | Total des messages non lus |
| POST | `/api/conversations/by-offer/{offerId}` | client de la demande ou prestataire de l'offre | Ouvrir / retrouver le fil |
| POST | `/api/conversations/by-booking/{bookingId}` | participants | Ouvrir / retrouver le fil d'une réservation |
| GET / POST | `/api/conversations/{id}/messages` | participants | Historique (50 par page, `?before=`) / envoyer |
| POST | `/api/conversations/{id}/read` | participants | Marquer comme lu |
| POST | `/api/conversations/with/{userId}` | contact accepté | Ouvrir / retrouver une discussion directe (sans demande de travail) |
| GET | `/api/contacts` | CUSTOMER, PROVIDER | Mes contacts et demandes en attente |
| GET | `/api/contacts/pending` | CUSTOMER, PROVIDER | Nombre de demandes reçues en attente |
| GET | `/api/contacts/with/{userId}` | CUSTOMER, PROVIDER | Ma relation avec cette personne (NONE, PENDING_OUT, PENDING_IN, ACCEPTED, DECLINED) |
| POST | `/api/contacts` | CUSTOMER, PROVIDER | Demande de contact (`{targetId}`), toujours entre un client et un prestataire |
| POST | `/api/contacts/{id}/accept` · `/decline` | destinataire | Répondre à une demande |
| DELETE | `/api/contacts/{id}` | participants | Annuler sa demande ou retirer un contact (ferme la discussion directe) |
| WS | `/ws` | JWT dans `CONNECT` | STOMP, s'abonner à `/user/queue/messages` pour recevoir les messages en direct |

## Sécurité

| Mesure | Détail |
|---|---|
| JWT signé HS256 | Access token 15 min, refresh token 7 jours. Chaque token porte `iss`, `aud`, `jti` et son type, tous vérifiés. Aucune donnée personnelle (email) dans le token. |
| Identité relue en base | À chaque requête, le filtre JWT recharge le compte : suspension et changement de rôle prennent effet immédiatement. |
| Rotation des refresh tokens | Chaque refresh token est enregistré (collection `refresh_tokens`, purgée par index TTL) et ne sert qu'une fois. Rejouer un token déjà utilisé révoque toute la session (vol présumé). |
| Révocation | `POST /api/auth/logout`, changement de mot de passe et suspension d'un compte révoquent les refresh tokens concernés. |
| Anti force brute (Bucket4j) | Par IP : 10 connexions/min, 10 inscriptions/h, 30 refresh/min. Par compte : 5 échecs de connexion → blocage 15 min. Réponse `429` + `Retry-After`. |
| Mots de passe | BCrypt, politique forte (8 à 72 caractères, majuscule, minuscule, chiffre, caractère spécial). |
| En-têtes HTTP | `Content-Security-Policy`, `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`, `Permissions-Policy`, `X-Content-Type-Options`, HSTS en HTTPS. |
| Images (SEC-08) | Photos stockées en GridFS. Format reconnu à sa signature binaire (JPEG, PNG, WebP), 2 Mo maximum, 6 photos de réalisations par prestataire. Servies avec `nosniff` et un cache immuable. |
| Prix réservés aux membres | Sans compte, l'API ne renvoie qu'un aperçu des services (`locked: true` : titre, catégorie, ville). Filtrer ou trier par prix exige d'être connecté (401). |
| Messagerie (SEC-09) | Seuls le client et le prestataire d'un fil y accèdent. WebSocket `/ws` (STOMP) authentifié par le JWT dans la trame CONNECT, abonnement limité à `/user/queue/messages`, envoi uniquement par l'API REST. |
| Profil `prod` | Swagger et `/v3/api-docs` désactivés, pas de seed, aucun détail d'erreur ni stack trace renvoyé. |

## Organisation du code : Clean Architecture par fonctionnalité

Le code est découpé **par fonctionnalité métier**, et chaque fonctionnalité suit les mêmes **quatre couches** :

```
com.testhakni.backend
├── identity/       comptes, inscription, connexion, JWT
├── provider/       profil professionnel des prestataires
├── catalog/        services publiés, catégories, recherche
├── request/        demandes de service des clients
├── offer/          offres des prestataires, acceptation
├── booking/        réservations et suivi de la prestation
├── review/         avis clients et notes
├── notification/   notifications in-app
├── moderation/     administration, modération, signalements
└── shared/         noyau commun (erreurs, pagination, config Mongo/CORS/Swagger, seed)

<fonctionnalité>/
├── domain/           cœur métier, sans dépendance technique
│   ├── model/        entités et énumérations (+ règles, ex. BookingStatus.expectedPrevious)
│   └── repository/   ports : interfaces de dépôt dont le métier a besoin
├── application/      cas d'usage (services), DTO d'entrée/sortie avec validation, ports de sortie
├── infrastructure/   adaptateurs : MongoDB (Spring Data + MongoTemplate), sécurité JWT/BCrypt
└── presentation/     contrôleurs REST
```

Règle des dépendances : `presentation → application → domain ← infrastructure`.
- Les cas d'usage ne voient jamais MongoDB : ils appellent des ports (`UserRepository`, `OfferRepository`…) implémentés par des adaptateurs `*RepositoryAdapter` dans `infrastructure/persistence`. Les interfaces Spring Data (`*MongoRepository`) sont privées au paquet.
- Le domaine lève des `DomainException` portant un `ErrorType` (NOT_FOUND, CONFLICT…) ; seul `GlobalExceptionHandler` les traduit en codes HTTP.
- La génération de tokens et le hachage des mots de passe passent par les ports `TokenProvider` et `PasswordHasher` (implémentés par `JwtService` et `BCryptPasswordHasher`).
- Compromis assumé : les entités du domaine gardent les annotations de mapping MongoDB (`@Document`, `@Indexed`) plutôt que d'être dupliquées en classes de persistance avec des mappers.

## API du parcours de réservation

Swagger expose les schémas, paramètres, validations et méthodes à `/v3/api-docs` et permet de les essayer depuis `/swagger-ui.html`. Préfixer les chemins ci-dessous avec `/api`.

| Méthode et chemin | Accès et résultat |
|---|---|
| GET `/providers/me/requests` | Prestataire disponible, demandes ouvertes de sa ville et de ses catégories. Adresse exacte et identifiant du client exclus. |
| POST `/requests/{id}/offers` | Prestataire éligible. Corps `price, proposedDate, description`. Une offre par couple demande/prestataire. |
| GET `/providers/me/offers`, PATCH `/providers/me/offers/{id}/withdraw` | Lister ses propositions, retirer une offre en attente. |
| GET `/requests/{id}/offers` | Client propriétaire, liste triée par prix, avec prestataire, date et description. |
| POST `/requests/{id}/offers/{offerId}/accept` | Client propriétaire. Retourne la réservation, rejette les concurrentes. Un nouvel essai sur le gagnant retrouve la réservation en cours. |
| GET `/bookings`, GET `/bookings/{id}` | Client ou prestataire participant uniquement. |
| PATCH `/bookings/{id}/status` | Prestataire affecté. Corps `status`. BOOKED → IN_PROGRESS → AWAITING_CONFIRMATION. |
| POST `/bookings/{id}/confirm` | Client propriétaire. Passe la réservation et la demande à COMPLETED. |
| POST `/bookings/{id}/review` | Client après confirmation. Corps `rating, comment`. Un avis par réservation. |
| GET `/providers/{id}/reviews`, GET `/services/{id}/reviews` | Public. Avis visibles, nombre et moyenne. Le profil prestataire inclut aussi `reviewCount, averageRating`. |
| GET `/notifications?page=0`, GET `/notifications/unread`, PATCH `/notifications/{id}/read` | Utilisateur connecté. Pages de 30 éléments, nombre non lu et lecture idempotente, limités au destinataire. |
| POST `/reports` | Utilisateur connecté. Corps `targetType` (SERVICE ou REVIEW), `targetId, reason`. |
| GET `/admin/dashboard` | ADMIN. Nombre d’utilisateurs, services, demandes, offres, réservations, avis et signalements ouverts. |
| GET `/admin/users?role=&status=`, GET `/admin/users/{id}`, PATCH `/admin/users/{id}` | ADMIN. Filtres optionnels. Activation/suspension avec `enabled, reason`. |
| GET `/admin/services`, GET `/admin/reviews`, PATCH `/admin/services/{id}`, PATCH `/admin/reviews/{id}` | ADMIN. Modération avec `enabled, reason`, décision et auteur audités. |
| GET `/admin/reports`, PATCH `/admin/reports/{id}` | ADMIN. Clôturer par `status` (RESOLVED ou DISMISSED) et `reason`. |

L’annulation existante `PATCH /requests/{id}/cancel` annule aussi les offres en attente. Les notifications persistées concernent le client pour une offre reçue ou retirée, les prestataires pour les décisions ou l’annulation, l’autre participant pour les changements de réservation, le prestataire pour un nouvel avis et l’auteur pour une modération.

Pour le déploiement, les variables de la pile, les backups et le dépannage, consulter le [guide d’exploitation](../docs/EXPLOITATION.md).
