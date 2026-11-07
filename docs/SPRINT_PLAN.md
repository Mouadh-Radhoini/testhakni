# Plan des sprints — Local Services Marketplace

4 sprints de **2 semaines** (1 sprint = 2 anciennes semaines). Basé sur les 62 issues GitHub réelles.
Les items **NOUVEAU** manquaient : base de données, CI, profil prestataire, pages Angular et tests à chaque sprint.
Chaque item respecte la Definition of Done : code + validation + tests + Swagger (si API) + revue de code.

---

## Sprint 1 — Setup & fondations (semaines 1–2)
Objectif : backend, frontend et MongoDB démarrent, la CI tourne.

| Issue | Item |
|---|---|
| #2 | SET-01 — Initialiser le backend Spring Boot |
| #3 | SET-02 — Initialiser le frontend Angular |
| NOUVEAU | SET-03 — CI GitHub Actions (build + tests backend et frontend) |
| NOUVEAU | SET-04 — README d'installation, variables d'environnement, conventions Git |
| NOUVEAU | DB-01 — Configurer MongoDB (docker-compose de dev, application.yml, profils dev/test) |
| NOUVEAU | DB-02 — Modèle de données : collections, index, données de test (seed) |
| NOUVEAU | FE-01 — Socle Angular : routing, layout, environnements, service HTTP, page 404 |
| NOUVEAU | TEST-00 — Socle de tests backend (JUnit + Mongo de test) et frontend |

## Sprint 2 — Auth, services, recherche, demandes (semaines 3–4)
Objectif : client et prestataire s'inscrivent et se connectent ; le prestataire publie ses services ; le client cherche et crée une demande.

| Issue | Item |
|---|---|
| #13 → #21 | AUTH-01 → AUTH-09 — User, inscriptions, login, JWT, hachage, rôles, profil |
| #64 | SEC-01 — Validation des requêtes |
| #65 | SEC-02 — Vérification du propriétaire des ressources |
| NOUVEAU | PROV-01 — Profil professionnel du prestataire (création / modification) |
| #28 → #32 | SERV-01 → SERV-05 — Modèle service, créer, modifier, supprimer, catégories |
| #34 → #37 | SEARCH-01 → SEARCH-04 — Recherche, filtres, détail service, profil prestataire |
| #38, #39 | REQ-01, REQ-02 — Le client crée et consulte ses demandes |
| NOUVEAU | FE-02 — Pages Login / Inscription client / Inscription prestataire |
| NOUVEAU | FE-03 — Intercepteur JWT, guards par rôle, logout, refresh |
| NOUVEAU | FE-04 — Pages profil utilisateur et profil prestataire |
| NOUVEAU | FE-05 — Pages « Mes services » du prestataire |
| NOUVEAU | FE-06 — Pages de recherche, détail service, création et liste des demandes |
| NOUVEAU | TEST-01a — Tests unitaires auth, services, demandes |

## Sprint 3 — Offres, réservations, avis, notifications, admin (semaines 5–6)
Objectif : le parcours demande → offre → réservation → fin → avis fonctionne, avec l'admin.

| Issue | Item |
|---|---|
| #40 | REQ-03 — Le prestataire voit les demandes pertinentes |
| #41 → #44 | OFFER-01 → OFFER-04 — Envoyer, voir, accepter une offre, statuts |
| #45 → #49 | BOOK-01 → BOOK-05 — Réservation, gestion client/prestataire, statuts, fin |
| #50 → #52 | REV-01 → REV-03 — Avis, avis invalides, modération |
| #56 → #58 | NOTIF-01 → NOTIF-03 — Notifications |
| #59 → #63 | ADMIN-01 → ADMIN-05 — Auth admin, utilisateurs, modération, signalements, tableau de bord |
| #33 | SERV-06 — Modération des services (doublon possible avec ADMIN-03 #61) |
| NOUVEAU | FE-07 — Prestataire : demandes ouvertes et formulaire d'offre |
| NOUVEAU | FE-08 — Client : comparer et accepter une offre |
| NOUVEAU | FE-09 — Pages réservations client et prestataire |
| NOUVEAU | FE-10 — Formulaire d'avis et affichage des avis |
| NOUVEAU | FE-11 — Cloche de notifications |
| NOUVEAU | FE-12 — Pages admin (utilisateurs, modération, signalements, tableau de bord) |
| NOUVEAU | TEST-01b — Tests unitaires offres, réservations, avis, notifications |

## Sprint 4 — Sécurité, tests, docs, Docker, déploiement (semaines 7–8)
Objectif : application testée, documentée, déployée et prête pour la démo.

| Issue | Item |
|---|---|
| #66, #67 | SEC-03, SEC-04 — Upload de fichiers sécurisé, logs de sécurité |
| #68 → #71 | TEST-01 → TEST-04 — Tests unitaires, API, sécurité, intégration |
| #72 → #75 | DOC-01 → DOC-04 — Swagger, guide d'installation, guide utilisateur, rapport tests/sécurité |
| #76 → #78 | DEP-01 → DEP-03 — Docker backend, frontend, docker-compose |
| #79 | DEP-04 — Déploiement |
| #80 | DEP-05 — Test final de bout en bout |
| #81 | DEP-06 — Démo finale |
