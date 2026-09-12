
package com.cricklocal.service;

import com.cricklocal.dto.CreatePlayerRequest;
import com.cricklocal.dto.PlayerResponse;
import com.cricklocal.dto.PlayerTeamResponse;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.TeamPlayer;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.PlayerRepository;
import com.cricklocal.repository.TeamPlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;

    public PlayerService(
            PlayerRepository playerRepository,
            TeamPlayerRepository teamPlayerRepository) {
        this.playerRepository = playerRepository;
        this.teamPlayerRepository = teamPlayerRepository;
    }

    @Transactional
    public PlayerResponse createPlayer(CreatePlayerRequest request) {

        Player player = new Player();

        player.setFirstName(request.getFirstName());
        player.setLastName(request.getLastName());
        player.setDisplayName(request.getDisplayName());
        player.setPhone(request.getPhone());
        player.setBattingStyle(request.getBattingStyle());
        player.setBowlingStyle(request.getBowlingStyle());
        player.setRole(request.getRole());

        Player savedPlayer = playerRepository.save(player);

        return toPlayerResponse(savedPlayer);
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> getAllPlayers() {

        return playerRepository.findAll()
                .stream()
                .map(this::toPlayerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlayerResponse getPlayerById(Long playerId) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Player not found: " + playerId));

        return toPlayerResponse(player);
    }

    private PlayerResponse toPlayerResponse(Player player) {

        PlayerResponse response = new PlayerResponse();

        response.setId(player.getId());
        response.setFirstName(player.getFirstName());
        response.setLastName(player.getLastName());
        response.setDisplayName(player.getDisplayName());
        response.setPhone(player.getPhone());
        response.setBattingStyle(player.getBattingStyle());
        response.setBowlingStyle(player.getBowlingStyle());
        response.setRole(player.getRole());
        response.setActive(player.getActive());
        response.setCreatedAt(player.getCreatedAt());

        List<PlayerTeamResponse> teams = teamPlayerRepository
                .findByPlayerAndActiveTrue(player)
                .stream()
                .map(this::toPlayerTeamResponse)
                .toList();

        response.setTeams(teams);

        return response;
    }

    private PlayerTeamResponse toPlayerTeamResponse(
            TeamPlayer teamPlayer) {

        PlayerTeamResponse response = new PlayerTeamResponse();

        response.setTeamId(teamPlayer.getTeam().getId());
        response.setTeamName(teamPlayer.getTeam().getName());
        response.setShortName(teamPlayer.getTeam().getShortName());
        response.setJerseyNumber(teamPlayer.getJerseyNumber());
        response.setJoinedAt(teamPlayer.getJoinedAt());
        response.setLeftAt(teamPlayer.getLeftAt());

        return response;
    }
}