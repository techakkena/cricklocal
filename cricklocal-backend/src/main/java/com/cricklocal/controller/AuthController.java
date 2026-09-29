package com.cricklocal.controller;

import com.cricklocal.entity.Player;
import com.cricklocal.entity.User;
import com.cricklocal.repository.PlayerRepository;
import com.cricklocal.service.AdminAccessService;
import com.cricklocal.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthenticationService authenticationService;
    private final PlayerRepository playerRepository;
    private final AdminAccessService adminAccessService;

    public AuthController(
            AuthenticationService authenticationService,
            PlayerRepository playerRepository,
            AdminAccessService adminAccessService
    ) {
        this.authenticationService = authenticationService;
        this.playerRepository = playerRepository;
        this.adminAccessService = adminAccessService;
    }

    @GetMapping("/api/auth/me")
    public ResponseEntity<AuthUserResponse> currentUser(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            return ResponseEntity.status(401).build();
        }

        User user =
                authenticationService.getUserForGoogleLogin(oauth2User);

        Player player =
                playerRepository.findByUser(user).orElse(null);

        boolean hasPlayerProfile = player != null;

        boolean hasActiveAdminAccess =
                adminAccessService.hasActiveAdminAccess(user);

        PlayerProfileResponse playerProfile =
                new PlayerProfileResponse(
                        hasPlayerProfile,
                        hasPlayerProfile ? player.getId() : null,
                        hasPlayerProfile
                                && player.getRegistrationStatus() != null
                                ? player.getRegistrationStatus().name()
                                : null
                );

        AdminAccessResponse adminAccess =
                new AdminAccessResponse(hasActiveAdminAccess);

        return ResponseEntity.ok(
                new AuthUserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getRole().name(),
                        user.getActive(),
                        playerProfile,
                        adminAccess
                )
        );
    }

    public record AuthUserResponse(
            Long id,
            String email,
            String displayName,
            String role,
            Boolean active,
            PlayerProfileResponse playerProfile,
            AdminAccessResponse adminAccess
    ) {
    }

    public record PlayerProfileResponse(
            Boolean exists,
            Long playerId,
            String registrationStatus
    ) {
    }

    public record AdminAccessResponse(
            Boolean active
    ) {
    }
}