# Explication détaillée du projet (pour débutant)

Ce document explique **tout** le projet : le vocabulaire, les outils, l'architecture,
chaque fichier, le fonctionnement d'une requête de bout en bout, et les questions
que ton prof peut poser avec les réponses.

---

## 1. L'objectif du projet

On veut construire **deux applications séparées** (deux « microservices ») qui
tournent chacune de leur côté et qui se parlent **par le réseau** (protocole HTTP,
style REST) :

- **`employe`** : gère une liste d'employés (créer, lister, filtrer).
- **`entreprise`** : gère une liste d'entreprises. Chaque entreprise contient une
  liste d'identifiants d'employés. Quand on demande la liste des entreprises,
  `entreprise` **appelle** `employe` par le réseau pour récupérer le détail
  (nom, etc.) de chaque employé, et renvoie tout assemblé.

C'est le principe des microservices : au lieu d'une seule grosse application,
on découpe en petits services indépendants qui communiquent entre eux.

```
   Navigateur / Postman
          |
          |  GET http://localhost:9090/api/entreprises
          v
  +-------------------+        GET http://localhost:8081/api/employes?idEmployes=1&idEmployes=2
  |   entreprise      |  ----------------------------------------------------->  +----------------+
  |   port 9090       |                                                         |   employe      |
  |   base H2         |  <-----------------------------------------------------  |   port 8081    |
  |   "entreprise"    |        réponse JSON : [{id:1,nom:...},{id:2,nom:...}]    |   base H2      |
  +-------------------+                                                         |   "employe"    |
                                                                               +----------------+
```

> Note : dans le tutoriel d'origine `entreprise` est sur le port **8080**.
> Sur cette machine le **8080 est déjà pris par Jenkins**, donc on a mis
> `entreprise` sur **9090**. C'est le seul changement de port.

---

## 2. Le vocabulaire de base

