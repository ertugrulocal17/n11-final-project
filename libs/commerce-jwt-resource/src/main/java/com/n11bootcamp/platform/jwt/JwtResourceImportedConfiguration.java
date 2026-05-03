package com.n11bootcamp.platform.jwt;

import com.n11bootcamp.platform.jwt.security.PlatformJwtAccessDeniedHandler;
import com.n11bootcamp.platform.jwt.security.PlatformJwtAuthenticationEntryPoint;
import com.n11bootcamp.platform.jwt.security.StatelessJwtAuthenticationFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mikroservislerde {@code @Import(JwtResourceImportedConfiguration.class)} ile kullanın.
 * Auth servisi bu sınıfı import etmez; kendi JWT filtresini kullanır.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtResourceImportedConfiguration {

    @Bean
    public JwtTokenSupport jwtTokenSupport(JwtProperties properties) {
        return new JwtTokenSupport(properties);
    }

    @Bean
    public StatelessJwtAuthenticationFilter statelessJwtAuthenticationFilter(JwtTokenSupport jwtTokenSupport) {
        return new StatelessJwtAuthenticationFilter(jwtTokenSupport);
    }

    @Bean
    public PlatformJwtAuthenticationEntryPoint platformJwtAuthenticationEntryPoint(
            tools.jackson.databind.json.JsonMapper jsonMapper) {
        return new PlatformJwtAuthenticationEntryPoint(jsonMapper);
    }

    @Bean
    public PlatformJwtAccessDeniedHandler platformJwtAccessDeniedHandler(
            tools.jackson.databind.json.JsonMapper jsonMapper) {
        return new PlatformJwtAccessDeniedHandler(jsonMapper);
    }
}
