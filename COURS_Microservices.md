# Résumé de cours — Les microservices

Résumé synthétique, orienté débutant, avec les liens vers le projet
`employe` / `entreprise` que tu as réalisé.

---

## 1. Définition

Une **architecture microservices** consiste à découper une application en un
ensemble de **petits services indépendants**, chacun :

- responsable d'**une seule capacité métier** (ex. « gérer les employés ») ;
- avec son **propre code**, sa **propre base de données**, son **propre cycle de
  vie** (on peut le développer, tester, déployer, redémarrer seul) ;
- qui communique avec les autres **uniquement par le réseau** (HTTP/REST, gRPC,
  ou messages), jamais en partageant une base ou de la mémoire.

À l'opposé : le **monolithe**, une seule application qui contient tout.

---

## 2. Monolithe vs microservices

| Critère | Monolithe | Microservices |
|---|---|---|
| Déploiement | 1 bloc unique | chaque service séparément |
| Montée en charge | on duplique tout le bloc | on duplique seulement le service saturé |
| Panne | une erreur peut tout faire tomber | une panne reste (idéalement) isolée à un service |
| Équipes | tout le monde sur le même code | une équipe par service |
| Techno | une seule pile imposée | chaque service peut avoir la sienne |
| Complexité | simple au début | réseau, latence, cohérence des données, supervision |
| Transactions | faciles (une seule base) | difficiles (plusieurs bases) |

**À retenir :** les microservices ne sont *pas* « mieux » en soi. Ils échangent
de la **simplicité de code** contre de la **complexité opérationnelle**. On les
choisit quand l'application est grande, l'équipe nombreuse, les besoins de
scalabilité/déploiement fréquents.

---

## 3. Les concepts clés

### 3.1 Découpage par domaine (*bounded context*)
Chaque service correspond à un **sous-domaine métier** cohérent. Le bon découpage
suit le métier, pas la technique. Dans ton projet : un service « Employé », un
service « Entreprise ».

### 3.2 Une base de données par service (*database per service*)
Chaque service est **seul propriétaire** de ses données. Les autres ne lisent
jamais sa base directement : ils passent par son API. Ça garantit l'indépendance.
Dans ton projet : base H2 `employe` d'un côté, base H2 `entreprise` de l'autre ;
`entreprise` récupère les employés via un **appel REST**, pas via la base.

### 3.3 Couplage faible, cohésion forte
- **Cohésion forte** : tout ce qui concerne un domaine est dans le même service.
- **Couplage faible** : un service connaît le moins possible des autres (juste
  leur API). On peut alors changer l'intérieur d'un service sans casser les
  autres.

### 3.4 Contrat d'API
L'API publique d'un service (ses URL, ses formats JSON) est un **contrat**. Tant
qu'on le respecte, on peut tout réécrire derrière. On versionne l'API quand on
doit la casser (`/v1/...`, `/v2/...`).

### 3.5 Indépendance de déploiement
On peut livrer une nouvelle version d'`employe` sans toucher à `entreprise`.

---

## 4. La communication entre services

### 4.1 Synchrone (requête → réponse immédiate)
- **REST/HTTP** : le plus courant, simple, lisible (JSON). C'est ce que tu as
  fait : `entreprise` fait `GET http://localhost:8081/api/employes?...` sur
  `employe` et attend la réponse.
- **gRPC** : binaire, plus rapide, contrat strict (`.proto`). Pour de gros volumes
  ou de la communication interne intensive.

Inconvénient du synchrone : si le service appelé est lent ou éteint, l'appelant
est bloqué ou échoue (couplage temporel).

### 4.2 Asynchrone (messages / événements)
- Un service **publie un message** dans un **broker** (Kafka, RabbitMQ) ; d'autres
  services le consomment quand ils veulent.
- Avantage : les services ne dépendent plus d'être allumés en même temps
  (découplage temporel), meilleure résilience.
- Inconvénient : la donnée devient *cohérente à terme* (*eventual consistency*),
  plus difficile à raisonner.

### 4.3 Le style REST (rappel)
| Verbe HTTP | Sens | Exemple projet |
|---|---|---|
| `GET` | lire | `GET /api/employes` |
| `POST` | créer | `POST /api/employes` |
| `PUT` / `PATCH` | modifier | (non utilisé ici) |
| `DELETE` | supprimer | (non utilisé ici) |

Codes de réponse courants : `200 OK`, `201 Created`, `204 No Content`,
`400 Bad Request`, `404 Not Found`, `500 Internal Server Error`.

---

## 5. L'architecture interne d'un service (couches)

Un microservice reste une application classique bien rangée :

```
presentation   → API : reçoit les requêtes, renvoie le JSON (DTO), mappers. Pas de logique.
application    → métier : entités, services (règles), objets d'échange
infrastructure → accès externes : base de données (repository), appels aux autres services (client REST)
```

Objets à distinguer :
- **Entité** : image d'une table (persistée).
- **DTO** (*Data Transfer Object*) : ce qu'on expose dans l'API, séparé de l'entité.
- **DAO** (ici) : donnée reçue d'un autre service, non persistée.
- **Mapper** : convertit entité ⇄ DTO.
- **Repository** : parle à la base.

Bonnes pratiques appliquées dans ton projet :
- endpoints sans logique (ils délèguent au service) ;
- on ne renvoie jamais une entité brute, toujours un DTO ;
- injection des dépendances **par constructeur**.

---

## 6. Les patterns d'infrastructure (écosystème microservices)

Quand il y a beaucoup de services, on ajoute des briques transverses :

