package com.n11bootcamp.auth.service;

import com.n11bootcamp.auth.dto.AuthResponse;
import com.n11bootcamp.auth.dto.LoginRequest;
import com.n11bootcamp.auth.dto.RegisterRequest;
import com.n11bootcamp.auth.jwt.AuthJwtService;
import com.n11bootcamp.common.exception.EmailAlreadyExistsException;
import com.n11bootcamp.platform.jwt.JwtProperties;
import com.n11bootcamp.user.domain.AppUser;
import com.n11bootcamp.user.domain.AppUserRepository;
import com.n11bootcamp.user.domain.Role;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthJwtService authJwtService;
    private final JwtProperties jwtProperties;

    public AuthService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            AuthJwtService authJwtService,
            JwtProperties jwtProperties) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.authJwtService = authJwtService;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        appUserRepository.save(user);
        return tokenResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        AppUser user = appUserRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        return tokenResponse(user);
    }

    private AuthResponse tokenResponse(AppUser user) {
        String token = authJwtService.createToken(user);
        long expiresInSeconds = jwtProperties.expirationMs() / 1000;
        return new AuthResponse(token, "Bearer", expiresInSeconds);
    }
}

