package com.kfpcl.config;

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

    public static final String SESSION_COOKIE_AUTH = "SessionCookieAuth";
    public static final String SESSION_HEADER_AUTH = "SessionHeaderAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KFPCL B2B Marketplace - Developer 1 API")
                        .description("REST API Documentation for Developer 1 Module: Authentication, User Profiles, Seller KYC, and Account Security.\n\n" +
                                "**Authentication Mechanism:** Server-Side Session Authentication via Secure HttpOnly Cookies.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("KFPCL Engineering Team")
                                .email("dev@kfpcl.com"))
                        .license(new License().name("Proprietary").url("https://kfpcl.com")))
                .addSecurityItem(new SecurityRequirement().addList(SESSION_COOKIE_AUTH).addList(SESSION_HEADER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(SESSION_COOKIE_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("KFPCL_SESSION_ID")
                                .description("Server-side Session ID stored in secure HttpOnly cookie"))
                        .addSecuritySchemes(SESSION_HEADER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Session-Id")
                                .description("Optional fallback session header for non-browser API clients")));
    }
}
