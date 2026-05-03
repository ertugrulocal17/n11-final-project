package com.n11bootcamp.auth;

import com.n11bootcamp.platform.jwt.JwtProperties;
import com.n11bootcamp.platform.jwt.JwtTokenSupport;
import com.n11bootcamp.platform.jwt.security.PlatformJwtAccessDeniedHandler;
import com.n11bootcamp.platform.jwt.security.PlatformJwtAuthenticationEntryPoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AuthJwtConfiguration {

    @Bean
    public JwtTokenSupport jwtTokenSupport(JwtProperties properties) {
        return new JwtTokenSupport(properties);
    }

    @Bean
    public PlatformJwtAuthenticationEntryPoint platformJwtAuthenticationEntryPoint(JsonMapper jsonMapper) {
        return new PlatformJwtAuthenticationEntryPoint(jsonMapper);
    }

    @Bean
    public PlatformJwtAccessDeniedHandler platformJwtAccessDeniedHandler(JsonMapper jsonMapper) {
        return new PlatformJwtAccessDeniedHandler(jsonMapper);
    }
}