| Terme | Explication simple |
|---|---|
| **Microservice** | Une petite application autonome qui rend un service précis et se lance toute seule. |
| **HTTP** | Le protocole du web. Un client envoie une *requête*, le serveur renvoie une *réponse*. |
| **REST / API REST** | Une façon standard d'exposer des données via HTTP avec des URL et des verbes (`GET` pour lire, `POST` pour créer...). |
| **Endpoint** | Une URL précise que le service sait traiter, ex. `GET /api/employes`. |
| **Port** | Un numéro qui identifie une application sur la machine. Deux applications ne peuvent pas écouter le même port en même temps (d'où 8081 et 9090). |
| **JSON** | Le format texte d'échange des données, ex. `{"id":1,"nom":"Alice"}`. |
| **Base de données** | Là où les données sont stockées durablement. Ici **H2**, une base légère qui écrit dans un simple fichier. |
| **ORM / JPA / Hibernate** | Une couche qui transforme automatiquement des **objets Java** en **lignes de table** (et inversement), pour ne pas écrire du SQL à la main. JPA = la norme, Hibernate = l'outil qui l'implémente. |
| **Entité** | Une classe Java qui correspond à une table de la base (annotée `@Entity`). |
| **DTO** (*Data Transfer Object*) | Un objet « de transport » : ce qu'on envoie/reçoit par l'API. Volontairement différent de l'entité. |
| **DAO** (*Data Access Object*) | Ici : un objet qui **représente une donnée venue d'un autre service** (pas de la base locale). |
| **Repository** | L'objet qui parle à la base (lire, écrire). Fourni presque gratuitement par Spring Data JPA. |
| **Injection de dépendances** | On ne fait pas `new MonService()` soi-même : c'est **Spring** qui crée les objets et les « injecte » là où on en a besoin. |

---

## 3. Les outils et technologies

| Outil | À quoi ça sert dans le projet |
|---|---|
| **Java 17** | Le langage. (La machine a un JDK 25 installé, compatible.) |
| **Maven** | L'outil de build : il télécharge les bibliothèques, compile, lance les tests, fabrique le `.jar`. Configuré par le fichier `pom.xml`. |
| **Spring Boot 4.1.1** | Le framework principal. Il démarre un serveur web (Tomcat) tout seul, gère l'injection de dépendances, la connexion à la base, etc. |
| **Jersey** | La bibliothèque qui gère les **webservices REST** (annotations `@Path`, `@GET`, `@POST`...). C'est une implémentation de la norme JAX-RS. Le tutoriel l'impose. |
| **Spring Data JPA + Hibernate** | Pour la persistance : on écrit des interfaces `Repository`, Hibernate génère le SQL. |
| **H2** | La base de données. Mode « fichier » : les données sont dans `employe.mv.db` et `entreprise.mv.db`. Une console web permet de la visualiser. |
| **RestClient** (Spring) | L'outil qui permet à `entreprise` d'**appeler** `employe` par HTTP depuis du code Java. |

### Le fichier `pom.xml` (identique dans les 2 projets, au nom près)

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>4.1.1</version>
</parent>
<properties><java.version>17</java.version></properties>

<dependencies>
  spring-boot-starter          → socle Spring
  spring-boot-starter-web      → serveur web Tomcat + support HTTP
  spring-boot-starter-jersey   → webservices REST (Jersey / JAX-RS)
  spring-boot-starter-data-jpa → persistance JPA / Hibernate
  h2                           → base de données H2
  spring-boot-h2console        → console web de la base H2
  spring-boot-starter-test     → JUnit pour les tests (portée "test")
</dependencies>
```

Ce sont **exactement** les dépendances demandées par le tutoriel (Jersey, Spring Web,
H2, Spring Data JPA), rien de superflu.

---

## 4. L'architecture en couches (dans CHAQUE microservice)

Chaque microservice est découpé en **3 couches**. Une couche ne parle qu'à la
couche voisine. Ça sépare les responsabilités et rend le code clair et testable.

```
        REQUÊTE HTTP entrante
               |
               v
+---------------------------------+
|  presentation                   |   "la façade web"
|  - classe REST (@Path, @GET...) |   reçoit la requête, renvoie du JSON
|  - DTO (objets échangés)        |   ne contient PAS de logique métier
|  - mapper (entité <-> DTO)      |
+---------------------------------+
               |  appelle
               v
+---------------------------------+
|  application                    |   "le cerveau / métier"
|  - entité métier (@Entity)      |   les règles : quoi faire, dans quel ordre
|  - service (@Service)           |
|  - objets d'échange (DAO)       |
+---------------------------------+
               |  appelle
               v
+---------------------------------+
|  infrastructure                 |   "les branchements vers l'extérieur"
|  - repository (accès base)      |   la base de données
|  - client REST vers l'autre     |   l'autre microservice
|    microservice                 |
+---------------------------------+
               |
               v
        Base H2   /   Microservice voisin
```

**Règles qu'on a respectées :**
- Les endpoints (couche `presentation`) ne contiennent **aucune** logique : ils
  délèguent au `service`.
- On ne renvoie **jamais** une entité JPA directement : toujours un **DTO**.
- Un microservice n'accède **jamais** directement à la base de l'autre : il passe
  par un **appel REST**.
- Les objets sont fournis par **injection de dépendances par constructeur**
  (le constructeur reçoit ce dont la classe a besoin).

---

## 5. Le microservice `employe` (port 8081), fichier par fichier

Package racine : `projet.microservices.employe`

### `EmployeApplication.java` — le point de démarrage
```java
@SpringBootApplication
public class EmployeApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmployeApplication.class, args);
    }
}
```
`@SpringBootApplication` = « ceci est une appli Spring Boot ». La méthode `main`
démarre tout : le serveur web, la base, le scan des classes annotées.

### `JerseyConfig.java` — activer les webservices REST
```java
@Component
@Configuration
@ApplicationPath("api")          // toutes les URL REST commenceront par /api
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(EmployePresentation.class);   // on déclare la classe qui contient les endpoints
    }
}
```
Sans ce fichier, Jersey ne sait pas quelles classes exposer. `@ApplicationPath("api")`
explique pourquoi les URL sont `/api/employes` et pas juste `/employes`.

### `application/Employe.java` — l'entité (table en base)
```java
@Entity
public class Employe {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)   // l'id est généré automatiquement
    private int id;
    private String nom;
    // constructeurs, getters, setters
}
```
`@Entity` = Hibernate crée une table `EMPLOYE(ID, NOM)`. `@Id` = clé primaire.
`@GeneratedValue` = on ne fournit pas l'id, la base l'attribue.

### `infrastructure/EmployeRepository.java` — l'accès à la base
```java
@Repository
public interface EmployeRepository extends JpaRepository<Employe, Integer> {
}
```
On écrit juste une **interface vide**. En héritant de `JpaRepository<Employe, Integer>`
(Employe = le type géré, Integer = le type de l'id), on récupère gratuitement
`findAll()`, `findById(id)`, `save(objet)`, `deleteById(id)`, etc. Hibernate écrit
le SQL pour nous.

### `application/EmployeService.java` — les règles métier
```java
@Service
public class EmployeService {
    private final EmployeRepository repo;

