# Frontend — Local Services Marketplace

Application Angular 20 : composants standalone, signals, formulaires réactifs, chargement des pages à la demande (lazy loading).

## Prérequis
- Node.js 20.19+, 22+ ou 24 (`node -v`)
- Le backend lancé (voir `../backend/README.md`)

## Lancer
```bash
cd frontend
npm install
npm start          # http://localhost:4200
```
En dev, les appels `/api/**` sont **redirigés vers le backend par un proxy** ([proxy.conf.js](proxy.conf.js)), donc pas de souci de CORS.
Si le backend ne tourne pas sur 8080 : `$env:API_PORT = "8081"; npm start`.

## Tests
```bash
npm test                                                # mode watch
npx ng test --watch=false --browsers=ChromeHeadless     # une seule exécution (comme la CI)
```

## Pages
| URL | Accès | Contenu |
|---|---|---|
| `/` | public | Accueil, recherche rapide, état de l'API |
| `/login`, `/register`, `/register/customer`, `/register/provider` | non connecté | Connexion et inscriptions |
| `/services` | public | Recherche avec filtres dans l'URL et pagination |
| `/services/:id` | public | Détail du service, bouton « Faire une demande » |
| `/providers/:id` | public | Profil public du prestataire et ses services |
| `/account` | connecté | Mon compte et changement de mot de passe |
| `/provider/profile` | prestataire | Profil professionnel |
| `/provider/services`, `/provider/services/new`, `/provider/services/:id/edit` | prestataire | Gestion de mes services |
| `/requests`, `/requests/new` | client | Mes demandes et nouvelle demande |
| `/forbidden`, `**` | — | Accès refusé / 404 |

## Organisation du code
```
src/app
├── core/
│   ├── models.ts            types alignés sur les DTO du backend
│   ├── api-error.ts         messages d'erreur, erreurs serveur → champs du formulaire
│   ├── services/            appels HTTP (auth, catalog, provider, request, account)
│   ├── interceptors/        intercepteur JWT (token + refresh automatique sur 401)
│   └── guards/              authGuard, roleGuard('PROVIDER'), guestGuard
├── shared/                  composants réutilisables (carte de service, pipe prix)
└── features/                une page par fichier : auth, account, catalog, provider, requests, errors, home
```
