package com.anil.crm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        Info apiInfo = new Info()
                .title("CRM API")
                .version("v1.0")
                .description("Anıl CRM projesi için API dokümantasyonu.");

        SecurityScheme securityScheme = new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .description("JWT Authorization header using the Bearer scheme. " +
                        "Giriş yapmak için 'api/auth/login' endpoint'inden token alın.");


        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(securitySchemeName);

        return new OpenAPI()
                .info(apiInfo)
                .addSecurityItem(securityRequirement)
                .components(
                        new Components().addSecuritySchemes(securitySchemeName, securityScheme)
                )
                .addServersItem(new Server().url("/")
                        .description("Default Server URL"));
    }
}