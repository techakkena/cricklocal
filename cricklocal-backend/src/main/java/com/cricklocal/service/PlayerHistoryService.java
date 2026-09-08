package com.cricklocal.service;

import com.cricklocal.dto.PlayerMatchHistoryResponse;
import com.cricklocal.entity.MatchLineup;
import com.cricklocal.entity.Player;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.MatchLineupRepository;
import com.cricklocal.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerHistoryService {

    private final PlayerRepository playerRepository;
    private final MatchLineupRepository matchLineupRepository;

    public PlayerHistoryService(
            PlayerRepository playerRepository,
            MatchLineupRepository matchLineupRepository) {

        this.playerRepository = playerRepository;
        this.matchLineupRepository = matchLineupRepository;
    }

    @Transactional(readOnly = true)
    public List<PlayerMatchHistoryResponse> getPlayerMatchHistory(
            Long playerId) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Player not found: " + playerId));

        return matchLineupRepository
                .findPlayerMatchHistory(player)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PlayerMatchHistoryResponse toResponse(
            MatchLineup lineup) {

        PlayerMatchHistoryResponse response =
                new PlayerMatchHistoryResponse();

        response.setMatchId(
                lineup.getMatch().getId());

        response.setMatchName(
                lineup.getMatch().getName());

        response.setMatchDate(
                lineup.getMatch().getScheduledAt());

        response.setMatchStatus(
                lineup.getMatch().getStatus());

        response.setTeamId(
                lineup.getTeam().getId());

        response.setTeamName(
                lineup.getTeam().getName());

        response.setPlaying(
                lineup.getPlaying());

        return response;
    }
}