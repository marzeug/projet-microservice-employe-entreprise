package projet.microservices.entreprise.application;

/**
 * Representation d'un employe tel que renvoye par le microservice Employe.
 * Ce n'est pas une entite JPA : le microservice Entreprise ne persiste jamais d'employe,
 * il se contente de consommer les donnees exposees par l'autre microservice.
 */
public class EmployeDAO {

	private int id;
	private String nom;

	public EmployeDAO() {
	}

	public EmployeDAO(int id, String nom) {
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
