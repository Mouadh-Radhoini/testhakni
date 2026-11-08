# Bilan des Sprints 1 et 2 — Local Services Marketplace

> Compte rendu de ce qui a été réalisé pendant les deux premiers sprints : ce qui a été construit, comment ça marche, pourquoi ces choix, et comment le vérifier.
> Branche : `feature/SET-01-backend` · Date : 08/11/2026

---

## 1. Résumé

| Objectif du sprint (voir [SPRINT_PLAN.md](SPRINT_PLAN.md)) | Résultat |
|---|---|
| **Sprint 1** — backend, frontend et MongoDB démarrent, la CI tourne | ✅ Atteint |
| **Sprint 2** — client et prestataire s'inscrivent et se connectent ; le prestataire publie ses services ; le client cherche et crée une demande | ✅ Atteint |

**En chiffres**

| | Backend (Spring Boot) | Frontend (Angular) |
|---|---|---|
| Code applicatif | 60 fichiers Java, ~2 500 lignes | 31 fichiers TypeScript, ~2 400 lignes |
| Tests | 9 classes, **40 tests** — tous verts | 8 fichiers, **24 tests** — tous verts |
| Endpoints / pages | 20 endpoints REST documentés dans Swagger | 15 pages |

Le parcours complet a aussi été vérifié **à la main dans le navigateur** avec les deux serveurs lancés :
1. **Client** : connexion → recherche filtrée → détail d'un service → « Faire une demande » (d'abord refusée car la description était trop courte, puis acceptée) → la demande apparaît dans « Mes demandes ».
2. **Prestataire** : connexion → publication d'un service « sur devis » → désactivation → profil pro pré-rempli.
3. **Sécurité** : un client qui ouvre `/provider/services` est renvoyé vers « Accès refusé ».

---

## 2. Architecture générale

```
 Navigateur (Angular, port 4200)
        │  HTTP JSON  +  en-tête "Authorization: Bearer <JWT>"
        ▼
 API Spring Boot (port 8080)
   Filtre JWT ──► Règles par rôle ──► Controller ──► Service (règles métier) ──► Repository
        │                                                                          │
        └──────────── erreurs → GlobalExceptionHandler → JSON ApiError             ▼
                                                                         MongoDB (base "testhakni")
```

**Le principe** : le frontend ne fait qu'afficher et envoyer des données. Toutes les règles (qui a le droit de faire quoi, validation, propriété des ressources) sont **vérifiées côté serveur**. Le frontend refait certaines vérifications uniquement pour le confort de l'utilisateur (messages immédiats). Une personne qui appellerait l'API directement, sans passer par l'interface, ne pourrait donc rien contourner.

### Organisation du backend (en couches)
| Couche | Rôle | Exemple |
|---|---|---|
| `controller/` | Reçoit la requête HTTP, valide le JSON (`@Valid`), appelle le service | `ServiceController` |
| `service/` | Règles métier : propriétaire, statuts, transitions | `ServiceOfferingService.loadOwned()` |
| `repository/` | Accès MongoDB via Spring Data | `UserRepository.findByEmail()` |
| `model/` | Documents MongoDB et énumérations | `User`, `ServiceOffering`, `ServiceRequest` |
| `dto/` | Ce qui entre et sort de l'API (jamais le modèle directement) | `RegisterProviderRequest`, `UserResponse` |
| `security/` | JWT : création, vérification, réponses 401/403 | `JwtService`, `JwtAuthenticationFilter` |
| `exception/` | Format d'erreur unique | `ApiError`, `GlobalExceptionHandler` |

> **Pourquoi des DTO séparés des modèles ?** Pour ne jamais renvoyer par erreur un champ sensible (le hash du mot de passe par exemple), et pour empêcher un utilisateur d'envoyer des champs qu'il n'a pas le droit de modifier (son rôle, son statut). Un test le vérifie : envoyer `"role": "ADMIN"` dans `PUT /api/users/me` n'a aucun effet.

