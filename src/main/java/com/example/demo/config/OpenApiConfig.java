package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI demoOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("API Web II · TP3 - Seguridad y Autorización")
                        .description("API RESTful con Arquitectura Hexagonal, Spring Security, JWT (HMAC-SHA256), "
                                + "roles USER/ADMIN y persistencia en PostgreSQL.")
                        .version("v3"))
                // Aplica el esquema de seguridad globalmente
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // Define el esquema Bearer JWT en la interfaz de Swagger UI
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresá tu token JWT obtenido en el endpoint /api/auth/login")));
    }
}
