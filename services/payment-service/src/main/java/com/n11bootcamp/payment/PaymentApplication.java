package com.n11bootcamp.payment;

import com.n11bootcamp.payment.config.IyzicoProperties;
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
@EnableConfigurationProperties({IyzicoProperties.class, PaymentCommerceProperties.class})
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }
}
