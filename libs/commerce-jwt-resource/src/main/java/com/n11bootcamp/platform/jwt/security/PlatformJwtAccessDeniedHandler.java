package com.n11bootcamp.platform.jwt.security;

import com.n11bootcamp.common.exception.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.Instant;

public class PlatformJwtAccessDeniedHandler implements AccessDeniedHandler {
    private final JsonMapper jsonMapper;

    public PlatformJwtAccessDeniedHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiError body = new ApiError(
                Instant.now(),
                403,
                "Forbidden",
                accessDeniedException.getMessage() != null ? accessDeniedException.getMessage() : "Access Denided", request.getRequestURI());
        jsonMapper.writeValue(response.getOutputStream(), body);

    }

}
