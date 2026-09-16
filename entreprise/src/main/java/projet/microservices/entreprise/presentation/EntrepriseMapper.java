package projet.microservices.entreprise.presentation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import projet.microservices.entreprise.application.EmployeDAO;
import projet.microservices.entreprise.application.Entreprise;

/**
 * Conversions entre l'entite metier Entreprise, les DAO distants et les DTO de presentation.
 */
@Component
public class EntrepriseMapper {

	/** Entite -> DTO (la liste des employes est completee ensuite par l'appel REST). */
	public EntrepriseDTO mapEntrepriseToEntrepriseDTO(Entreprise entreprise) {
		return new EntrepriseDTO(entreprise.getId(), entreprise.getNom(), new ArrayList<>());
	}

	/** Charge utile de creation -> entite. */
	public Entreprise mapCreationEntrepriseDTOToEntreprise(CreationEntrepriseDTO dto) {
		Entreprise entreprise = new Entreprise();
		entreprise.setNom(dto.getNom());
		entreprise.setIdEmployes(dto.getIdEmployes());
		return entreprise;
	}

	/** DAO distant -> DTO expose. */
	public EmployeDTO mapEmployeDAOToEmployeDTO(EmployeDAO dao) {
		return new EmployeDTO(dao.getId(), dao.getNom());
	}

	public List<EmployeDTO> mapEmployeDAOToEmployeDTO(List<EmployeDAO> daos) {
		List<EmployeDTO> resultat = new ArrayList<>();
		for (EmployeDAO dao : daos) {
			resultat.add(mapEmployeDAOToEmployeDTO(dao));
		}
		return resultat;
	}
}