---

## 3. Sprint 1 — Setup et fondations

### SET-01 — Backend Spring Boot
- Spring Boot **3.5.7**, **Java 21**, Maven Wrapper (`mvnw`) : pas besoin d'installer Maven.
- Dépendances : Web, Data MongoDB, Validation, Security, Actuator, springdoc (Swagger), Lombok, jjwt (JWT).
- Endpoint de santé `GET /api/health` et Swagger UI sur `/swagger-ui.html`.
- Problème rencontré : le PC avait **Java 8** par défaut, alors que Spring Boot 3 exige Java 17 ou plus. Le script [`scripts/java21.ps1`](../scripts/java21.ps1) active Java 21 dans la fenêtre PowerShell courante, sans toucher à la configuration du système.

### SET-02 — Frontend Angular
- Angular **20** en composants *standalone*, état géré avec des **signals**, styles SCSS.

### DB-01 — Configuration MongoDB
- [`docker-compose.yml`](../docker-compose.yml) : MongoDB 8 avec un volume persistant et un *healthcheck*. Le port est configurable (`MONGO_PORT`) car un autre projet occupait déjà 27017 sur le PC.
- [`MongoConfig`](../backend/src/main/java/com/testhakni/backend/config/MongoConfig.java) :
  - **audit automatique** : `createdAt` et `updatedAt` sont remplis par Spring ;
  - **prix stockés en `Decimal128`** : par défaut, Spring Data stocke les `BigDecimal` sous forme de *texte*. Les filtres `prix min / max` et le tri par prix ne marcheraient alors pas (en texte, `"100" < "60"`). Ce piège est corrigé par deux convertisseurs.
- **Profils Spring** :
  - `dev` (par défaut) : seed des données de démo et logs détaillés ;
  - `test` : base séparée `testhakni_test` pour que les tests ne touchent jamais les données de dev.

### DB-02 — Modèle de données, index et seed
| Collection | Contenu | Index |
|---|---|---|
| `users` | email, hash BCrypt, nom, téléphone, ville, rôle, statut | **email unique**, rôle |
| `provider_profiles` | nom commercial, bio, catégories, compétences, ville, zone, expérience, disponibilité | **userId unique** (relation 1-1 avec le compte) |
| `services` | prestataire, titre, description, catégorie, ville, prix, type de prix, statut | providerId, (statut, catégorie, ville) |
| `service_requests` | client, service d'origine (optionnel), titre, description, catégorie, ville, adresse, date, budget, statut | customerId, (statut, catégorie, ville) |

- Les index sont créés automatiquement au démarrage (`auto-index-creation: true`). L'index unique sur l'email protège même en cas de deux inscriptions simultanées.
- [`DataSeeder`](../backend/src/main/java/com/testhakni/backend/seed/DataSeeder.java) : si la base est vide, il crée 4 comptes (admin, client, 2 prestataires), 2 profils, 6 services et 1 demande. Mot de passe : `Password123!`.

### FE-01 — Socle Angular
- Mise en page commune : barre de navigation qui **change selon le rôle**, contenu, pied de page.
- Toutes les pages sont **chargées à la demande** (*lazy loading*) : le bundle initial reste à ~90 ko compressé.
- Page 404 et page « Accès refusé ».
- **Proxy de dev** ([`proxy.conf.js`](../frontend/proxy.conf.js)) : le frontend appelle `/api/...` et `ng serve` transmet au backend. Il n'y a pas de CORS en dev et le port du backend se change avec `API_PORT`.
- Styles globaux dans [`styles.scss`](../frontend/src/styles.scss) : cartes, formulaires, boutons, badges de statut.

### SET-03 — CI GitHub Actions
[`.github/workflows/ci.yml`](../.github/workflows/ci.yml) s'exécute à chaque push sur `main` / `feature/**` et à chaque pull request. Il contient deux jobs en parallèle :
- **backend** : un conteneur MongoDB de service, Java 21, puis `./mvnw verify` (compilation + 40 tests) ;
- **frontend** : Node 22, `npm ci`, build de production, puis les tests dans Chrome headless.

