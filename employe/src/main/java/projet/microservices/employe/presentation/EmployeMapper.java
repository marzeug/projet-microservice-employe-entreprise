package projet.microservices.employe.presentation;

import org.springframework.stereotype.Component;

import projet.microservices.employe.application.Employe;

/**
 * Conversions entre l'entite metier Employe et les DTO de presentation.
 */
@Component
public class EmployeMapper {

	public EmployeDTO mapEmployeToEmployeDTO(Employe employe) {
		return new EmployeDTO(employe.getId(), employe.getNom());
	}

	public Employe mapCreationEmployeDTOToEmploye(CreationEmployeDTO dto) {
		Employe employe = new Employe();
		employe.setNom(dto.getNom());
		return employe;
	}
}
