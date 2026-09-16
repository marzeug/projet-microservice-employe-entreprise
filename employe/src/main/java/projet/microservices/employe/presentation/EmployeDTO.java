package projet.microservices.employe.presentation;

/**
 * Representation d'un employe exposee par l'API.
 */
public class EmployeDTO {

	private int id;
	private String nom;

	public EmployeDTO() {
	}

	public EmployeDTO(int id, String nom) {
		this.id = id;
		this.nom = nom;
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
}
