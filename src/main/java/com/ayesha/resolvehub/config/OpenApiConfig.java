package com.ayesha.resolvehub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resolveHubOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("ResolveHub API")
                .description("Issue tracking and resolution backend built with Spring Boot, Spring MVC, Spring Data JPA, Hibernate, and PostgreSQL.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Ayesha")
                    .email("support@resolvehub.com")
                )
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")
                )
            );
    }
}
