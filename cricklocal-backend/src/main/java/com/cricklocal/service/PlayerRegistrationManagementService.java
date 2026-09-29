package com.cricklocal.service;

import com.cricklocal.dto.PlayerRegistrationManagementResponse;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.repository.PlayerRegistrationInvitationRepository;
import com.cricklocal.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlayerRegistrationManagementService {

    private final PlayerRepository playerRepository;
    private final PlayerRegistrationInvitationRepository invitationRepository;

    public PlayerRegistrationManagementService(
            PlayerRepository playerRepository,
            PlayerRegistrationInvitationRepository invitationRepository) {

        this.playerRepository = playerRepository;
        this.invitationRepository = invitationRepository;
    }

    @Transactional(readOnly = true)
    public List<PlayerRegistrationManagementResponse> getRegistrations() {

        List<Player> players = playerRepository.findAll();

        if (players.isEmpty()) {
            return List.of();
        }

        List<PlayerRegistrationInvitation> invitations =
                invitationRepository.findByPlayerIn(players);

        Map<Long, PlayerRegistrationInvitation> latestInvitations =
                new HashMap<>();

        for (PlayerRegistrationInvitation invitation : invitations) {
            Long playerId = invitation.getPlayer().getId();

            PlayerRegistrationInvitation existing =
                    latestInvitations.get(playerId);

            if (existing == null
                    || invitation.getCreatedAt()
                            .isAfter(existing.getCreatedAt())
                    || (invitation.getCreatedAt()
                            .equals(existing.getCreatedAt())
                        && invitation.getId() > existing.getId())) {

                latestInvitations.put(playerId, invitation);
            }
        }

        return players.stream()
                .map(player -> {
                    PlayerRegistrationInvitation invitation =
                            latestInvitations.get(player.getId());

                    return new PlayerRegistrationManagementResponse(
                            player.getId(),
                            player.getDisplayName(),
                            player.getFirstName(),
                            player.getLastName(),
                            player.getRegistrationStatus() != null
                                    ? player.getRegistrationStatus().name()
                                    : null,
                            invitation != null
                                    ? invitation.getStatus().name()
                                    : null,
                            invitation != null
                                    ? invitation.getCreatedAt()
                                    : null,
                            invitation != null
                                    ? invitation.getExpiresAt()
                                    : null
                    );
                })
                .toList();
    }
}