| Pattern | Problème résolu | Exemple d'outil |
|---|---|---|
| **API Gateway** | un point d'entrée unique pour les clients ; route vers le bon service, gère l'authentification | Spring Cloud Gateway, Kong, Nginx |
| **Service Discovery** | retrouver l'adresse d'un service qui peut changer / être dupliqué | Eureka, Consul, DNS Kubernetes |
| **Configuration centralisée** | gérer la config de tous les services au même endroit | Spring Cloud Config |
| **Load balancing** | répartir la charge entre plusieurs instances d'un service | côté client (Spring Cloud LoadBalancer) ou infra |
| **Circuit Breaker** | arrêter d'appeler un service en panne pour ne pas s'effondrer avec lui | Resilience4j |
| **Retry / Timeout / Fallback** | tolérer les erreurs réseau transitoires | Resilience4j |
| **Saga** | gérer une « transaction » qui traverse plusieurs services (avec compensations en cas d'échec) | orchestration ou chorégraphie d'événements |
| **API versioning** | faire évoluer une API sans casser les clients existants | `/v1`, `/v2` |

Dans ton projet (2 services simples) tu utilises la version minimale : appel REST
direct avec l'URL du service cible dans un fichier de configuration
(`employe.service.url=...`). C'est le point de départ ; Service Discovery + Gateway
seraient les évolutions naturelles.

---

## 7. Observabilité (surveiller un système distribué)

Avec plusieurs services, comprendre un bug demande des outils :

- **Logs centralisés** : rassembler les logs de tous les services (ELK/Loki).
- **Métriques** : nombre de requêtes, temps de réponse, taux d'erreur
  (Micrometer + Prometheus + Grafana).
- **Tracing distribué** : suivre **une même requête** qui traverse plusieurs
  services grâce à un identifiant commun (*trace id*) — OpenTelemetry, Zipkin,
  Jaeger.
- **Health checks** : chaque service expose un `/health` (Spring Boot Actuator)
  pour dire s'il va bien.

---

## 8. Déploiement

- **Conteneurs (Docker)** : on empaquette chaque service avec tout ce qu'il faut
  pour tourner. « Ça marche sur ma machine » devient « ça marche partout ».
- **Orchestration (Kubernetes)** : lance, surveille, redémarre, duplique les
  conteneurs automatiquement ; gère le réseau interne et la montée en charge.
- **CI/CD** : chaque service a son pipeline (build → tests → image → déploiement).
  C'est là qu'intervient un outil comme **Jenkins**.
- **12-Factor App** : ensemble de bonnes pratiques pour des applis prêtes au
  cloud (config par variables d'environnement, services externes attachés,
  processus sans état, logs vers la sortie standard, etc.).

---

## 9. Les difficultés (à connaître pour être honnête)

- **Réseau** : lent, faillible. Il faut gérer timeouts, retries, pannes.
- **Cohérence des données** : pas de transaction unique sur plusieurs bases →
  cohérence à terme, patterns Saga.
- **Tests** : tester l'ensemble demande des tests de contrat et d'intégration.
- **Débogage** : une requête traverse plusieurs services → tracing indispensable.
- **Coût opérationnel** : plus de déploiements, plus de supervision, plus d'infra.
- **Découpage raté** : si les services sont trop bavards entre eux, on obtient un
  « monolithe distribué » : tous les inconvénients, aucun avantage.

**Conseil classique :** commencer par un monolithe bien modularisé, puis extraire
des microservices quand le besoin est réel.

---

## 10. Lien direct avec ton projet

| Notion du cours | Où c'est dans le projet |
|---|---|
| Service autonome | `employe` (port 8081) et `entreprise` (port 9090), lancés séparément |
| Database per service | base H2 `employe` vs base H2 `entreprise`, jamais partagées |
| Communication synchrone REST | `EmployeClient` dans `entreprise` fait un `GET` HTTP vers `employe` |
| Contrat d'API | `GET /api/employes?idEmployes=1&idEmployes=3`, `GET /api/entreprises` |
| Découplage par configuration | `employe.service.url=http://localhost:8081/api` dans `application.properties` |
| Architecture en couches | dossiers `presentation` / `application` / `infrastructure` dans chaque service |
| Entité vs DTO vs DAO | `Employe` (@Entity) / `EmployeDTO` / `EmployeDAO` |
| Résilience (limite actuelle) | si `employe` est éteint, `GET /api/entreprises` échoue → un Circuit Breaker / fallback serait l'étape suivante |
| Évolution possible | 3e service `Projet`, API Gateway, Service Discovery, conteneurs Docker |

---

## 11. Glossaire express

- **Monolithe** : application unique et indivisible.
- **Microservice** : petit service autonome, une responsabilité métier.
- **Bounded context** : périmètre métier cohérent qui délimite un service.
- **API** : interface d'accès d'un service (ses URL et formats).
- **REST** : style d'API basé sur HTTP (ressources + verbes).
- **DTO / DAO / Entité** : objet d'échange / objet de donnée externe / objet mappé
  à une table.
- **Broker de messages** : intermédiaire pour la communication asynchrone (Kafka,
  RabbitMQ).
- **Eventual consistency** : les données finissent par être cohérentes, pas
  instantanément.
- **API Gateway** : porte d'entrée unique qui route vers les services.
- **Service Discovery** : annuaire qui donne l'adresse courante d'un service.
- **Circuit Breaker** : coupe-circuit qui stoppe les appels vers un service en
  panne.
- **Saga** : suite d'opérations sur plusieurs services avec compensation en cas
  d'échec.
- **Tracing distribué** : suivi d'une requête à travers tous les services.
- **Conteneur / Docker** : paquet exécutable isolé.
- **Kubernetes** : orchestrateur de conteneurs.
- **CI/CD** : intégration et livraison continues (build + tests + déploiement
  automatisés).
