package com.n11bootcamp.auth.service;

import com.n11bootcamp.auth.jwt.AuthJwtService;
import com.n11bootcamp.auth.dto.AuthResponse;
import com.n11bootcamp.auth.dto.LoginRequest;
import com.n11bootcamp.auth.dto.RegisterRequest;
import com.n11bootcamp.common.exception.EmailAlreadyExistsException;
import com.n11bootcamp.platform.jwt.JwtProperties;
import com.n11bootcamp.user.domain.AppUser;
import com.n11bootcamp.user.domain.AppUserRepository;
import com.n11bootcamp.user.domain.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthJwtService authJwtService;

    private JwtProperties jwtProperties;

    private AuthService authService;

    @BeforeEach
    void jwtProps() {
        jwtProperties = new JwtProperties("test-jwt-secret-key-at-least-32-characters-long!!", 86_400_000L);
        authService = new AuthService(appUserRepository, passwordEncoder, authJwtService, jwtProperties);
    }

    @Test
    void register_persistsUserAndReturnsToken() {
        when(appUserRepository.existsByEmailIgnoreCase("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secretpass")).thenReturn("HASH");
        when(authJwtService.createToken(any(AppUser.class))).thenReturn("jwt-token");

        AuthResponse res =
                authService.register(new RegisterRequest("New@Example.com", "secretpass"));

        assertThat(res.accessToken()).isEqualTo("jwt-token");
        assertThat(res.tokenType()).isEqualTo("Bearer");
        assertThat(res.expiresInSeconds()).isEqualTo(86_400);
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void register_duplicateEmail_throws() {
        when(appUserRepository.existsByEmailIgnoreCase("x@y.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("x@y.com", "secretpass")))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void login_success_returnsToken() {
        AppUser user = new AppUser();
        user.setId(10L);
        user.setEmail("u@example.com");
        user.setPasswordHash("HASH");
        user.setRole(Role.USER);

        when(appUserRepository.findByEmailIgnoreCase("u@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("ok", "HASH")).thenReturn(true);
        when(authJwtService.createToken(user)).thenReturn("tok");

        AuthResponse res = authService.login(new LoginRequest("u@example.com", "ok"));

        assertThat(res.accessToken()).isEqualTo("tok");
        verify(passwordEncoder).matches(eq("ok"), eq("HASH"));
    }

    @Test
    void login_badPassword_throws() {
        AppUser user = new AppUser();
        user.setEmail("u@example.com");
        user.setPasswordHash("HASH");
        when(appUserRepository.findByEmailIgnoreCase("u@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("u@example.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
