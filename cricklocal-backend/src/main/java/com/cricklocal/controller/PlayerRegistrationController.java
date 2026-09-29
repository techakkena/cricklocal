package com.cricklocal.controller;

import com.cricklocal.dto.PlayerRegistrationInvitationResponse;
import com.cricklocal.dto.PlayerRegistrationUpdateRequest;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.entity.User;
import com.cricklocal.service.AuthenticationService;
import com.cricklocal.service.PlayerRegistrationInvitationService;
import com.cricklocal.service.PlayerRegistrationService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player-registration")
public class PlayerRegistrationController {

    private final PlayerRegistrationInvitationService invitationService;
    private final PlayerRegistrationService registrationService;
    private final AuthenticationService authenticationService;

    public PlayerRegistrationController(
            PlayerRegistrationInvitationService invitationService,
            PlayerRegistrationService registrationService,
            AuthenticationService authenticationService
    ) {
        this.invitationService = invitationService;
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/invitations/{token}")
    public ResponseEntity<PlayerRegistrationInvitationResponse> getInvitation(
            @PathVariable String token
    ) {
        PlayerRegistrationInvitation invitation =
                invitationService.validateToken(token);

        Player player = invitation.getPlayer();

        PlayerRegistrationInvitationResponse response =
                new PlayerRegistrationInvitationResponse(
                        player.getId(),
                        player.getDisplayName(),
                        player.getFirstName(),
                        player.getLastName(),
                        player.getBattingStyle() != null
                                ? player.getBattingStyle().name()
                                : null,
                        player.getBowlingStyle() != null
                                ? player.getBowlingStyle().name()
                                : null,
                        player.getRole() != null
                                ? player.getRole().name()
                                : null,
                        player.getRegistrationStatus() != null
                                ? player.getRegistrationStatus().name()
                                : null
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitations/{token}/complete")
    public ResponseEntity<PlayerRegistrationInvitationResponse> completeRegistration(
            @PathVariable String token,
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            return ResponseEntity.status(401).build();
        }

        User user =
                authenticationService.getUserForGoogleLogin(oauth2User);

        Player player =
                registrationService.completeRegistration(
                        token,
                        user
                );

        PlayerRegistrationInvitationResponse response =
                new PlayerRegistrationInvitationResponse(
                        player.getId(),
                        player.getDisplayName(),
                        player.getFirstName(),
                        player.getLastName(),
                        player.getBattingStyle() != null
                                ? player.getBattingStyle().name()
                                : null,
                        player.getBowlingStyle() != null
                                ? player.getBowlingStyle().name()
                                : null,
                        player.getRole() != null
                                ? player.getRole().name()
                                : null,
                        player.getRegistrationStatus() != null
                                ? player.getRegistrationStatus().name()
                                : null
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/invitations/{token}/confirm")
    public ResponseEntity<PlayerRegistrationInvitationResponse> confirmRegistration(
                @PathVariable String token,
                @Valid @RequestBody PlayerRegistrationUpdateRequest request,
                Authentication authentication
        ) {
        if (authentication == null
                || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).build();
        }

        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
                return ResponseEntity.status(401).build();
        }

        User user =
                authenticationService.getUserForGoogleLogin(oauth2User);

        Player player =
                registrationService.confirmAndCompleteRegistration(
                        token,
                        user,
                        request
                );

        PlayerRegistrationInvitationResponse response =
                new PlayerRegistrationInvitationResponse(
                        player.getId(),
                        player.getDisplayName(),
                        player.getFirstName(),
                        player.getLastName(),
                        player.getBattingStyle() != null
                                ? player.getBattingStyle().name()
                                : null,
                        player.getBowlingStyle() != null
                                ? player.getBowlingStyle().name()
                                : null,
                        player.getRole() != null
                                ? player.getRole().name()
                                : null,
                        player.getRegistrationStatus() != null
                                ? player.getRegistrationStatus().name()
                                : null
                );

        return ResponseEntity.ok(response);
    }
}