package com.kfpcl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir:uploads/catalog/}")
    private String uploadDir;

    @Autowired
    private MockAuthInterceptor mockAuthInterceptor;

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(mockAuthInterceptor).addPathPatterns("/api/v1/**");
    }

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Serve external directory using an absolute path to avoid relative‑path issues
        String absoluteLocation = java.nio.file.Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize()
                .toString();
        // Ensure trailing slash
        if (!absoluteLocation.endsWith("/")) {
            absoluteLocation = absoluteLocation + "/";
        }
        registry.addResourceHandler("/uploads/**", "/api/v1/uploads/**")
                .addResourceLocations("file:" + absoluteLocation);
    }

    // Duplicate addResourceHandlers method removed to avoid compilation error
}
