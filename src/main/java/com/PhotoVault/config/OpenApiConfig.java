package com.PhotoVault.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PhotoVault API")
                        .version("1.0")
                        .description("RESTful API for photo management – PhotoVault allows photographers to securely organize, share, and manage their photos.")
                        .contact(new Contact()
                                .name("Vinicius Tertuliano da Silva")
                                .email("viniciustertulianodasilva@hotmail.com")
                                .url("https://github.com/Viniciustertuliano")
                        )
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/license/mit/")
                        )
                )
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insert the JWT token obtained from the login endpoint.")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}
