package projet.microservices.employe;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.ApplicationPath;
import projet.microservices.employe.presentation.EmployePresentation;

/**
 * Expose les webservices Jersey sous le prefixe /api.
 */
@Component
@Configuration
@ApplicationPath("api")
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
		register(EmployePresentation.class);
	}
}
