# Backend — Local Services Marketplace

API REST Spring Boot 3.5 (Java 21) + MongoDB, sécurisée par JWT.

## Prérequis
- **JDK 21** (Java 8 ne marche pas avec Spring Boot 3). Si Java 8 est le défaut du PC : `. .\scripts\java21.ps1` depuis la racine du projet.
- Pas besoin d'installer Maven : le wrapper `mvnw` le télécharge.
- MongoDB sur `localhost:27017` : `docker compose up -d` à la racine du projet.

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
| `test` | tests automatiques | base `testhakni_test`, pas de seed |

| Variable | Défaut | Rôle |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/testhakni` | Connexion MongoDB |
| `PORT` | `8080` | Port de l'API |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Origines autorisées (frontend) |
| `JWT_SECRET` | clé de dev | Clé HMAC en Base64 (≥ 256 bits). **À changer en production** |
| `JWT_ACCESS_TTL` / `JWT_REFRESH_TTL` | `15m` / `7d` | Durée de vie des tokens |
| `SEED_ENABLED` / `SEED_PASSWORD` | `true` / `Password123!` | Données de démo (profil dev) |
| `MONGODB_TEST_URI` | `mongodb://localhost:27017/testhakni_test` | Base des tests |

## Endpoints (Sprint 2)
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
