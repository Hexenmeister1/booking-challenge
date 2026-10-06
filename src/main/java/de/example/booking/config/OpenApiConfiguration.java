package de.example.booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI bookingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ressourcen-Buchung API")
                        .version("1.0.0")
                        .description("API zum Anzeigen von Ressourcen und Verwalten ihrer Buchungen."));
    }
}