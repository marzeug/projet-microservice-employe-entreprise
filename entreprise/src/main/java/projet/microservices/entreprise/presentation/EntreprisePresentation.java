package projet.microservices.entreprise.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import projet.microservices.entreprise.application.EmployeDAO;
import projet.microservices.entreprise.application.Entreprise;
import projet.microservices.entreprise.application.EntrepriseService;

/**
 * Webservices REST de la ressource Entreprise.
 */
@Component
@Path("entreprises")
public class EntreprisePresentation {

	private final EntrepriseService service;
	private final EntrepriseMapper mapper;

	public EntreprisePresentation(EntrepriseService service, EntrepriseMapper mapper) {
		this.service = service;
		this.mapper = mapper;
	}

	/**
	 * Liste les entreprises. Pour chaque entreprise possedant des identifiants d'employes,
	 * un appel REST vers le microservice Employe enrichit la reponse avec les employes complets.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<EntrepriseDTO> getEntreprises() {
		List<Entreprise> entreprisesBdd = service.getEntreprises();
		List<EntrepriseDTO> entreprisesRetournees = new ArrayList<>();

		for (Entreprise e : entreprisesBdd) {
			EntrepriseDTO dto = mapper.mapEntrepriseToEntrepriseDTO(e);
			if (!e.getIdEmployes().isEmpty()) {
				List<EmployeDAO> employes = service.getEmployes(e.getIdEmployes());
				dto.setEmployes(mapper.mapEmployeDAOToEmployeDTO(employes));
			}
			entreprisesRetournees.add(dto);
		}
		return entreprisesRetournees;
	}

	/**
	 * Cree une entreprise a partir de la charge utile JSON et renvoie 201 + la ressource creee.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response creationEntreprise(CreationEntrepriseDTO creationEntrepriseDTO) {
		Entreprise aCreer = mapper.mapCreationEntrepriseDTOToEntreprise(creationEntrepriseDTO);
		Entreprise cree = service.creationEntreprise(aCreer);
		return Response.status(Response.Status.CREATED)
				.entity(mapper.mapEntrepriseToEntrepriseDTO(cree))
				.build();
	}

	/** Liste les entreprises marquees favorites. */
	@GET
	@Path("favoris")
	@Produces(MediaType.APPLICATION_JSON)
	public List<EntrepriseDTO> getEntreprisesFavorites() {
		List<Entreprise> favoris = service.getEntreprisesFavorites();
		List<EntrepriseDTO> entreprisesRetournees = new ArrayList<>();

		for (Entreprise e : favoris) {
			EntrepriseDTO dto = mapper.mapEntrepriseToEntrepriseDTO(e);
			if (!e.getIdEmployes().isEmpty()) {
				List<EmployeDAO> employes = service.getEmployes(e.getIdEmployes());
				dto.setEmployes(mapper.mapEmployeDAOToEmployeDTO(employes));
			}
			entreprisesRetournees.add(dto);
		}
		return entreprisesRetournees;
	}

	/** Bascule l'etat favori d'une entreprise (ajoute si absente, retire si presente). */
	@PUT
	@Path("{id}/favori")
	@Produces(MediaType.APPLICATION_JSON)
	public Response toggleFavori(@PathParam("id") int id) {
		Optional<Entreprise> entreprise = service.toggleFavori(id);
		if (entreprise.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		return Response.ok(mapper.mapEntrepriseToEntrepriseDTO(entreprise.get())).build();
	}
}
