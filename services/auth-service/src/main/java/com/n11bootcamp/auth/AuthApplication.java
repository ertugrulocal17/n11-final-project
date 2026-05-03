package com.n11bootcamp.auth;

import com.n11bootcamp.platform.jwt.JwtResourceImportedConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.n11bootcamp.user.domain")
@EntityScan(basePackages = "com.n11bootcamp.user.domain")
@ComponentScan(
        basePackages = "com.n11bootcamp",
        excludeFilters =
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtResourceImportedConfiguration.class))
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
