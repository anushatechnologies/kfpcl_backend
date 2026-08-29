package com.kfpcl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.kfpcl", "com.payment"})
@EntityScan({"com.kfpcl.entity", "com.payment.entity"})
@EnableJpaRepositories({"com.kfpcl.repository", "com.payment.repository"})
public class KfpclBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KfpclBackendApplication.class, args);
    }

}