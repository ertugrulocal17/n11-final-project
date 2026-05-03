package com.n11bootcamp.auth.web;

import com.n11bootcamp.auth.dto.UserProfileResponse;
import com.n11bootcamp.platform.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Account", description = "Oturum açmış kullanıcı")
public class MeController {

    @GetMapping("/me")
    @SecurityRequirement(name = "bearer-jwt")
    @Operation(summary = "Mevcut kullanıcı profili")
    public UserProfileResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return new UserProfileResponse(principal.getId(), principal.getEmail(), principal.getRole().name());
    }
}
