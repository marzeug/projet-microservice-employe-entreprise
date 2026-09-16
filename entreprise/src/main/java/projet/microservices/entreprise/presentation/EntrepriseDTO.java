package projet.microservices.entreprise.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation d'une entreprise exposee par l'API, employes complets inclus.
 */
public class EntrepriseDTO {

	private int id;
	private String nom;
	private List<EmployeDTO> employes = new ArrayList<>();

	public EntrepriseDTO() {
	}

	public EntrepriseDTO(int id, String nom, List<EmployeDTO> employes) {
		this.id = id;
		this.nom = nom;
		this.employes = (employes != null) ? employes : new ArrayList<>();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public List<EmployeDTO> getEmployes() {
		return employes;
	}

	public void setEmployes(List<EmployeDTO> employes) {
		this.employes = (employes != null) ? employes : new ArrayList<>();
	}
}
