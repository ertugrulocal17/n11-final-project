package com.n11bootcamp.config;

import com.n11bootcamp.common.internal.InternalServiceAuthFilter;
import com.n11bootcamp.common.internal.InternalServiceProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class InternalFilterConfig {

    @Bean
    public FilterRegistrationBean<InternalServiceAuthFilter> internalServiceAuthFilter(
            InternalServiceProperties properties) {
        var reg = new FilterRegistrationBean<>(new InternalServiceAuthFilter(properties));
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);
        reg.addUrlPatterns("/internal/*");
        return reg;
    }
}
