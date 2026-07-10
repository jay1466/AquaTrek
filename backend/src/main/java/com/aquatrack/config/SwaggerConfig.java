package com.aquatrack.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 / Swagger UI configuration for AquaTrack.
 *
 * <p>Accessible at:
 * <ul>
 *   <li>Swagger UI: {@code /swagger-ui.html}</li>
 *   <li>OpenAPI JSON: {@code /api-docs}</li>
 * </ul>
 * </p>
 *
 * <p>All secured endpoints require a Bearer JWT token. The "Authorize" button
 * in the Swagger UI allows testers to supply a token globally.</p>
 *
 * @author AquaTrack Engineering Team
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfig {

    /** Name used to reference the security scheme in @SecurityRequirement annotations. */
    public static final String BEARER_AUTH = "bearerAuth";

    @Value("${app.swagger.server-url:http://localhost:8080}")
    private String serverUrl;

    /**
     * Builds the top-level {@link OpenAPI} specification.
     *
     * <p>Registers:
     * <ul>
     *   <li>API metadata (title, version, description, contact, license)</li>
     *   <li>HTTP Bearer JWT security scheme</li>
     *   <li>Global security requirement so all endpoints require auth by default</li>
     *   <li>Server URLs for dev and prod environments</li>
     * </ul>
     * </p>
     *
     * @return the configured {@link OpenAPI} instance
     */
    @Bean
    public OpenAPI aquaTrackOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .externalDocs(buildExternalDocs())
                .servers(buildServers())
                .components(buildSecurityComponents())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    /**
     * Builds the API metadata block shown at the top of the Swagger UI.
     */
    private Info buildApiInfo() {
        return new Info()
                .title("AquaTrack API")
                .description("""
                        AquaTrack — Enterprise Water Consumption and Billing Management Platform.
                        
                        This API powers a multi-tenant SaaS application for apartment societies
                        to manage water meters, readings, billing, invoices, and alerts.
                        
                        **Authentication**: All endpoints (except /api/v1/auth/**) require a valid
                        JWT Bearer token. Obtain a token via POST /api/v1/auth/login.
                        
                        **Multi-Tenancy**: Every request is scoped to the apartment society
                        embedded in the JWT claims. Cross-tenant data access is rejected with 403.
                        """)
                .version("v1.0.0")
                .contact(new Contact()
                        .name("AquaTrack Support")
                        .email("support@aquatrack.com")
                        .url("https://aquatrack.com"))
                .license(new License()
                        .name("Proprietary")
                        .url("https://aquatrack.com/license"));
    }

    /**
     * Link to external documentation (e.g., Wiki or GitHub).
     */
    private ExternalDocumentation buildExternalDocs() {
        return new ExternalDocumentation()
                .description("AquaTrack Developer Documentation")
                .url("https://docs.aquatrack.com");
    }

    /**
     * Registers the server URLs shown in the Swagger "Servers" dropdown.
     */
    private List<Server> buildServers() {
        return List.of(
                new Server().url(serverUrl).description("Current Environment"),
                new Server().url("http://localhost:8080").description("Local Development"),
                new Server().url("https://api.aquatrack.com").description("Production")
        );
    }

    /**
     * Registers the HTTP Bearer JWT security scheme.
     *
     * <p>This makes the "Authorize" button appear in the Swagger UI,
     * allowing users to enter their JWT token and have it sent automatically
     * in the {@code Authorization: Bearer <token>} header.</p>
     */
    private Components buildSecurityComponents() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .name(BEARER_AUTH)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter your JWT access token. Obtain it from POST /api/v1/auth/login");

        return new Components().addSecuritySchemes(BEARER_AUTH, bearerScheme);
    }
}
