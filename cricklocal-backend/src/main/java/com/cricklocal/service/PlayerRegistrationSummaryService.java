package com.cricklocal.service;

import com.cricklocal.dto.PlayerRegistrationSummaryResponse;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.enums.PlayerRegistrationInvitationStatus;
import com.cricklocal.enums.PlayerRegistrationStatus;
import com.cricklocal.repository.PlayerRegistrationInvitationRepository;
import com.cricklocal.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerRegistrationSummaryService {

    private final PlayerRepository playerRepository;
    private final PlayerRegistrationInvitationRepository invitationRepository;

    public PlayerRegistrationSummaryService(
            PlayerRepository playerRepository,
            PlayerRegistrationInvitationRepository invitationRepository) {

        this.playerRepository = playerRepository;
        this.invitationRepository = invitationRepository;
    }

    @Transactional(readOnly = true)
    public PlayerRegistrationSummaryResponse getSummary() {

        List<Player> players = playerRepository.findAll();

        long totalPlayers = players.size();

        long registeredPlayers = players.stream()
                .filter(player ->
                        player.getRegistrationStatus()
                                == PlayerRegistrationStatus.REGISTERED)
                .count();

        long pendingPlayers = players.stream()
                .filter(player ->
                        player.getRegistrationStatus()
                                == PlayerRegistrationStatus.PENDING)
                .count();

        List<PlayerRegistrationInvitation> invitations =
                invitationRepository.findAll();

        long expiredInvitations = invitations.stream()
                .filter(invitation ->
                        invitation.getStatus()
                                == PlayerRegistrationInvitationStatus.EXPIRED)
                .count();

        return new PlayerRegistrationSummaryResponse(
                totalPlayers,
                registeredPlayers,
                pendingPlayers,
                expiredInvitations
        );
    }
}