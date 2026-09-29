package com.cricklocal.service;

import com.cricklocal.dto.PlayerRegistrationLinkResponse;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.enums.PlayerRegistrationInvitationStatus;
import com.cricklocal.enums.PlayerRegistrationStatus;
import com.cricklocal.repository.PlayerRegistrationInvitationRepository;
import com.cricklocal.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerRegistrationLinkService {

    private static final String REGISTRATION_BASE_URL =
            "http://localhost:5173/player-registration/";

    private final PlayerRepository playerRepository;
    private final PlayerRegistrationInvitationRepository invitationRepository;

    public PlayerRegistrationLinkService(
            PlayerRepository playerRepository,
            PlayerRegistrationInvitationRepository invitationRepository) {

        this.playerRepository = playerRepository;
        this.invitationRepository = invitationRepository;
    }

    @Transactional(readOnly = true)
    public PlayerRegistrationLinkResponse getRegistrationLink(
            Long playerId) {

        if (playerId == null) {
            throw new IllegalArgumentException(
                    "Player ID is required");
        }

        Player player =
                playerRepository.findById(playerId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Player not found"));

        if (player.getRegistrationStatus()
                == PlayerRegistrationStatus.REGISTERED) {

            throw new IllegalStateException(
                    "Player is already registered");
        }

        PlayerRegistrationInvitation invitation =
                invitationRepository.findByPlayer(player)
                        .stream()
                        .max((first, second) -> {

                            int createdAtComparison =
                                    first.getCreatedAt()
                                            .compareTo(
                                                    second.getCreatedAt());

                            if (createdAtComparison != 0) {
                                return createdAtComparison;
                            }

                            return Long.compare(
                                    first.getId(),
                                    second.getId());
                        })
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "No invitation exists for player"));

        if (invitation.getStatus()
                != PlayerRegistrationInvitationStatus.PENDING) {

            throw new IllegalStateException(
                    "Registration invitation is not active");
        }

        if (invitation.getExpiresAt() == null
                || !invitation.getExpiresAt()
                        .isAfter(java.time.Instant.now())) {

            throw new IllegalStateException(
                    "Registration invitation has expired");
        }

        return new PlayerRegistrationLinkResponse(
                player.getId(),
                player.getDisplayName(),
                player.getRegistrationStatus() != null
                        ? player.getRegistrationStatus().name()
                        : null,
                invitation.getStatus().name(),
                REGISTRATION_BASE_URL + invitation.getToken(),
                invitation.getExpiresAt()
        );
    }
}