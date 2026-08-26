package com.kfpcl.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI kfpclOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("KFPCL Agriculture & E-Commerce Governance Platform API")
                        .description("REST API documentation for KFPCL Admin Governance, Catalog Management, Orders, RFQs, Support, Analytics, and Logistics.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("KFPCL Platform Engineering")
                                .email("support@kfpcl.org")
                                .url("https://kfpcl.org"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://kfpcl.org/terms")))
                .servers(List.of(
                        new Server().url("https://api.kfpclexports.com").description("Production Custom Domain (SSL / HTTPS)"),
                        new Server().url("http://18.61.70.201:8080").description("Live Remote Server (AWS / Production)"),
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ));
    }
}
