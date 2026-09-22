package projet.microservices.entreprise.application;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import projet.microservices.entreprise.infrastructure.EmployeClient;
import projet.microservices.entreprise.infrastructure.EntrepriseRepository;

/**
 * Regles metier de manipulation des entreprises.
 */
@Service
public class EntrepriseService {

	private final EntrepriseRepository repo;
	private final EmployeClient employeClient;

	public EntrepriseService(EntrepriseRepository repo, EmployeClient employeClient) {
		this.repo = repo;
		this.employeClient = employeClient;
	}

	/** Retourne toutes les entreprises en base. */
	public List<Entreprise> getEntreprises() {
		return repo.findAll();
	}

	/** Retourne les entreprises marquees favorites. */
	public List<Entreprise> getEntreprisesFavorites() {
		return repo.findByFavoriTrue();
	}

	/** Bascule l'etat favori d'une entreprise et retourne l'entite mise a jour, ou vide si l'id est inconnu. */
	public Optional<Entreprise> toggleFavori(int id) {
		Optional<Entreprise> entreprise = repo.findById(id);
		entreprise.ifPresent(e -> {
			e.setFavori(!e.isFavori());
			repo.save(e);
		});
		return entreprise;
	}

	/** Cree une entreprise et retourne l'instance persistee (id genere). */
	public Entreprise creationEntreprise(Entreprise entreprise) {
		return repo.save(entreprise);
	}

	/** Recupere aupres du microservice Employe les informations des employes demandes. */
	public List<EmployeDAO> getEmployes(List<Integer> idEmployes) {
		return employeClient.getEmployesByIds(idEmployes);
	}
}
