# Livraison des sprints 3 et 4 — Testhakni

Branche `feature/sprints-3-4-completion`, base `5c2fd37`. Vérifications locales du 18 novembre 2026. Le parcours navigateur fonctionne sur une pile Docker isolée, avec données persistantes et quatre sessions indépendantes. Le déploiement public (#79) reste ouvert. La revue humaine relève de la pull request.

Les numéros ci-dessous suivent le plan de sprint et les issues GitHub réelles. Les tickets #95 à #101 correspondent aux lignes « NOUVEAU » du Sprint 3. Les anciens identifiants du backlog et les paiements de la roadmap ne sont pas utilisés pour numéroter cette livraison.

## Sprint 3

| Issue | Fonction vérifiée | Implémentation et preuve |
|---|---|---|
| #40 REQ-03 | Demandes ouvertes compatibles, sans adresse ni identifiant client | OfferService, OpenRequest, tests de confidentialité et d’éligibilité |
| #41 OFFER-01 | Proposition chiffrée, datée et liée au prestataire | Offer, OfferRepository, index composé, scénario navigateur |
| #42 OFFER-02 | Comparaison des offres de sa demande | OfferService.received, OffersPage, deux prestataires en démo |
| #43 OFFER-03 | Acceptation exclusive | OfferService.accept, tests concurrence et rollback |
| #44 OFFER-04 | Attente, retrait, rejet des concurrentes, annulation | OfferService, ServiceRequestService, test d’annulation |
| #45 BOOK-01 | Une réservation par offre acceptée | Booking, index uniques demande et offre, test concurrent |
| #46 BOOK-02 | Réservations du client, détail privé | BookingService, BookingsPage, navigateur et MockMvc |
| #47 BOOK-03 | Réservations affectées au prestataire | BookingService, BookingsPage, test d’accès par tiers |
| #48 BOOK-04 | Transitions ordonnées, historique daté avec acteur | BookingService.transition, MarketplaceWorkflowTest |
| #49 BOOK-05 | Confirmation par le client, clôture de la demande | BookingService, confirmation précoce refusée en test |
| #50 REV-01 | Note et commentaire après confirmation | BookingService.review, formulaire et avis publics en démo |
| #51 REV-02 | Refus d’avis prématuré, doublon ou note invalide | Index bookingId, Feedback, MarketplaceWorkflowTest |
| #52 REV-03 | Masquage d’un avis, moyenne et visibilité recalculées | AdminService, ReviewRepository, tests API et navigateur |
| #56 NOTIF-01 | Document persistant par destinataire | Notification, NotificationRepository, test de persistance |
| #57 NOTIF-02 | Événements offre, réservation, avis, modération | NotificationService, participants vérifiés dans le navigateur |
| #58 NOTIF-03 | Liste paginée, total non lu, lecture propriétaire | MarketplaceController, test de lecture inter-comptes refusée |
| #59 ADMIN-01 | ADMIN existant et contrôle côté API | SecurityConfig, refus 401/403 et connexion admin en démo |
| #60 ADMIN-02 | Consultation, activation et suspension | AdminService.account, navigateur et jeton suspendu refusé |
| #61 ADMIN-03 | Modération d’un service et motif audité | AdminService.moderate, masquage/restauration au navigateur |
| #62 ADMIN-04 | Dépôt d’un signalement et traitement admin | Report, AdminService.resolve, scénario multi-rôles |
| #63 ADMIN-05 | Nombre de comptes, services, demandes, offres, réservations, avis et signalements ouverts | AdminService.dashboard, AdminPage, scénario navigateur |
| #33 SERV-06 | Même opération de modération que #61 | AdminController, AdminService, test de suspension de service |
| #95 FE-07 | Découverte et formulaire d’offre | OpenRequests, OffersPage, test Angular et navigateur |
| #96 FE-08 | Comparaison et acceptation | OffersPage, deux propositions réelles en démo |
| #97 FE-09 | Réservations des deux participants | BookingsPage, test Angular d’accès refusé et navigateur |
| #98 FE-10 | Formulaire, avis et moyenne publics | BookingsPage, ReviewsPanel, catalogue, profil public |
| #99 FE-11 | Cloche, compteur et lecture | NotificationBell, NotificationsPage, tests Angular et navigateur |
| #100 FE-12 | Dashboard, comptes, services, avis, signalements | AdminPage, routes protégées, démo admin |
| #101 TEST-01b | Tests du domaine et de ses limites | MarketplaceServicesTest et MarketplaceWorkflowTest |

## Sprint 4

| Issue | État | Preuve ou limite |
|---|---|---|
| #66 SEC-03 | Sans objet pour les fichiers | Aucun modèle, écran, endpoint ou parcours produit ne consomme de fichier. Multipart désactivé. Un corps binaire soumis au catalogue retourne 415 et ne crée aucun service. |
| #67 SEC-04 | Vérifié | Journaux de connexion, refus, décisions admin. Audit persistant avec cible, auteur, date et motif. Contrôle des valeurs secrètes dans les logs de la pile de production locale. |
| #68 TEST-01 | Vérifié | JUnit/Mockito, AuthServiceTest, ServiceOfferingServiceTest, JwtServiceTest, MarketplaceServicesTest |
| #69 TEST-02 | Vérifié | MockMvc avec contexte Spring et MongoDB réel, anciennes et nouvelles routes |
| #70 TEST-03 | Vérifié | JWT, rôles, propriété, données privées, suspension, contraintes, refus et conflits |
| #71 TEST-04 | Vérifié | Intégration transactions/rollback/concurrence et navigateur avec Nginx, API et Mongo |
| #72 DOC-01 | Vérifié | `/v3/api-docs` version 0.4.0, 43 chemins, Swagger UI HTTP 200 |
| #73 DOC-02 | Vérifié localement | Trois README, génération de configuration privée, installation et dépannage dans le guide d’exploitation |
| #74 DOC-03 | Vérifié localement | Guide par rôle, scénario parcouru dans le navigateur |
| #75 DOC-04 | Livré | Résultats, sécurité, limites et commandes dans ce rapport |
| #76 DEP-01 | Vérifié | Image Java 21 multi-étapes, API démarrée en profils démo et production |
| #77 DEP-02 | Vérifié | Build Angular et image Nginx non privilégiée, proxy et rechargement direct des routes |
| #78 DEP-03 | Vérifié | Deux configs Compose valides, replica set authentifié, trois services sains, persistance vérifiée |
| #79 DEP-04 | **Ouvert pour l’hébergement public** | Pile de production validée localement. Aucun hôte cible, domaine ni accès d’exploitation externe fourni. |
| #80 DEP-05 | Vérifié localement | scripts/demo_e2e.py, Chrome/Chromium, deux offres concurrentes et trois rôles |
| #81 DEP-06 | Vérifié localement | Comptes seedés, scénario guidé et démo automatisée dans le guide |

## Architecture, API et routes Angular

L’acceptation, la clôture des offres, la réservation et leurs notifications sont transactionnelles sur un replica set. La soumission modifie la demande dans la transaction pour sérialiser les propositions avec la décision. Le prix est stocké en Decimal128. Les index uniques imposent l’unicité même en présence de requêtes concurrentes. La réservation conserve les références et conditions convenues. Les notes visibles sont agrégées par prestataire. La configuration de production utilise un compte Mongo applicatif, un secret JWT externe et exclut le seed.

L’[inventaire API](../backend/README.md#api-du-parcours-de-réservation) donne chaque méthode, chemin, rôle et entrée pour les demandes compatibles, offres, réservations, avis, notifications, signalements et opérations admin. L’API existante d’annulation ferme désormais les propositions. Le profil public gagne le nombre d’avis et la note moyenne. Les erreurs suivent le format `ApiError`.

Nouvelles pages Angular : `/provider/requests`, `/provider/offers`, `/requests/:id/offers`, `/bookings`, `/bookings/:id`, `/notifications`, `/admin/dashboard`, `/admin/users`, `/admin/services`, `/admin/reviews` et `/admin/reports`. Les routes publiques `/providers/:id` et `/services/:id` affichent les avis. Les formulaires d’offre, avis et signalement sont intégrés aux pages concernées.

## Vérifications exécutées

| Vérification | Résultat observé |
|---|---|
| `cd backend && ./mvnw -B verify` avec Java 21 et MongoDB 7 | **52 tests réussis**, aucun échec, erreur ou test ignoré, JAR Spring Boot produit. La suite initiale de 40 tests a également réussi avant ajout des scénarios. |
| `cd frontend && npm ci && npm run build` | Installation depuis le lockfile et compilation Angular de production réussies. |
| `cd frontend && npx ng test --watch=false --browsers=ChromeHeadless` | **27 tests réussis**, Chrome Headless 158. |
| Configurations Compose, build des images et `up -d --wait` | Config de dev et de pile valides. Les trois services ont atteint l’état healthy. MongoDB 7 utilisé pour les validations isolées. |
| Test de migration de développement | Passage d’un serveur autonome avec une demande synthétique Sprint 2 au replica set `rs0` sur le même volume jetable. Demande relue après élection du primaire. |
| Pile de production locale | Santé HTTP 200, profil prod sans compte seedé, deep link Angular HTTP 200, dashboard anonyme 401. Aucun des secrets générés trouvé dans les logs des trois conteneurs inspectés. |
| Parcours navigateur | Inscription et connexion client et prestataires, profil, publication, recherche, demande, deux offres, acceptation, réservation unique, suivi, confirmation, avis visible puis masqué, notifications, signalement, administration, suspension/réactivation et refus d’accès. |
| Rendu mobile | Huit routes authentifiées, réparties sur les trois rôles, testées sur une largeur de 390 pixels sans débordement horizontal. |
| Recréation des trois conteneurs | Les quatre réservations, quatre avis, 36 notifications et huit décisions d’audit issus des essais successifs sont restés présents sans modification des compteurs. |

Les tests Maven ont utilisé `MONGODB_TEST_URI=mongodb://localhost:27183/testhakni_test?replicaSet=rs0`. Le nettoyage vérifie le nom réel de la base avant toute suppression. Le navigateur a utilisé une autre pile dédiée avec sa propre base `testhakni_test`. Aucune donnée du volume de développement existant n’a servi au nettoyage. Les corrections validées pendant les essais incluent la précision des dates au rejeu, l’initialisation du replica set, le header Host transmis par Nginx et la soumission du formulaire de signalement.

## Rejouer la livraison

Depuis la racine, générer une configuration isolée et démarrer la démonstration avec les commandes suivantes. Le guide détaille les prérequis, la configuration, les rôles, le déploiement et le rollback. [Guide d’exploitation et de démonstration](EXPLOITATION.md)

```sh
DEMO_ENV="$(mktemp -d)/demo.env"
python3 scripts/stack_env.py "$DEMO_ENV" --demo
export MONGO_IMAGE=mongo:7.0
docker compose --env-file "$DEMO_ENV" -p testhakni-demo -f docker-compose.stack.yml config --quiet
docker compose --env-file "$DEMO_ENV" -p testhakni-demo -f docker-compose.stack.yml build
docker compose --env-file "$DEMO_ENV" -p testhakni-demo -f docker-compose.stack.yml up -d --wait
curl -fsS http://127.0.0.1:8088/api/health
python3 -m venv /tmp/testhakni-browser-tools
/tmp/testhakni-browser-tools/bin/pip install playwright==1.56.0
set -a
. "$DEMO_ENV"
set +a
/tmp/testhakni-browser-tools/bin/python scripts/demo_e2e.py
```

Pour les tests Maven et Angular, utiliser un replica set de développement et sa base dédiée, puis exécuter les commandes suivantes.

```sh
docker compose config --quiet
docker compose up -d --wait
(cd backend && MONGODB_TEST_URI='mongodb://localhost:27017/testhakni_test?replicaSet=rs0' ./mvnw -B verify)
(cd frontend && npm ci && npm run build && npx ng test --watch=false --browsers=ChromeHeadless)
```
