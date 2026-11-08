# Local Services Marketplace (testhakni)

Plateforme qui met en relation des **clients** et des **prestataires de services locaux** (plomberie, ménage, jardinage…).
Le client cherche un service et publie une demande ; le prestataire publie ses services et gère son profil professionnel.

| Partie | Technologie | Dossier |
|---|---|---|
| API REST | Spring Boot 3.5, Java 21, Spring Security + JWT | [`backend/`](backend/README.md) |
| Interface web | Angular 20 (standalone, signals) | [`frontend/`](frontend/README.md) |
| Base de données | MongoDB 8 | `docker-compose.yml` |
| CI | GitHub Actions | `.github/workflows/ci.yml` |

Documentation projet : [`docs/`](docs). Le plan des sprints est dans [SPRINT_PLAN.md](docs/SPRINT_PLAN.md).

## Démarrage rapide (Windows / PowerShell)

Prérequis : **JDK 21**, **Node.js 22+**, **Docker Desktop** (ou un MongoDB local).

```powershell
# 1. MongoDB
docker compose up -d

# 2. Backend (nouvelle fenêtre PowerShell)
. .\scripts\java21.ps1          # active Java 21 si Java 8 est le défaut du PC
cd backend
.\mvnw.cmd spring-boot:run      # http://localhost:8080/swagger-ui.html

# 3. Frontend (autre fenêtre)
cd frontend
npm install
npm start                       # http://localhost:4200
```

Au premier démarrage, le profil `dev` insère des **données de démonstration** (mot de passe `Password123!`) :

| Compte | Rôle |
|---|---|
| `client@testhakni.dev` | Client (a déjà une demande) |
| `plombier@testhakni.dev` | Prestataire (3 services) |
| `menage@testhakni.dev` | Prestataire (3 services) |
| `admin@testhakni.dev` | Admin (écrans admin au Sprint 3) |

### Port 8080 déjà occupé ?
Sur certains PC, Oracle Database (processus `TNSLSNR`) utilise déjà le port 8080. Lance alors le backend sur un autre port et indique-le au frontend :
```powershell
$env:PORT = "8081"; .\mvnw.cmd spring-boot:run        # fenêtre backend
$env:API_PORT = "8081"; npm start                     # fenêtre frontend
```

## Tests
```powershell
cd backend;  .\mvnw.cmd test                                        # 40 tests JUnit (MongoDB requis)
cd frontend; npx ng test --watch=false --browsers=ChromeHeadless   # 24 tests Jasmine
```

## Conventions Git
- `main` reste stable : on n'y pousse jamais directement, tout passe par une **pull request** relue par l'autre membre.
- Une branche par item du backlog : `feature/<ID>-description` (ex. `feature/AUTH-04-login`), `fix/<ID>-...` pour une correction.
- Messages de commit : `<ID> : ce qui change` (ex. `SERV-02 : CRUD des services du prestataire`).
- La CI doit être verte avant le merge.
- Ne jamais committer de secret : les variables sensibles (`JWT_SECRET`, `MONGODB_URI`…) passent par l'environnement.
