package projet.microservices.entreprise.infrastructure;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import projet.microservices.entreprise.application.EmployeDAO;

/**
 * Client REST vers le microservice Employe.
 * <p>
 * Remplace l'API Jersey {@code Client} du tutoriel par {@link RestClient} (Spring Boot 3+),
 * et isole l'appel distant dans la couche infrastructure plutot que dans le service metier.
 */
@Component
public class EmployeClient {

	private final RestClient restClient;

	public EmployeClient(@Value("${employe.service.url}") String employeServiceUrl) {
		this.restClient = RestClient.create(employeServiceUrl);
	}

	/**
	 * Interroge GET /employes?idEmployes=..&idEmployes=.. sur le microservice Employe.
	 */
	public List<EmployeDAO> getEmployesByIds(List<Integer> idEmployes) {
		if (idEmployes == null || idEmployes.isEmpty()) {
			return List.of();
		}
		return restClient.get()
				.uri(uriBuilder -> {
					uriBuilder.path("/employes");
					for (Integer id : idEmployes) {
						uriBuilder.queryParam("idEmployes", id);
					}
					return uriBuilder.build();
				})
				.retrieve()
				.body(new ParameterizedTypeReference<List<EmployeDAO>>() {
				});
	}
}
