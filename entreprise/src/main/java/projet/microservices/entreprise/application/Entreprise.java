package projet.microservices.entreprise.application;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

/**
 * Entite metier persistée du microservice Entreprise.
 * La liste des identifiants d'employes est stockee dans une table dediee
 * via @ElementCollection (JPA ne sait pas persister une List&lt;Integer&gt; brute).
 */
@Entity
public class Entreprise {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;

	private String nom;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "entreprise_employes", joinColumns = @JoinColumn(name = "entreprise_id"))
	@Column(name = "id_employe")
	private List<Integer> idEmployes = new ArrayList<>();

	public Entreprise() {
	}

	public Entreprise(int id, String nom) {
		this.id = id;
		this.nom = nom;
	}

	public Entreprise(int id, String nom, List<Integer> idEmployes) {
		this.id = id;
		this.nom = nom;
		this.idEmployes = (idEmployes != null) ? idEmployes : new ArrayList<>();
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

	public List<Integer> getIdEmployes() {
		return idEmployes;
	}

	public void setIdEmployes(List<Integer> idEmployes) {
		this.idEmployes = (idEmployes != null) ? idEmployes : new ArrayList<>();
	}
}
