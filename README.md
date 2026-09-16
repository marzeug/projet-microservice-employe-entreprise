# Microservices Entreprise / Employe (Spring Boot 4 + Jersey + H2)

Deux microservices Spring Boot **independants** qui communiquent en REST, d'apres
le tutoriel *Tuto Microservices (bis).pdf*.

```
                 GET /api/entreprises
client  ─────────────────────────────►  ┌───────────────────────┐
                                        │  entreprise  :9090    │
                                        │  H2 file ./entreprise │
                                        └──────────┬────────────┘
                                                   │ appel REST
                                   GET /api/employes?idEmployes=1&idEmployes=3
                                                   ▼
                                        ┌───────────────────────┐
                                        │  employe     :8081    │
                                        │  H2 file ./employe    │
                                        └───────────────────────┘
```

Le microservice `entreprise` ne touche jamais la base de `employe` : il passe
uniquement par l'API REST.

## Arborescence

```
demo (5)/
├── README.md                     ← ce fichier
├── entreprise/                   ← microservice Entreprise (port 9090)
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd / .mvn/
│   └── src/main/java/projet/microservices/entreprise/
│       ├── EntrepriseApplication.java
│       ├── JerseyConfig.java
│       ├── presentation/   EntreprisePresentation, EntrepriseDTO, CreationEntrepriseDTO,
│       │                   EmployeDTO, EntrepriseMapper
│       ├── application/    Entreprise (entite), EmployeDAO, EntrepriseService
│       └── infrastructure/ EntrepriseRepository, EmployeClient
└── employe/                      ← microservice Employe (port 8081)
    ├── pom.xml
    ├── mvnw / mvnw.cmd / .mvn/
    └── src/main/java/projet/microservices/employe/
        ├── EmployeApplication.java
        ├── JerseyConfig.java
        ├── presentation/   EmployePresentation, EmployeDTO, CreationEmployeDTO, EmployeMapper
        ├── application/    Employe (entite), EmployeService
        └── infrastructure/ EmployeRepository
```

> Le dossier `demo/` (squelette Spring Initializr de depart, sans endpoint,
> configure lui aussi sur le port 8081) a ete retire : il faisait doublon et
> provoquait un conflit de port. S'il est encore present :
> `Remove-Item -Recurse -Force "C:\Users\tOp laptOps\Downloads\demo (5)\demo"`

Chaque microservice suit l'architecture en couches :

| Couche          | Role                                                        |
|-----------------|------------------------------------------------------------|
| `presentation`  | endpoints Jersey, DTO exposes, mappers                     |
| `application`   | entite metier JPA, service (regles metier), objets d'echange |
| `infrastructure`| repository JPA, client REST vers l'autre microservice      |

## Prerequis

- **JDK 17+** (teste avec JDK 25)
- **Maven 3.9+** (ou le wrapper `./mvnw` fourni dans chaque module)
- Ports **9090** et **8081** libres
  - Le port 8080 du tutoriel est **deja occupe par Jenkins** sur cette machine :
    le microservice `entreprise` est donc configure sur **9090** (`server.port` dans
    son `application.properties`). Rien d'autre a faire.

## Configuration

| Microservice | Port  | Base H2                     | Console H2                     |
|--------------|-------|-----------------------------|-------------------------------|
| entreprise   | 9090  | `jdbc:h2:file:./entreprise` | http://localhost:9090/h2      |
| employe      | 8081  | `jdbc:h2:file:./employe`    | http://localhost:8081/h2      |

Console H2 : `Driver` = `org.h2.Driver`, `JDBC URL` = valeur ci-dessus,
`User` = `sa`, `Password` = *(vide)*.

Le microservice `entreprise` connait l'URL de `employe` via
`employe.service.url=http://localhost:8081/api` (dans `application.properties`).

## Demarrage

Ouvrir **deux terminaux**. Toujours demarrer `employe` en premier.

```powershell
# Terminal 1 - microservice Employe (port 8081)
cd "C:\Users\tOp laptOps\Downloads\demo (5)\employe"
mvn spring-boot:run
#   ou : mvn -DskipTests package ; java -jar target\employe-0.0.1-SNAPSHOT.jar
```

```powershell
# Terminal 2 - microservice Entreprise (port 9090)
cd "C:\Users\tOp laptOps\Downloads\demo (5)\entreprise"
mvn spring-boot:run
#   ou : mvn -DskipTests package ; java -jar target\entreprise-0.0.1-SNAPSHOT.jar
```