### SET-04 — Documentation
- [README racine](../README.md) : démarrage rapide, comptes de démo, astuce « port 8080 occupé », **conventions Git** (branches `feature/<ID>-...`, PR obligatoire, messages de commit, pas de secrets).
- [backend/README.md](../backend/README.md) : variables d'environnement, profils, **tableau de tous les endpoints** et codes d'erreur.
- [frontend/README.md](../frontend/README.md) : proxy, tests, **tableau des pages**, organisation du code.

### TEST-00 — Socle de tests
- Backend : la classe [`IntegrationTest`](../backend/src/test/java/com/testhakni/backend/support/IntegrationTest.java) sert de base à tous les tests d'API. Elle démarre le vrai contexte Spring avec le profil `test` et **vide la base avant chaque test** (en gardant les index). Elle fournit aussi des raccourcis : `registerCustomer()`, `registerProvider()`, `token()`.
- Frontend : Karma + Jasmine, exécutables en mode headless comme sur la CI.

---

## 4. Sprint 2 — Authentification, services, recherche, demandes

### 4.1 Authentification (AUTH-01 → AUTH-09)

| Item | Ce qui a été fait |
|---|---|
| **AUTH-01** Modèle User | Document `User` avec rôle (`CUSTOMER`, `PROVIDER`, `ADMIN`), statut (`ACTIVE`, `SUSPENDED`) et email unique en minuscules |
| **AUTH-02** Inscription client | `POST /api/auth/register/customer` → 201 + tokens. Email déjà pris → **409** |
| **AUTH-03** Inscription prestataire | `POST /api/auth/register/provider` : téléphone, ville, nom commercial et au moins une catégorie sont **obligatoires**. Le **profil pro initial** est créé en même temps (s'il échoue, le compte est supprimé pour ne pas laisser de prestataire sans profil) |
| **AUTH-04** Connexion | `POST /api/auth/login` → access + refresh token. Le message d'erreur est **identique** que l'email existe ou non, pour ne pas révéler quels emails sont inscrits. Compte suspendu → 403 |
| **AUTH-05** JWT + refresh | Signature HMAC-SHA256. Access token de **15 min**, refresh token de **7 jours**. Le claim `type` empêche d'utiliser un refresh token comme access token (et inversement). Durées et clé configurables (`JWT_SECRET`…) |
| **AUTH-06** Filtre JWT | [`JwtAuthenticationFilter`](../backend/src/main/java/com/testhakni/backend/security/JwtAuthenticationFilter.java) vérifie le token **et** que le compte existe toujours et est actif : un compte suspendu perd l'accès immédiatement, même avec un token encore valide |
| **AUTH-07** Mots de passe | **BCrypt** (hash à sens unique avec sel aléatoire). Politique : 8 à 72 caractères, avec une majuscule, une minuscule, un chiffre et un caractère spécial. Elle s'applique à l'inscription **et** au changement de mot de passe, qui exige aussi l'ancien mot de passe |
| **AUTH-08** Rôles | Matrice d'autorisation centralisée dans [`SecurityConfig`](../backend/src/main/java/com/testhakni/backend/config/SecurityConfig.java). Non connecté → **401**, mauvais rôle → **403**, toujours au format JSON |
| **AUTH-09** Profil utilisateur | `GET/PUT /api/users/me` et `PUT /api/users/me/password`. Seuls prénom, nom, téléphone et ville sont modifiables |

**Matrice des droits**

| Route | Public | Client | Prestataire |
|---|:-:|:-:|:-:|
| `/api/auth/**`, `GET /api/services/**`, `GET /api/categories`, `GET /api/providers/{id}` | ✅ | ✅ | ✅ |
| `/api/users/me` | ❌ 401 | ✅ | ✅ |
| `/api/providers/me/**`, `GET /api/services/mine`, `POST/PUT/PATCH/DELETE /api/services/**` | ❌ 401 | ❌ 403 | ✅ |
| `/api/requests/**` | ❌ 401 | ✅ | ❌ 403 |

> **Pourquoi deux tokens ?** Un access token court limite les dégâts s'il est volé. Le refresh token évite de redemander le mot de passe toutes les 15 minutes. Côté Angular, le renouvellement est **automatique** et invisible pour l'utilisateur (voir FE-03).

### 4.2 Sécurité (SEC-01, SEC-02)
- **SEC-01 — Validation et erreurs** : chaque DTO porte ses règles (`@NotBlank`, `@Size`, `@Email`, `@Pattern`, `@FutureOrPresent`…). [`GlobalExceptionHandler`](../backend/src/main/java/com/testhakni/backend/exception/GlobalExceptionHandler.java) transforme **toute** erreur en un JSON unique :
  ```json
  { "status": 400, "error": "Bad Request", "message": "Donnees invalides",
    "path": "/api/requests", "fieldErrors": { "title": "Le titre doit faire entre 3 et 100 caracteres" } }
  ```
  Aucune *stack trace* ni aucun détail interne n'est renvoyé au client. Les erreurs inattendues sont journalisées côté serveur et renvoyées comme un simple « Erreur interne ».
- **SEC-02 — Propriétaire des ressources** : modifier, supprimer ou désactiver un service, ou consulter ou annuler une demande, passe par une méthode `loadOwned()`. Elle renvoie **404** si la ressource n'existe pas et **403** si elle appartient à quelqu'un d'autre. Le profil pro est toujours celui du compte connecté (`/me`) : il est impossible de viser celui d'un autre.

### 4.3 Profil prestataire (PROV-01)
- `GET/PUT /api/providers/me/profile` : la modification **crée le profil s'il n'existe pas** (*upsert*).
- `GET /api/providers/{id}` : profil **public** avec les services actifs. Il exclut volontairement l'email, le téléphone et le statut du compte, et affiche le nom sous la forme « Prénom N. ».

### 4.4 Services (SERV-01 → SERV-05)
| Item | Détail |
|---|---|
| SERV-01 Modèle | Titre, description, catégorie, ville, prix, type de prix (`FIXED`, `HOURLY`, `ON_QUOTE`), statut (`ACTIVE`, `INACTIVE`, `SUSPENDED`) |
| SERV-02 Créer | `POST /api/services`. Règle : **un prix est obligatoire sauf pour un service « sur devis »** (règle sur plusieurs champs, `@AssertTrue`) |
| SERV-03 Modifier / activer / désactiver | `PUT /api/services/{id}` et `PATCH /api/services/{id}/status`. Le prestataire ne peut **pas** lever une suspension posée par un admin (préparé pour la modération du Sprint 3) |
| SERV-04 Supprimer | `DELETE /api/services/{id}` → 204 |
| SERV-05 Catégories | Énumération de 10 catégories avec un libellé français, exposée par `GET /api/categories` |

### 4.5 Recherche (SEARCH-01 → SEARCH-04)
`GET /api/services` renvoie **uniquement les services actifs**, avec pagination (`page`, `size` ≤ 50, `totalElements`, `totalPages`…).

| Filtre | Comportement |
|---|---|
| `q` | Mot-clé dans le titre **ou** la description, insensible à la casse |
| `category` | Catégorie exacte. Une valeur inconnue → 400 |
| `city` | Recherche partielle, insensible à la casse (« tun » trouve « Tunis ») |
| `minPrice`, `maxPrice` | Bornes incluses. Une valeur négative ou `min > max` → 400 |
| `sort` | `NEWEST` (par défaut), `PRICE_ASC`, `PRICE_DESC` |

Tous les filtres se combinent. Le texte saisi est **échappé** (`Pattern.quote`) avant d'être transformé en expression régulière MongoDB : un utilisateur ne peut donc pas injecter de regex. Les profils des prestataires sont chargés **en une seule requête** pour toute la page de résultats, au lieu d'une requête par service (problème dit « N+1 »).

- SEARCH-03 : `GET /api/services/{id}`. Un service inactif renvoie 404 au public, mais reste visible par son propriétaire.
- SEARCH-04 : profil public du prestataire (voir 4.3).

### 4.6 Demandes (REQ-01, REQ-02)
- Modèle `ServiceRequest` avec un cycle de vie `OPEN → IN_PROGRESS → COMPLETED` ou `OPEN → CANCELLED` (les transitions vers « en cours » arriveront avec les offres au Sprint 3).
- `POST /api/requests` : la demande est liée au client connecté. Si elle part d'un service, celui-ci doit exister et être actif. La date souhaitée ne peut pas être dans le passé.
- `GET /api/requests/mine?status=` : **uniquement les demandes du client connecté**.
- `GET /api/requests/{id}` et `PATCH /api/requests/{id}/cancel` : seul le propriétaire y a accès. Seule une demande ouverte peut être annulée (sinon 409).

### 4.7 Frontend (FE-02 → FE-06)

| Item | Pages / éléments |
|---|---|
| **FE-02** Auth | Connexion (avec retour vers la page demandée via `returnUrl`), choix du type de compte, inscription client, inscription prestataire avec cases à cocher pour les catégories |
| **FE-03** Sécurité UI | [`AuthService`](../frontend/src/app/core/services/auth.service.ts) (signals + localStorage), [intercepteur JWT](../frontend/src/app/core/interceptors/auth.interceptor.ts) qui ajoute le token et, sur un 401, **rafraîchit une seule fois puis rejoue la requête** (si le refresh échoue : déconnexion). [Guards](../frontend/src/app/core/guards/auth.guards.ts) `authGuard`, `roleGuard('PROVIDER')` et `guestGuard`. Bouton de déconnexion |
| **FE-04** Profils | « Mon compte » (informations et mot de passe), « Profil professionnel » pré-rempli |
| **FE-05** Mes services | Liste avec badges de statut, activer / désactiver, modifier, supprimer (avec confirmation). Formulaire création / modification dont le champ prix se désactive pour « sur devis » |
| **FE-06** Catalogue et demandes | Recherche avec **filtres dans l'URL** (lien partageable, bouton « précédent » fonctionnel) et pagination. Détail d'un service avec « Faire une demande » qui **pré-remplit** le formulaire. Profil public du prestataire. Mes demandes (onglets par statut, annulation). Nouvelle demande |

**Points de qualité côté interface**
- Chaque page gère les états **chargement, vide et erreur**.
- Les erreurs de validation renvoyées par le backend (`fieldErrors`) s'affichent **sous le bon champ** du formulaire ([`applyServerErrors`](../frontend/src/app/core/api-error.ts)).
- La politique de mot de passe est identique côté Angular et côté Java : l'utilisateur est prévenu avant même d'envoyer le formulaire.

### 4.8 Tests (TEST-01a)

**Backend — 40 tests**
| Classe | Type | Ce qui est vérifié |
|---|---|---|
| `AuthControllerTest` (11) | API | Inscription et hash BCrypt, email en double, mot de passe faible, champs prestataire obligatoires, claims du JWT, mauvais identifiants, refresh (et refus d'un access token comme refresh), 401 sans token ou avec un faux token, champs protégés de `/me`, changement de mot de passe, 403 d'un client sur les routes prestataire |
| `ServiceControllerTest` (7) | API | Création, validation détaillée, **un prestataire ne peut pas modifier ni supprimer le service d'un autre**, service inactif invisible du public, recherche (mot-clé, catégorie + ville, prix + tri, pagination, résultat vide), filtres invalides, profil public sans email ni téléphone |
| `RequestControllerTest` (5) | API | Création liée au client, accès réservé aux clients, validation (date passée, budget négatif…), **un client ne voit pas les demandes d'un autre**, annulation (puis 409 si on recommence), service inexistant |
| `AuthServiceTest` (5) | Unitaire (Mockito) | Normalisation de l'email, conflit, mauvais mot de passe, compte suspendu, refresh invalide |
| `JwtServiceTest` (4) | Unitaire | Claims, type de token, **token expiré**, **token signé avec une autre clé** |
| `ServiceOfferingServiceTest` (4) | Unitaire | 403 / 404 sur la propriété, suspension admin non levable |
| `HealthControllerTest` (3) + `BackendApplicationTests` (1) | API | Santé, catégories publiques, route inconnue protégée |

**Frontend — 24 tests**
`AuthService` (stockage des tokens, refresh, logout), intercepteur (ajout du token, pas de token vers d'autres domaines, refresh puis rejeu, déconnexion si le refresh échoue, 403 non rafraîchi), guards, page de login (formulaire invalide, redirection selon le rôle, message d'erreur de l'API), accueil, menu selon le rôle, pipe prix.

---

## 5. Problèmes rencontrés et solutions

| Problème | Cause | Solution |
|---|---|---|
| `java -version` affichait 1.8 | Java 8 est le Java par défaut du PC | Script `scripts/java21.ps1` (pour la fenêtre courante uniquement) |
| Le backend ne démarrait pas : « Port 8080 already in use » | **Oracle Database** (`TNSLSNR`) écoute sur 8080 | Port configurable (`PORT`) et proxy Angular configurable (`API_PORT`), documentés dans le README |
| Le port 27017 était déjà utilisé | Un MongoDB d'un autre projet (belleora) tournait déjà | Base dédiée `testhakni` (et `testhakni_test` pour les tests). Port du docker-compose configurable (`MONGO_PORT`) |
| Les filtres de prix ne pouvaient pas fonctionner | Spring Data stocke les `BigDecimal` en texte | Convertisseurs vers `Decimal128` |
| Risque de double exécution du filtre JWT | Un `@Component` filtre est aussi enregistré automatiquement comme filtre servlet | `FilterRegistrationBean` désactivé : le filtre ne tourne que dans la chaîne Spring Security |

---

## 6. Limites connues et suite (Sprint 3)

- **Refresh token sans état** : un refresh token reste valide jusqu'à son expiration, même après une déconnexion. Pour le révoquer, il faudrait le stocker en base (amélioration possible au Sprint 4, SEC-03/04).
- **Tri par prix** : les services « sur devis » (sans prix) apparaissent en tête avec « prix croissant ».
- **Recherche par regex** : simple et suffisante pour le volume du projet. Un index texte MongoDB serait plus performant avec beaucoup de données.
- Le rôle **ADMIN** existe déjà (compte de démo), mais ses écrans arrivent au Sprint 3 (ADMIN-01 → 05).
- Prochaines étapes du **Sprint 3** : REQ-03 (le prestataire voit les demandes pertinentes), offres, réservations, avis, notifications, administration. Le modèle a été préparé pour ça : statut `IN_PROGRESS` des demandes, statut `SUSPENDED` des services, profil pro avec catégories et ville pour le filtrage.

---

## 7. Comment vérifier soi-même

```powershell
docker compose up -d                                   # MongoDB
. .\scripts\java21.ps1; cd backend; .\mvnw.cmd test    # 40 tests
.\mvnw.cmd spring-boot:run                             # API + seed
cd ..\frontend; npm install; npm start                 # http://localhost:4200
npx ng test --watch=false --browsers=ChromeHeadless    # 24 tests
```
Connecte-toi ensuite avec `client@testhakni.dev` ou `plombier@testhakni.dev` (mot de passe `Password123!`) et refais les parcours de la section 1.
