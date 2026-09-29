package com.cricklocal.service;

import com.cricklocal.entity.Player;
import com.cricklocal.entity.User;
import com.cricklocal.enums.PlayerRegistrationStatus;
import com.cricklocal.repository.PlayerRegistrationInvitationRepository;
import com.cricklocal.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerRegistrationInvitationBackfillService {

    private final PlayerRepository playerRepository;
    private final PlayerRegistrationInvitationRepository invitationRepository;
    private final PlayerRegistrationInvitationService invitationService;

    public PlayerRegistrationInvitationBackfillService(
            PlayerRepository playerRepository,
            PlayerRegistrationInvitationRepository invitationRepository,
            PlayerRegistrationInvitationService invitationService) {

        this.playerRepository = playerRepository;
        this.invitationRepository = invitationRepository;
        this.invitationService = invitationService;
    }

    @Transactional
    public PlayerRegistrationInvitationBackfillResponse backfill(
            User invitedBy) {

        if (invitedBy == null) {
            throw new IllegalArgumentException(
                    "Inviting admin user is required");
        }

        List<Player> players = playerRepository.findAll();

        int created = 0;
        int skipped = 0;

        for (Player player : players) {

            if (player.getRegistrationStatus()
                    != PlayerRegistrationStatus.PENDING) {
                skipped++;
                continue;
            }

            if (!invitationRepository
                    .findByPlayer(player)
                    .isEmpty()) {
                skipped++;
                continue;
            }

            invitationService.createInvitation(
                    player,
                    invitedBy);

            created++;
        }

        return new PlayerRegistrationInvitationBackfillResponse(
                players.size(),
                created,
                skipped
        );
    }

    public record PlayerRegistrationInvitationBackfillResponse(
            int totalPlayers,
            int invitationsCreated,
            int playersSkipped
    ) {
    }
}