La base H2 (`*.mv.db`) est creee dans le dossier depuis lequel le microservice
est lance. Lancez donc chaque service depuis son propre dossier.

## Endpoints

### Microservice Employe (8081)

| Methode | URL                                             | Description                          |
|---------|-------------------------------------------------|-------------------------------------|
| GET     | `/api/employes`                                 | liste tous les employes             |
| GET     | `/api/employes?idEmployes=1&idEmployes=3`       | liste filtree par identifiants      |
| POST    | `/api/employes`                                 | cree un employe (201 + ressource)   |

### Microservice Entreprise (9090)

| Methode | URL                  | Description                                             |
|---------|----------------------|------------------------------------------------------|
| GET     | `/api/entreprises`   | liste les entreprises, employes complets inclus       |
| POST    | `/api/entreprises`   | cree une entreprise (201 + ressource)                 |

## Exemples Postman / curl

### 1. Creer des employes

```
POST http://localhost:8081/api/employes
Content-Type: application/json

{ "nom": "mon super employe" }
```

Repeter pour `"mon super employe 2"` et `"mon super employe 3"`.
Reponse : `201 Created` + `{ "id": 1, "nom": "mon super employe" }`

### 2. Lister les employes

```
GET http://localhost:8081/api/employes
```
```json
[
  { "id": 1, "nom": "mon super employe" },
  { "id": 2, "nom": "mon super employe 2" },
  { "id": 3, "nom": "mon super employe 3" }
]
```

### 3. Filtrer les employes

```
GET http://localhost:8081/api/employes?idEmployes=1&idEmployes=3
```
```json
[
  { "id": 1, "nom": "mon super employe" },
  { "id": 3, "nom": "mon super employe 3" }
]
```

### 4. Creer une entreprise avec des identifiants d'employes

```
POST http://localhost:9090/api/entreprises
Content-Type: application/json

{ "nom": "mon test final", "idEmployes": [1, 2, 3] }
```
Reponse : `201 Created` + `{ "id": 1, "nom": "mon test final", "employes": [] }`
(la liste `employes` est renseignee lors de la consultation, pas de la creation)

### 5. Lister les entreprises (reponse enrichie)

```
GET http://localhost:9090/api/entreprises
```
```json
[
  {
    "id": 1,
    "nom": "mon test final",
    "employes": [
      { "id": 1, "nom": "mon super employe" },
      { "id": 2, "nom": "mon super employe 2" },
      { "id": 3, "nom": "mon super employe 3" }
    ]
  }
]
```

## Ordre conseille pour tester

1. Demarrer `employe` (8081).
2. `POST http://localhost:8081/api/employes` x3.
3. `GET http://localhost:8081/api/employes` (verifier les 3).
4. `GET http://localhost:8081/api/employes?idEmployes=1&idEmployes=3` (verifier le filtre).
5. Demarrer `entreprise` (9090).
6. `POST http://localhost:9090/api/entreprises` avec `"idEmployes": [1, 2, 3]`.
7. `GET http://localhost:9090/api/entreprises` : les employes complets doivent apparaitre dans l'entreprise.
8. Consoles H2 : http://localhost:8081/h2 et http://localhost:9090/h2.

## Tests automatises

```bash
cd employe    && mvn test
cd entreprise && mvn test
```

Chaque module contient un test `contextLoads` (`@SpringBootTest`) qui verifie
le demarrage complet du contexte Spring + Jersey + JPA.

## Ecart avec le PDF

- `List<Integer> idEmployes` de l'entite `Entreprise` : ajout de
  `@ElementCollection` + `@CollectionTable` (JPA ne persiste pas une liste brute).
- Appel inter-services : `org.springframework.web.client.RestClient` (Spring Boot 4)
  au lieu de l'API Jersey `Client`, isole dans `infrastructure/EmployeClient`
  (et non dans le service metier).
- Injection par constructeur partout ; ressources Jersey annotees `@Component`.
- POST : renvoie `201 Created` + la ressource creee au lieu de `204 No Content`.
- `spring.h2.console.path=/h2`.
- Port du microservice `entreprise` : **9090** au lieu de 8080 (8080 occupe par Jenkins).

## Evolution future (non implementee)

Un 3e microservice `Projet` (port 8082) : chaque employe participe a des projets.
Le listing des entreprises declencherait alors un 2e appel REST en cascade
(`entreprise -> employe -> projet`). Hors perimetre de cette livraison.
