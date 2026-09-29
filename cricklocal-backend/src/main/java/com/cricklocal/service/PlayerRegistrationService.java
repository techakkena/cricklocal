package com.cricklocal.service;

import com.cricklocal.dto.PlayerRegistrationUpdateRequest;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.entity.User;
import com.cricklocal.enums.PlayerRegistrationInvitationStatus;
import com.cricklocal.enums.PlayerRegistrationStatus;
import com.cricklocal.repository.PlayerRepository;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;

@Service
public class PlayerRegistrationService {

    private final PlayerRepository playerRepository;
    private final PlayerRegistrationInvitationService invitationService;

    public PlayerRegistrationService(
            PlayerRepository playerRepository,
            PlayerRegistrationInvitationService invitationService
    ) {
        this.playerRepository = playerRepository;
        this.invitationService = invitationService;
    }

    @Transactional
    public Player completeRegistration(
            String invitationToken,
            User user
    ) {
        if (user == null) {
            throw new IllegalArgumentException("User is required");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalStateException("User is inactive");
        }

        PlayerRegistrationInvitation invitation =
                invitationService.validateToken(invitationToken);

        Player player = invitation.getPlayer();

        if (player == null) {
            throw new IllegalStateException(
                    "Invitation is not associated with a player"
            );
        }

        if (player.getRegistrationStatus()
                == PlayerRegistrationStatus.REGISTERED) {

            throw new IllegalStateException(
                    "Player is already registered"
            );
        }

        if (player.getUser() != null
                && !player.getUser().getId().equals(user.getId())) {

            throw new IllegalStateException(
                    "Player is already associated with another user"
            );
        }

        Player existingPlayerForUser =
                playerRepository.findByUser(user)
                        .orElse(null);

        if (existingPlayerForUser != null
                && !existingPlayerForUser.getId().equals(player.getId())) {

            throw new IllegalStateException(
                    "User is already associated with another player"
            );
        }

        player.setUser(user);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.REGISTERED
        );

        Player savedPlayer = playerRepository.save(player);

        invitation.setStatus(
                PlayerRegistrationInvitationStatus.USED
        );
        invitation.setCompletedAt(Instant.now());

        return savedPlayer;
    }

    @Transactional
    public Player confirmAndCompleteRegistration(
                String invitationToken,
                User user,
                PlayerRegistrationUpdateRequest request
        ) {
        if (user == null) {
                throw new IllegalArgumentException("User is required");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
                throw new IllegalStateException("User is inactive");
        }

        if (request == null) {
                throw new IllegalArgumentException("Registration details are required");
        }

        PlayerRegistrationInvitation invitation =
                invitationService.validateToken(invitationToken);

        Player player = invitation.getPlayer();

        if (player == null) {
                throw new IllegalStateException(
                        "Invitation is not associated with a player"
                );
        }

        if (player.getRegistrationStatus()
                == PlayerRegistrationStatus.REGISTERED) {

                throw new IllegalStateException(
                        "Player is already registered"
                );
        }

        if (player.getUser() != null
                && !player.getUser().getId().equals(user.getId())) {

                throw new IllegalStateException(
                        "Player is already associated with another user"
                );
        }

        Player existingPlayerForUser =
                playerRepository.findByUser(user)
                        .orElse(null);

        if (existingPlayerForUser != null
                && !existingPlayerForUser.getId().equals(player.getId())) {

                throw new IllegalStateException(
                        "User is already associated with another player"
                );
        }

        player.setFirstName(request.getFirstName());
        player.setLastName(request.getLastName());
        player.setDisplayName(request.getDisplayName());
        player.setPhone(request.getPhone());
        player.setBattingStyle(request.getBattingStyle());
        player.setBowlingStyle(request.getBowlingStyle());
        player.setRole(request.getRole());

        player.setUser(user);
        player.setRegistrationStatus(
                PlayerRegistrationStatus.REGISTERED
        );

        Player savedPlayer = playerRepository.save(player);

        invitation.setStatus(
                PlayerRegistrationInvitationStatus.USED
        );
        invitation.setCompletedAt(Instant.now());

        return savedPlayer;
    }
}