<#
  Reorganise le backlog GitHub en 4 sprints de 2 semaines (voir docs/SPRINT_PLAN.md).
  - cree les milestones "Sprint 1" a "Sprint 4"
  - range les 62 issues existantes dans leur sprint (par numero)
  - cree les items manquants et les ajoute au board
  - rouvre #2 et #3 (marquees Done alors qu'il n'y a pas encore de code)

  Simulation :  powershell -ExecutionPolicy Bypass -File scripts\reorganiser-sprints.ps1 -DryRun
  Pour de vrai: powershell -ExecutionPolicy Bypass -File scripts\reorganiser-sprints.ps1
#>
param(
  [switch]$DryRun,
  [string]$Repo = "Mouadh-Radhoini/testhakni",
  [string]$ProjectOwner = "Mouadh-Radhoini",
  [int]$ProjectNumber = 5
)
$ErrorActionPreference = "Stop"

$Sprints = @{
  1 = @{ title = "Sprint 1 - Setup et fondations (S1-S2)";                desc = "Backend, frontend, MongoDB et CI qui demarrent" }
  2 = @{ title = "Sprint 2 - Auth, services, recherche, demandes (S3-S4)"; desc = "Inscription, login, services, recherche, demandes" }
  3 = @{ title = "Sprint 3 - Offres, reservations, avis, admin (S5-S6)";    desc = "Parcours demande -> offre -> reservation -> avis, admin" }
  4 = @{ title = "Sprint 4 - Securite, tests, docs, deploiement (S7-S8)";   desc = "Application testee, documentee et deployee" }
}

# Numero d'issue existante -> sprint
$Map = @{}
foreach ($n in 2,3)                          { $Map[$n] = 1 }
foreach ($n in (13..21) + (28..32) + (34..39) + 64,65) { $Map[$n] = 2 }
foreach ($n in (40..52) + (56..63) + 33)     { $Map[$n] = 3 }
foreach ($n in (66..81))                     { $Map[$n] = 4 }

$NewItems = @(
  @{ s=1; code="SET-03";   labels="devops,p0";            title="CI GitHub Actions (build + tests backend et frontend)"; body="Workflow GitHub Actions qui compile et lance les tests du backend et du frontend a chaque push et PR." },
  @{ s=1; code="SET-04";   labels="documentation,p0";     title="README d'installation, variables d'environnement et conventions Git"; body="Comment lancer backend, frontend et MongoDB en local, variables d'environnement, nommage des branches, regles de PR." },
  @{ s=1; code="DB-01";    labels="database,backend,p0";  title="Configurer MongoDB (docker-compose dev, application.yml, profils)"; body="MongoDB local via docker-compose, configuration Spring, profils dev/test, verification de la connexion." },
  @{ s=1; code="DB-02";    labels="database,p0";          title="Modele de donnees : collections, index et donnees de test"; body="Collections (users, services, categories, requests, offers, bookings, reviews, notifications, reports), index, script de seed." },
  @{ s=1; code="FE-01";    labels="frontend,p0";          title="Socle Angular : routing, layout, environnements, service HTTP"; body="Routing principal, layout (header/menu), fichiers environment, service HTTP de base, page 404." },
  @{ s=1; code="TEST-00";  labels="testing,p0";           title="Socle de tests backend et frontend"; body="JUnit + base Mongo de test cote backend, tests Angular configures, lances par la CI." },
  @{ s=2; code="PROV-01";  labels="backend,p0";           title="Profil professionnel du prestataire (creation / modification)"; body="Le prestataire renseigne et modifie son profil pro (metier, zone, description, experience). Necessaire pour SEARCH-04." },
  @{ s=2; code="FE-02";    labels="frontend,authentication,p0"; title="Pages Login / Inscription client / Inscription prestataire"; body="Formulaires avec validation, appel des API AUTH-02, AUTH-03, AUTH-04, messages d'erreur." },
  @{ s=2; code="FE-03";    labels="frontend,security,p0"; title="Intercepteur JWT, guards par role, logout et refresh"; body="Token ajoute aux requetes, routes protegees (connecte + role), deconnexion, renouvellement du token." },
  @{ s=2; code="FE-04";    labels="frontend,p0";          title="Pages profil utilisateur et profil prestataire"; body="Edition du profil (AUTH-09), profil pro (PROV-01), page publique du prestataire (SEARCH-04)." },
  @{ s=2; code="FE-05";    labels="frontend,services,p0"; title="Pages 'Mes services' du prestataire"; body="Lister, creer, modifier et supprimer ses services (SERV-02 a SERV-05)." },
  @{ s=2; code="FE-06";    labels="frontend,p0";          title="Pages recherche, detail service, creation et liste des demandes"; body="Recherche et filtres (SEARCH-01/02), detail (SEARCH-03), creer et lister ses demandes (REQ-01/02)." },
  @{ s=2; code="TEST-01a"; labels="testing,backend,p0";   title="Tests unitaires : auth, services, demandes"; body="Tests des services du sprint 2." },
  @{ s=3; code="FE-07";    labels="frontend,p0";          title="Prestataire : demandes ouvertes et formulaire d'offre"; body="REQ-03 et OFFER-01 cote interface." },
  @{ s=3; code="FE-08";    labels="frontend,p0";          title="Client : comparer et accepter une offre"; body="OFFER-02 et OFFER-03 cote interface." },
  @{ s=3; code="FE-09";    labels="frontend,p0";          title="Pages reservations client et prestataire"; body="BOOK-02 a BOOK-05 cote interface : liste, statuts, confirmation de fin." },
  @{ s=3; code="FE-10";    labels="frontend,p0";          title="Formulaire d'avis et affichage des avis"; body="REV-01 cote interface, avis visibles sur le profil et le service." },
  @{ s=3; code="FE-11";    labels="frontend,p1";          title="Cloche de notifications"; body="Liste, compteur non lues, marquer comme lu (NOTIF-03)." },
  @{ s=3; code="FE-12";    labels="frontend,p1";          title="Pages admin : utilisateurs, moderation, signalements, tableau de bord"; body="ADMIN-01 a ADMIN-05 et REV-03 cote interface." },
  @{ s=3; code="TEST-01b"; labels="testing,backend,p0";   title="Tests unitaires : offres, reservations, avis, notifications"; body="Tests des services du sprint 3." }
)

