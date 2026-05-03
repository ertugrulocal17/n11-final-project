package com.n11bootcamp.config;

import com.n11bootcamp.platform.jwt.security.PlatformJwtAccessDeniedHandler;
import com.n11bootcamp.platform.jwt.security.PlatformJwtAuthenticationEntryPoint;
import com.n11bootcamp.platform.jwt.security.StatelessJwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class PaymentSecurityConfig {

    private final StatelessJwtAuthenticationFilter jwtAuthenticationFilter;
    private final PlatformJwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final PlatformJwtAccessDeniedHandler jwtAccessDeniedHandler;

    public PaymentSecurityConfig(
            StatelessJwtAuthenticationFilter jwtAuthenticationFilter,
            PlatformJwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            PlatformJwtAccessDeniedHandler jwtAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .anonymous(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth.requestMatchers(HttpMethod.POST, "/api/v1/payments/iyzico/callback")
                        .permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/actuator/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/error")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

