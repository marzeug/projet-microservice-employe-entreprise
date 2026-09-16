package projet.microservices.employe.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import projet.microservices.employe.infrastructure.EmployeRepository;

/**
 * Regles metier de manipulation des employes.
 */
@Service
public class EmployeService {

	private final EmployeRepository repo;

	public EmployeService(EmployeRepository repo) {
		this.repo = repo;
	}

	/** Retourne tous les employes en base. */
	public List<Employe> getEmployes() {
		return repo.findAll();
	}

	/** Retourne uniquement les employes dont l'identifiant est present dans la liste. */
	public List<Employe> getEmployesByIds(List<Integer> idEmployes) {
		List<Employe> employesRetournes = new ArrayList<>();
		for (int id : idEmployes) {
			Optional<Employe> e = repo.findById(id);
			if (e.isPresent()) {
				employesRetournes.add(e.get());
			}
		}
		return employesRetournes;
	}

	/** Cree un employe et retourne l'instance persistee (id genere). */
	public Employe creationEmploye(Employe employe) {
		return repo.save(employe);
	}
}
