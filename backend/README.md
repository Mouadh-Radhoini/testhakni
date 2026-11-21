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
- Tests unitaires (Mockito, sans base) : `AuthServiceTest`, `JwtServiceTest`, `ServiceOfferingServiceTest`.
- Tests API (MockMvc + MongoDB) : `AuthControllerTest`, `ServiceControllerTest`, `RequestControllerTest`, `HealthControllerTest`.

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
| `SEED_ENABLED` / `SEED_PASSWORD` | `true` / `Password123!` | Données de démo (profil dev) |
| `MONGODB_TEST_URI` | `mongodb://localhost:27017/testhakni_test` | Base des tests |

## Endpoints de compte, catalogue et demandes
| Méthode | URL | Accès | Rôle |
|---|---|---|---|
| POST | `/api/auth/register/customer` | public | Inscription client |
| POST | `/api/auth/register/provider` | public | Inscription prestataire + profil pro |
| POST | `/api/auth/login` | public | Connexion → access + refresh token |
| POST | `/api/auth/refresh` | public | Nouveau couple de tokens |
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

## Organisation du code
```
com.testhakni.backend
├── config/       sécurité (matrice des rôles), CORS, Swagger, MongoDB
├── controller/   endpoints REST (/api/...)
├── service/      logique métier (règles, vérification du propriétaire)
├── repository/   accès MongoDB (Spring Data)
├── model/        documents MongoDB + énumérations
├── dto/          objets de requête / réponse + validation
├── security/     JWT : génération, filtre, réponses 401/403
├── exception/    format d'erreur unique (ApiError) + handler global
└── seed/         données de démonstration (profil dev)
```

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
