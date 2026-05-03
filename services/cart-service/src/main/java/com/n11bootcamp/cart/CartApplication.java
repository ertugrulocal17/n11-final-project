package com.n11bootcamp.cart;

import com.n11bootcamp.common.internal.InternalServiceProperties;
import com.n11bootcamp.platform.jwt.JwtResourceImportedConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Hello world!
 *
 */
@SpringBootApplication(scanBasePackages = "com.n11bootcamp")
@Import(JwtResourceImportedConfiguration.class)
@EnableConfigurationProperties({InternalServiceProperties.class, CommerceServicesProperties.class})
public class CartApplication {

    public static void main(String[] args) {
        SpringApplication.run(CartApplication.class, args);
    }
}
