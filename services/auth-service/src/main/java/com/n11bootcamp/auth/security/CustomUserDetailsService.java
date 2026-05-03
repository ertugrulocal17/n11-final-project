package com.n11bootcamp.auth.security;

import com.n11bootcamp.platform.security.UserPrincipal;
import com.n11bootcamp.user.domain.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public CustomUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return appUserRepository
                .findByEmailIgnoreCase(username)
                .map(u -> new UserPrincipal(u.getId(), u.getEmail(), u.getPasswordHash(), u.getRole()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}

