package com.cricklocal.controller;

import com.cricklocal.dto.AddPlayerToTeamRequest;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.Team;
import com.cricklocal.entity.TeamPlayer;
import com.cricklocal.repository.PlayerRepository;
import com.cricklocal.repository.TeamPlayerRepository;
import com.cricklocal.repository.TeamRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamPlayerController {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;

    public TeamPlayerController(
            TeamRepository teamRepository,
            PlayerRepository playerRepository,
            TeamPlayerRepository teamPlayerRepository) {

        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.teamPlayerRepository = teamPlayerRepository;
    }

    @PostMapping("/{teamId}/players/{playerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamPlayer addPlayerToTeam(
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @Valid @RequestBody AddPlayerToTeamRequest request) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Team not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Player not found"));

        if (teamPlayerRepository.findByTeamAndPlayer(team, player).isPresent()) {
            throw new IllegalArgumentException(
                    "Player is already a member of this team");
        }

        if (teamPlayerRepository.existsByTeamAndJerseyNumberAndActiveTrue(
                team, request.getJerseyNumber())) {

            throw new IllegalArgumentException(
                    "Jersey number is already assigned to an active player");
        }

        TeamPlayer teamPlayer = new TeamPlayer();

        teamPlayer.setTeam(team);
        teamPlayer.setPlayer(player);
        teamPlayer.setJerseyNumber(request.getJerseyNumber());

        return teamPlayerRepository.save(teamPlayer);
    }
    @GetMapping("/{teamId}/players")
    public List<TeamPlayer> getTeamPlayers(@PathVariable Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Team not found"));

        return teamPlayerRepository.findByTeamAndActiveTrue(team);
}
}