    public EmployeService(EmployeRepository repo) {   // injection par constructeur
        this.repo = repo;
    }

    public List<Employe> getEmployes() {
        return repo.findAll();                        // tous les employés
    }

    public List<Employe> getEmployesByIds(List<Integer> idEmployes) {
        List<Employe> resultat = new ArrayList<>();
        for (int id : idEmployes) {                   // on boucle sur les id demandés
            repo.findById(id).ifPresent(resultat::add);  // on ajoute si l'employé existe
        }
        return resultat;
    }

    public Employe creationEmploye(Employe employe) {
        return repo.save(employe);                    // insert en base, renvoie l'objet avec son id
    }
}
```
`@Service` = Spring crée un exemplaire unique et le rend injectable ailleurs.

### `presentation/EmployeDTO.java` — ce qu'on renvoie
```java
public class EmployeDTO {
    private int id;
    private String nom;
    // constructeurs, getters, setters
}
```
Ici identique à l'entité, mais c'est **volontairement une classe séparée** : si
demain l'entité gagne des champs internes (mot de passe, date de création...), on
ne les exposera pas dans l'API. Séparer DTO et entité est une bonne pratique.

### `presentation/CreationEmployeDTO.java` — ce qu'on reçoit pour créer
```java
public class CreationEmployeDTO {
    private String nom;      // pas d'id : c'est la base qui le génère
    // constructeur, getter, setter
}
```

### `presentation/EmployeMapper.java` — traducteur entité <-> DTO
```java
@Component
public class EmployeMapper {
    public EmployeDTO mapEmployeToEmployeDTO(Employe e) {
        return new EmployeDTO(e.getId(), e.getNom());
    }
    public Employe mapCreationEmployeDTOToEmploye(CreationEmployeDTO dto) {
        Employe e = new Employe();
        e.setNom(dto.getNom());
        return e;
    }
}
```
Le mapper évite de recopier ce code de conversion partout.

### `presentation/EmployePresentation.java` — les endpoints REST
```java
@Component
@Path("employes")                       // -> URL de base : /api/employes
public class EmployePresentation {
    private final EmployeService service;
    private final EmployeMapper mapper;

