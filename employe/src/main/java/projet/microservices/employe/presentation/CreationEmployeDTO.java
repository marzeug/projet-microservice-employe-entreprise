package projet.microservices.employe.presentation;

/**
 * Charge utile attendue pour la creation d'un employe.
 * L'identifiant est genere par la base, seul le nom est requis.
 */
public class CreationEmployeDTO {

	private String nom;

	public CreationEmployeDTO() {
	}

	public CreationEmployeDTO(String nom) {
		this.nom = nom;
	}

	public String getNom() {
		return nom;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}
}
