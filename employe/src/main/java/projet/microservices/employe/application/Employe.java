package projet.microservices.employe.application;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * Entite metier persistée du microservice Employe.
 */
@Entity
public class Employe {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String nom;

	public Employe() {
	}

	public Employe(int id, String nom) {
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
