package projet.microservices.entreprise.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import projet.microservices.entreprise.application.Entreprise;

/**
 * Acces aux donnees Entreprise via Spring Data JPA.
 */
@Repository
public interface EntrepriseRepository extends JpaRepository<Entreprise, Integer> {

	List<Entreprise> findByFavoriTrue();
}