    public EmployePresentation(EmployeService service, EmployeMapper mapper) {  // injection
        this.service = service;
        this.mapper = mapper;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<EmployeDTO> getEmployes(@QueryParam("idEmployes") List<Integer> idEmployes) {
        List<Employe> employes = (idEmployes == null || idEmployes.isEmpty())
                ? service.getEmployes()                       // pas de filtre -> tous
                : service.getEmployesByIds(idEmployes);       // filtre -> seulement ceux demandés
        List<EmployeDTO> resultat = new ArrayList<>();
        for (Employe e : employes) resultat.add(mapper.mapEmployeToEmployeDTO(e));
        return resultat;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response creationEmploye(CreationEmployeDTO dto) {
        Employe aCreer = mapper.mapCreationEmployeDTOToEmploye(dto);
        Employe cree = service.creationEmploye(aCreer);
        return Response.status(Response.Status.CREATED)       // code HTTP 201
                .entity(mapper.mapEmployeToEmployeDTO(cree))
                .build();
    }
}
```

Points importants :
- `@QueryParam("idEmployes") List<Integer>` : Jersey sait lire **plusieurs**
  paramètres du même nom dans l'URL. C'est ce qui permet
  `?idEmployes=1&idEmployes=3` → `[1, 3]`.
- L'endpoint ne fait **que** : appeler le service + convertir avec le mapper.
  Aucune règle métier ici.

### `src/main/resources/application.properties` — la configuration
```properties
spring.application.name=employe
server.port=8081                                   # ce service écoute sur 8081
spring.jpa.hibernate.ddl-auto=update              # crée/met à jour les tables au démarrage
spring.datasource.url=jdbc:h2:file:./employe      # base H2 dans le fichier ./employe.mv.db
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2                         # console visible sur http://localhost:8081/h2
```

---

## 6. Le microservice `entreprise` (port 9090), fichier par fichier

Package racine : `projet.microservices.entreprise`.
On retrouve la même structure, plus **la communication vers `employe`**.

### `application/Entreprise.java` — l'entité
```java
@Entity
public class Entreprise {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String nom;

    @ElementCollection(fetch = FetchType.EAGER)                       // <-- important
    @CollectionTable(name = "entreprise_employes",
                     joinColumns = @JoinColumn(name = "entreprise_id"))
    @Column(name = "id_employe")
    private List<Integer> idEmployes = new ArrayList<>();
    // constructeurs, getters, setters
}
```

**Pourquoi `@ElementCollection` ?** Une entité JPA ne sait **pas** stocker une
`List<Integer>` telle quelle dans une colonne. `@ElementCollection` +
`@CollectionTable` dit à Hibernate de créer une **2e table**
`ENTREPRISE_EMPLOYES(ENTREPRISE_ID, ID_EMPLOYE)` qui contient une ligne par
identifiant d'employé. `FetchType.EAGER` = on charge tout de suite cette liste
avec l'entreprise (pratique ici car on l'utilise juste après, hors transaction).

> Le tutoriel PDF écrit juste `private List<Integer> idEmployes;` sans annotation :
> ça **ne compile/tourne pas** avec JPA. C'est une correction nécessaire.

### `application/EmployeDAO.java` — un employé « venu de l'autre service »
```java
public class EmployeDAO {          // PAS de @Entity : ce n'est pas une table locale
    private int id;
    private String nom;
    // constructeurs, getters, setters
}
```
Quand `entreprise` appelle `employe`, la réponse JSON `{"id":1,"nom":"Alice"}` est
convertie en objet `EmployeDAO`. Le microservice `entreprise` **ne stocke jamais**
d'employé : il ne fait que le lire chez le voisin.

### `infrastructure/EntrepriseRepository.java`
Identique en principe à `EmployeRepository` : `interface ... extends JpaRepository<Entreprise, Integer>`.

### `infrastructure/EmployeClient.java` — l'appel REST vers `employe`
```java
@Component
public class EmployeClient {
    private final RestClient restClient;

    public EmployeClient(@Value("${employe.service.url}") String url) {
        this.restClient = RestClient.create(url);      // url = http://localhost:8081/api
    }

    public List<EmployeDAO> getEmployesByIds(List<Integer> idEmployes) {
        if (idEmployes == null || idEmployes.isEmpty()) return List.of();
        return restClient.get()
            .uri(b -> {
                b.path("/employes");
                for (Integer id : idEmployes) b.queryParam("idEmployes", id);  // ?idEmployes=1&idEmployes=2...
                return b.build();
            })
            .retrieve()
            .body(new ParameterizedTypeReference<List<EmployeDAO>>() {});       // convertit le JSON en List<EmployeDAO>
    }
}
```
C'est **le cœur du sujet microservices** : du code Java qui envoie une requête
HTTP `GET http://localhost:8081/api/employes?idEmployes=...` à l'autre service et
transforme la réponse JSON en objets.

> Le tutoriel utilise l'ancienne API `Client` de Jersey pour ça. On a préféré
> `RestClient` (l'outil moderne de Spring), et on l'a **rangé dans la couche
> `infrastructure`** (et non dans le service), ce qui respecte mieux
> l'architecture en couches.

### `application/EntrepriseService.java`
```java
@Service
public class EntrepriseService {
    private final EntrepriseRepository repo;
    private final EmployeClient employeClient;

    public EntrepriseService(EntrepriseRepository repo, EmployeClient employeClient) {
        this.repo = repo;
        this.employeClient = employeClient;
    }

    public List<Entreprise> getEntreprises()            { return repo.findAll(); }
    public Entreprise creationEntreprise(Entreprise e)  { return repo.save(e); }
    public List<EmployeDAO> getEmployes(List<Integer> ids) {
        return employeClient.getEmployesByIds(ids);     // délègue l'appel réseau à l'infrastructure
    }
}
```

### `presentation/` — DTO, mapper, endpoints

- `EmployeDTO` (id, nom) : l'employé tel qu'affiché **dans** la réponse entreprise.
- `EntrepriseDTO` (id, nom, `List<EmployeDTO> employes`) : ce qu'on renvoie.
- `CreationEntrepriseDTO` (nom, `List<Integer> idEmployes`) : ce qu'on reçoit pour créer.
- `EntrepriseMapper` :
  - `mapEntrepriseToEntrepriseDTO` : entité → DTO (liste `employes` vide au début).
  - `mapCreationEntrepriseDTOToEntreprise` : DTO de création → entité.
  - `mapEmployeDAOToEmployeDTO` : `EmployeDAO` (venu du réseau) → `EmployeDTO` (affiché).

`EntreprisePresentation` :
```java
@Component
@Path("entreprises")
public class EntreprisePresentation {
    private final EntrepriseService service;
    private final EntrepriseMapper mapper;
    // constructeur (injection)

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<EntrepriseDTO> getEntreprises() {
        List<EntrepriseDTO> resultat = new ArrayList<>();
        for (Entreprise e : service.getEntreprises()) {
            EntrepriseDTO dto = mapper.mapEntrepriseToEntrepriseDTO(e);
            if (!e.getIdEmployes().isEmpty()) {
                List<EmployeDAO> employes = service.getEmployes(e.getIdEmployes());  // APPEL au microservice employe
                dto.setEmployes(mapper.mapEmployeDAOToEmployeDTO(employes));
            }
            resultat.add(dto);
        }
        return resultat;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response creationEntreprise(CreationEntrepriseDTO dto) {
        Entreprise cree = service.creationEntreprise(mapper.mapCreationEntrepriseDTOToEntreprise(dto));
        return Response.status(Response.Status.CREATED)
                .entity(mapper.mapEntrepriseToEntrepriseDTO(cree))
                .build();
    }
}
```

### `application.properties` de `entreprise`
```properties
spring.application.name=entreprise
server.port=9090                                     # 8080 pris par Jenkins -> 9090
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:h2:file:./entreprise
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2
employe.service.url=http://localhost:8081/api        # adresse du microservice employe
```
La ligne `employe.service.url` est lue par `EmployeClient` grâce à `@Value("${employe.service.url}")`.
Si l'adresse du service `employe` change, on modifie juste cette ligne, pas le code.

---

## 7. Le parcours complet d'une requête (exemple concret)

**Scénario : `GET http://localhost:9090/api/entreprises`** avec en base une
entreprise « ACME » dont `idEmployes = [1, 2, 3]`.

1. Le navigateur envoie la requête HTTP au serveur `entreprise` (port 9090).
2. Jersey voit l'URL `/api/entreprises` + verbe `GET` → appelle
   `EntreprisePresentation.getEntreprises()`.
3. La méthode appelle `service.getEntreprises()`.
4. Le service appelle `repo.findAll()` → Hibernate fait un `SELECT * FROM ENTREPRISE`
   (+ la table `ENTREPRISE_EMPLOYES`) → renvoie l'objet `Entreprise` ACME avec
   `idEmployes = [1,2,3]`.
5. De retour dans l'endpoint : la liste `idEmployes` n'est pas vide, donc on
   appelle `service.getEmployes([1,2,3])`.
6. Le service délègue à `employeClient.getEmployesByIds([1,2,3])`.
7. `EmployeClient` envoie une **vraie requête HTTP** :
   `GET http://localhost:8081/api/employes?idEmployes=1&idEmployes=2&idEmployes=3`.
8. Le serveur `employe` (port 8081) reçoit ça : Jersey → `EmployePresentation.getEmployes([1,2,3])`
   → `service.getEmployesByIds([1,2,3])` → `repo.findById(1/2/3)` → 3 objets `Employe`
   → conversion en `EmployeDTO` → réponse JSON
   `[{"id":1,"nom":"..."},{"id":2,...},{"id":3,...}]`.
9. `EmployeClient` reçoit ce JSON et le transforme en `List<EmployeDAO>`.
10. L'endpoint `entreprise` convertit ces `EmployeDAO` en `EmployeDTO`, les place
    dans `EntrepriseDTO.employes`.
11. Jersey transforme le `EntrepriseDTO` final en JSON et le renvoie au navigateur :
```json
[{"id":1,"nom":"ACME","employes":[
   {"id":1,"nom":"mon super employe"},
   {"id":2,"nom":"mon super employe 2"},
   {"id":3,"nom":"mon super employe 3"}]}]
```

**Deux serveurs, deux bases, un appel réseau entre eux : c'est un vrai système en microservices.**

---

## 8. Les corrections faites par rapport au tutoriel PDF (et pourquoi)

| # | Le PDF dit | Ce qu'on a fait | Pourquoi |
|---|---|---|---|
| 1 | `private List<Integer> idEmployes;` dans l'entité | Ajout de `@ElementCollection` + `@CollectionTable` + `@Column` | JPA ne sait pas persister une `List<Integer>` sans ça : l'appli ne démarre pas. |
| 2 | Appel réseau avec l'API Jersey `Client` (`ClientBuilder.newClient()...`) | `RestClient` de Spring, dans `infrastructure/EmployeClient` | `RestClient` est l'outil moderne recommandé ; le ranger en `infrastructure` respecte l'architecture en couches (pas d'appel réseau dans le service métier). |
| 3 | `@Autowired` sur un champ (`@Autowired private EmployeService service;`) | Injection **par constructeur** + `@Component` sur les classes REST | L'injection par constructeur est la pratique recommandée : dépendances explicites, champs `final`, classe testable. |
| 4 | `POST` renvoie `void` → code HTTP `204 No Content` | `POST` renvoie `201 Created` + la ressource créée | `201` est le code standard pour « créé », et renvoyer l'objet créé (avec son id) est plus utile. |
| 5 | `spring.datasource.driverClassName=...` | `spring.datasource.driver-class-name=...` | Écriture normalisée de Spring Boot (les deux marchent, celle-ci est la forme officielle). |
| 6 | `entreprise` sur le port `8080` | port `9090` | Le `8080` est occupé par Jenkins sur cette machine. |
| 7 | Package `com.example.demo.infrastucture` (faute de frappe) | `...infrastructure` | Orthographe correcte. |
| 8 | Spring Boot `3.5.6` | Spring Boot `4.1.1` (déjà en place dans le projet fourni) | On garde la version du projet ; imports en `jakarta.*` (obligatoire à partir de Spring Boot 3). |
| 9 | 3e microservice `Projet` | Non réalisé | Explicitement demandé comme « évolution future » seulement. |

---

## 9. Comment compiler, lancer, tester

### Compiler + lancer les tests
```powershell
cd "C:\Users\tOp laptOps\Downloads\demo (5)\employe"
mvn test
cd "C:\Users\tOp laptOps\Downloads\demo (5)\entreprise"
mvn test
```
Chaque projet a un test `contextLoads` : il démarre tout le contexte Spring
(web + Jersey + JPA). S'il passe, le câblage est correct.
Résultat attendu : `Tests run: 1, Failures: 0, Errors: 0` + `BUILD SUCCESS`.

### Lancer les 2 services (2 terminaux séparés, `employe` d'abord)
```powershell
# Terminal A
cd "C:\Users\tOp laptOps\Downloads\demo (5)\employe"
mvn spring-boot:run          # attendre "Started EmployeApplication", laisser tourner

# Terminal B
cd "C:\Users\tOp laptOps\Downloads\demo (5)\entreprise"
mvn spring-boot:run          # attendre "Started EntrepriseApplication", laisser tourner
```
`mvn spring-boot:run` **ne rend pas la main** : c'est normal, le serveur tourne
tant que le terminal est ouvert.

### Tester (navigateur ou Postman)

| Requête | Attendu |
|---|---|
| `POST http://localhost:8081/api/employes` body `{"nom":"Alice"}` | `201` + `{"id":1,"nom":"Alice"}` |
| `GET  http://localhost:8081/api/employes` | la liste des employés |
| `GET  http://localhost:8081/api/employes?idEmployes=1&idEmployes=3` | seulement les employés 1 et 3 |
| `POST http://localhost:9090/api/entreprises` body `{"nom":"ACME","idEmployes":[1,2,3]}` | `201` |
| `GET  http://localhost:9090/api/entreprises` | ACME avec le détail complet des employés |
| `http://localhost:8081/h2` et `http://localhost:9090/h2` | console de la base (JDBC URL `jdbc:h2:file:./employe` ou `./entreprise`, user `sa`, pas de mot de passe) |

> `http://localhost:8081` **tout court** renvoie une erreur 404 : c'est normal,
> il n'y a rien à la racine. Une 404 prouve juste que le serveur répond.
> Les vraies URL commencent par `/api/`.

---

## 10. Questions possibles du prof + réponses courtes

**Q : C'est quoi un microservice ici ?**
Une application Spring Boot autonome, avec son propre port et sa propre base H2,
qu'on lance séparément. Il y en a deux : `employe` et `entreprise`.

**Q : Comment les deux services communiquent ?**
Par HTTP/REST. `entreprise` contient une classe `EmployeClient` qui fait un
`GET http://localhost:8081/api/employes?idEmployes=...` vers `employe` avec
`RestClient`, et transforme la réponse JSON en objets `EmployeDAO`.

**Q : Pourquoi `entreprise` n'accède pas directement à la base de `employe` ?**
Parce que le principe des microservices est l'indépendance : chacun est seul
maître de sa base. On ne communique que par l'API. Ça permet de faire évoluer
ou déployer un service sans casser l'autre.

**Q : Différence entre Entité, DTO et DAO ?**
- **Entité** (`@Entity`) : image d'une table, gérée par Hibernate.
- **DTO** : objet d'échange de l'API (ce qu'on reçoit/renvoie), séparé de l'entité
  pour ne pas exposer l'interne.
- **DAO** (ici) : objet qui représente une donnée **venue d'un autre service**
  (pas de la base locale) ; `entreprise` ne le persiste jamais.

**Q : À quoi sert le mapper ?**
À convertir entre entité et DTO (et DAO → DTO), en un seul endroit, pour ne pas
répéter ce code.

**Q : C'est quoi l'injection de dépendances par constructeur ?**
Au lieu de faire `new EmployeService()`, on déclare `EmployeService` en paramètre
du constructeur ; Spring crée l'objet et le passe automatiquement. Avantages :
dépendances visibles, champs `final`, tests faciles.

**Q : À quoi sert `JerseyConfig` ?**
À enregistrer les classes de webservices auprès de Jersey et à fixer le préfixe
d'URL `/api` (`@ApplicationPath("api")`).

**Q : Comment fonctionne le filtre `?idEmployes=1&idEmployes=3` ?**
Le paramètre `@QueryParam("idEmployes") List<Integer>` de Jersey collecte toutes
les valeurs portant ce nom dans l'URL → `[1, 3]`. Si la liste est vide on renvoie
tous les employés, sinon on ne renvoie que ceux dont l'id est dans la liste
(`getEmployesByIds`).

**Q : Pourquoi `@ElementCollection` sur `idEmployes` ?**
Parce qu'une entité JPA ne peut pas stocker une `List<Integer>` dans une simple
colonne. `@ElementCollection` crée une table annexe avec une ligne par identifiant.

**Q : Pourquoi le port 9090 et pas 8080 ?**
Le 8080 est déjà utilisé par Jenkins sur cette machine. On a mis `entreprise` sur
9090 dans `application.properties`. C'est le seul écart de configuration.

**Q : Que se passe-t-il si `employe` est éteint quand on appelle `entreprise` ?**
L'appel REST échoue et `GET /api/entreprises` renvoie une erreur 500 pour les
entreprises qui ont des employés. C'est pour ça qu'on démarre toujours `employe`
en premier. (On pourrait ajouter une gestion d'erreur « liste vide » si besoin.)

**Q : Rôle de H2 et de `ddl-auto=update` ?**
H2 est une base légère qui stocke tout dans un fichier (`employe.mv.db`,
`entreprise.mv.db`). `spring.jpa.hibernate.ddl-auto=update` demande à Hibernate de
créer/mettre à jour automatiquement les tables au démarrage, d'après les entités.

**Q : Comment tu as vérifié que ça marche ?**
`mvn test` (contexte qui se charge) sur les deux projets, puis lancement des deux
services et test des endpoints avec `curl` : création d'employés, filtre par ids,
création d'entreprise, et enfin `GET /api/entreprises` qui renvoie bien les
employés complets récupérés via l'appel REST inter-services.
