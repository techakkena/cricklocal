package com.cricklocal.service;

import com.cricklocal.dto.PlayerRegistrationRegenerateResponse;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.enums.PlayerRegistrationInvitationStatus;
import com.cricklocal.repository.PlayerRegistrationInvitationRepository;
import com.cricklocal.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerRegistrationRegenerateService {

    private final PlayerRegistrationInvitationRepository
            invitationRepository;

    private final PlayerRepository playerRepository;

    private final PlayerRegistrationInvitationService
            invitationService;

    public PlayerRegistrationRegenerateService(
                PlayerRegistrationInvitationRepository invitationRepository,
                PlayerRegistrationInvitationService invitationService,
                PlayerRepository playerRepository) {

        this.invitationRepository = invitationRepository;
        this.invitationService = invitationService;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public PlayerRegistrationRegenerateResponse createInvitation(
                Long playerId,
                com.cricklocal.entity.User invitedBy) {

        if (playerId == null) {
                throw new IllegalArgumentException(
                        "Player ID is required");
        }

        if (invitedBy == null) {
                throw new IllegalArgumentException(
                        "Inviting admin user is required");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Player not found"));

        if (player.getRegistrationStatus()
                == com.cricklocal.enums.PlayerRegistrationStatus.REGISTERED) {

                throw new IllegalStateException(
                        "Player is already registered");
        }

        boolean pendingInvitationExists =
                invitationRepository.findByPlayer(player)
                        .stream()
                        .anyMatch(invitation ->
                                invitation.getStatus()
                                        == PlayerRegistrationInvitationStatus.PENDING);

        if (pendingInvitationExists) {
                throw new IllegalStateException(
                        "A pending invitation already exists for player");
        }

        PlayerRegistrationInvitation invitation =
                invitationService.createInvitation(
                        player,
                        invitedBy);

        return new PlayerRegistrationRegenerateResponse(
                player.getId(),
                player.getDisplayName(),
                player.getRegistrationStatus() != null
                        ? player.getRegistrationStatus().name()
                        : null,
                invitation.getStatus().name(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt(),
                invitation.getToken()
        );
    }

    @Transactional
    public PlayerRegistrationRegenerateResponse regenerateInvitation(
            Long playerId,
            com.cricklocal.entity.User invitedBy) {

        if (playerId == null) {
            throw new IllegalArgumentException(
                    "Player ID is required");
        }

        if (invitedBy == null) {
            throw new IllegalArgumentException(
                    "Inviting admin user is required");
        }

        PlayerRegistrationInvitation latestInvitation =
                invitationRepository
                        .findByPlayer(
                                findPlayerFromInvitation(playerId))
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

        Player player = latestInvitation.getPlayer();

        if (player.getRegistrationStatus()
                == com.cricklocal.enums.PlayerRegistrationStatus.REGISTERED) {

            throw new IllegalStateException(
                    "Player is already registered");
        }

        if (latestInvitation.getStatus()
                == PlayerRegistrationInvitationStatus.USED) {

            throw new IllegalStateException(
                    "Invitation has already been used");
        }

        PlayerRegistrationInvitation newInvitation =
                invitationService.regenerateInvitation(
                        latestInvitation,
                        invitedBy);

        return new PlayerRegistrationRegenerateResponse(
                player.getId(),
                player.getDisplayName(),
                player.getRegistrationStatus() != null
                        ? player.getRegistrationStatus().name()
                        : null,
                newInvitation.getStatus().name(),
                newInvitation.getCreatedAt(),
                newInvitation.getExpiresAt(),
                newInvitation.getToken()
        );
    }

    private Player findPlayerFromInvitation(Long playerId) {
        return invitationRepository.findAll()
                .stream()
                .map(PlayerRegistrationInvitation::getPlayer)
                .filter(player ->
                        player != null
                                && player.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Player not found"));
    }
}