function Run([string]$what, [scriptblock]$action) {
  if ($DryRun) { Write-Host "[simulation] $what" -ForegroundColor Yellow }
  else { Write-Host $what; & $action }
}

# 1. Milestones
$existingMs = gh api "repos/$Repo/milestones?state=all&per_page=100" | ConvertFrom-Json
foreach ($n in 1..4) {
  $t = $Sprints[$n].title
  if (-not ($existingMs | Where-Object { $_.title -eq $t })) {
    Run "Creer milestone '$t'" { gh api "repos/$Repo/milestones" -f title="$t" -f description="$($Sprints[$n].desc)" | Out-Null }
  }
}

# 2. Labels manquants
$existingLabels = (gh label list -R $Repo --limit 200 --json name | ConvertFrom-Json).name
$wanted = $NewItems | ForEach-Object { $_.labels -split ',' } | Sort-Object -Unique
foreach ($l in $wanted) {
  if ($existingLabels -notcontains $l) { Run "Creer label '$l'" { gh label create $l -R $Repo | Out-Null } }
}

# 3. Ranger les issues existantes
$issues = gh issue list -R $Repo --state all --limit 300 --json number,title,state | ConvertFrom-Json
$titles = $issues | ForEach-Object { $_.title }
foreach ($i in ($issues | Sort-Object number)) {
  if ($Map.ContainsKey([int]$i.number)) {
    $s = $Map[[int]$i.number]
    Run "#$($i.number) -> Sprint $s" { gh issue edit $i.number -R $Repo --milestone "$($Sprints[$s].title)" | Out-Null }
  } else {
    Write-Host "  #$($i.number) non range (inconnu du plan) : $($i.title)" -ForegroundColor Cyan
  }
}

# 4. Rouvrir #2 et #3
foreach ($n in 2,3) {
  $st = ($issues | Where-Object { $_.number -eq $n }).state
  if ($st -eq "CLOSED") { Run "Rouvrir #$n" { gh issue reopen $n -R $Repo | Out-Null } }
}

# 5. Creer les items manquants
foreach ($it in $NewItems) {
  $fullTitle = "$($it.code) - $($it.title)"
  if ($titles | Where-Object { $_ -like "$($it.code) *" }) { Write-Host "  deja present : $($it.code)"; continue }
  Run "Creer '$fullTitle' (Sprint $($it.s))" {
    $url = gh issue create -R $Repo --title "$fullTitle" --body "$($it.body)" --label "$($it.labels)" --milestone "$($Sprints[$it.s].title)"
    try { gh project item-add $ProjectNumber --owner $ProjectOwner --url $url | Out-Null }
    catch { Write-Warning "Pas ajoute au board : $url" }
  }
}

Write-Host "`nTermine." -ForegroundColor Green
