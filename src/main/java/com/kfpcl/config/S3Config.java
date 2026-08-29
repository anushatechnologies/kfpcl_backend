package com.kfpcl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Bean
    public S3Client s3Client(org.springframework.core.env.Environment environment) {
        return S3Client.builder()
                .region(Region.of(environment.getRequiredProperty("aws.s3.region")))
                // ECS/Fargate resolves credentials from kfpcl-ecs-task-role automatically.
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}
