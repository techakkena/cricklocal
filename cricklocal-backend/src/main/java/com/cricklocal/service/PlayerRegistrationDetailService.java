package com.cricklocal.service;

import com.cricklocal.dto.PlayerRegistrationDetailResponse;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.repository.PlayerRegistrationInvitationRepository;
import com.cricklocal.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerRegistrationDetailService {

    private final PlayerRepository playerRepository;
    private final PlayerRegistrationInvitationRepository
            invitationRepository;

    public PlayerRegistrationDetailService(
            PlayerRepository playerRepository,
            PlayerRegistrationInvitationRepository invitationRepository) {

        this.playerRepository = playerRepository;
        this.invitationRepository = invitationRepository;
    }

    @Transactional(readOnly = true)
    public PlayerRegistrationDetailResponse getRegistrationDetail(
            Long playerId) {

        if (playerId == null) {
            throw new IllegalArgumentException(
                    "Player ID is required");
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Player not found"));

        PlayerRegistrationInvitation invitation =
                invitationRepository
                        .findByPlayer(player)
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
                        .orElse(null);

        return new PlayerRegistrationDetailResponse(
                player.getId(),
                player.getDisplayName(),
                player.getFirstName(),
                player.getLastName(),
                player.getPhone(),
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
                        : null,
                player.getUser() != null
                        ? player.getUser().getId()
                        : null,
                player.getUser() != null
                        ? player.getUser().getEmail()
                        : null,
                player.getUser() != null
                        ? player.getUser().getDisplayName()
                        : null,
                invitation != null
                        ? invitation.getStatus().name()
                        : null,
                invitation != null
                        ? invitation.getCreatedAt()
                        : null,
                invitation != null
                        ? invitation.getExpiresAt()
                        : null,
                invitation != null
                        ? invitation.getCompletedAt()
                        : null
        );
    }
}