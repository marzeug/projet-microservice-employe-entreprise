package projet.microservices.entreprise;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.ApplicationPath;
import projet.microservices.entreprise.presentation.EntreprisePresentation;

/**
 * Expose les webservices Jersey sous le prefixe /api.
 */
@Component
@Configuration
@ApplicationPath("api")
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
		register(EntreprisePresentation.class);
		register((jakarta.ws.rs.container.ContainerResponseFilter)
    (request, response) -> {
        response.getHeaders().putSingle(
            "Access-Control-Allow-Origin",
            "http://127.0.0.1:3000" //mettre l'url de votre front-end ici
        );
        response.getHeaders().putSingle(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, OPTIONS"
        );
        response.getHeaders().putSingle(
            "Access-Control-Allow-Headers",
            "Content-Type"
        );
    });
	}
}
