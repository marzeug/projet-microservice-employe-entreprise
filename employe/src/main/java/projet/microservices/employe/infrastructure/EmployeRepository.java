package projet.microservices.employe.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import projet.microservices.employe.application.Employe;

/**
 * Acces aux donnees Employe via Spring Data JPA.
 */
@Repository
public interface EmployeRepository extends JpaRepository<Employe, Integer> {
}
