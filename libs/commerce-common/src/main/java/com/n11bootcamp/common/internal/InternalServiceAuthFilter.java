package com.n11bootcamp.common.internal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class InternalServiceAuthFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Service-Token";
    private final InternalServiceProperties properties;

    public InternalServiceAuthFilter(InternalServiceProperties properties) {
        this.properties = properties;
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/internal/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!properties.matchesHeader(request.getHeader(HEADER))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Invalid or missing service token");
            return;
        }
        filterChain.doFilter(request, response);
    }


}
