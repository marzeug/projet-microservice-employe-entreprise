package projet.microservices.entreprise.presentation;

import java.util.ArrayList;
import java.util.List;

/**
 * Charge utile attendue pour la creation d'une entreprise.
 * L'identifiant est genere par la base ; seuls le nom et la liste
 * d'identifiants d'employes sont fournis par le client.
 */
public class CreationEntrepriseDTO {

	private String nom;
	private List<Integer> idEmployes = new ArrayList<>();

	public CreationEntrepriseDTO() {
	}

	public CreationEntrepriseDTO(String nom, List<Integer> idEmployes) {
		this.nom = nom;
		this.idEmployes = (idEmployes != null) ? idEmployes : new ArrayList<>();
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
