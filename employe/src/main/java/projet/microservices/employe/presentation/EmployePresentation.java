package projet.microservices.employe.presentation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import projet.microservices.employe.application.Employe;
import projet.microservices.employe.application.EmployeService;

/**
 * Webservices REST de la ressource Employe.
 */
@Component
@Path("employes")
public class EmployePresentation {

	private final EmployeService service;
	private final EmployeMapper mapper;

	public EmployePresentation(EmployeService service, EmployeMapper mapper) {
		this.service = service;
		this.mapper = mapper;
	}

	/**
	 * Liste les employes. Sans filtre : tous les employes.
	 * Avec un ou plusieurs parametres idEmployes : uniquement ceux demandes.
	 * Exemple : GET /api/employes?idEmployes=1&idEmployes=3
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<EmployeDTO> getEmployes(@QueryParam("idEmployes") List<Integer> idEmployes) {
		List<Employe> employesBdd;
		if (idEmployes == null || idEmployes.isEmpty()) {
			employesBdd = service.getEmployes();
		} else {
			employesBdd = service.getEmployesByIds(idEmployes);
		}

		List<EmployeDTO> employesRetournes = new ArrayList<>();
		for (Employe e : employesBdd) {
			employesRetournes.add(mapper.mapEmployeToEmployeDTO(e));
		}
		return employesRetournes;
	}

	/**
	 * Cree un employe a partir de la charge utile JSON et renvoie 201 + la ressource creee.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response creationEmploye(CreationEmployeDTO creationEmployeDTO) {
		Employe aCreer = mapper.mapCreationEmployeDTOToEmploye(creationEmployeDTO);
		Employe cree = service.creationEmploye(aCreer);
		return Response.status(Response.Status.CREATED)
				.entity(mapper.mapEmployeToEmployeDTO(cree))
				.build();
	}
}
