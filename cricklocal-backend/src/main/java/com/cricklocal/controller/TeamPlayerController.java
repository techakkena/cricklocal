package com.cricklocal.controller;

import com.cricklocal.dto.AddPlayerToTeamRequest;
import com.cricklocal.dto.TeamCaptainResponse;
import com.cricklocal.dto.TeamRosterPlayerResponse;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.Team;
import com.cricklocal.entity.TeamPlayer;
import com.cricklocal.repository.PlayerRepository;
import com.cricklocal.repository.TeamPlayerRepository;
import com.cricklocal.repository.TeamRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
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
    public TeamRosterPlayerResponse addPlayerToTeam(
            @PathVariable Long teamId,
            @PathVariable Long playerId,
            @Valid @RequestBody AddPlayerToTeamRequest request) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        if (teamPlayerRepository.findByTeamAndPlayer(team, player).isPresent()) {
            throw new IllegalArgumentException("Player is already a member of this team");
        }

        if (teamPlayerRepository.existsActiveMembershipInAnotherTeamOfSameSeries(
                        player,
                        team)) {
                throw new IllegalArgumentException(
                        "Player is already assigned to another team in this series");
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

        TeamPlayer savedTeamPlayer = teamPlayerRepository.save(teamPlayer);

        return toTeamRosterPlayerResponse(savedTeamPlayer);
    }

    @GetMapping("/{teamId}/players")
    public List<TeamRosterPlayerResponse> getTeamPlayers(@PathVariable Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        return teamPlayerRepository.findByTeamAndActiveTrue(team)
                .stream()
                .map(this::toTeamRosterPlayerResponse)
                .toList();
    }

    @GetMapping("/{teamId}/captain")
    public TeamCaptainResponse getCaptain(@PathVariable Long teamId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        TeamPlayer captain = team.getCaptain();

        if (captain == null) {
                throw new IllegalArgumentException("Team captain is not assigned");
        }

        TeamCaptainResponse response = new TeamCaptainResponse();
        response.setTeamId(team.getId());
        response.setTeamName(team.getName());
        response.setShortName(team.getShortName());
        response.setTeamPlayerId(captain.getId());
        response.setPlayerId(captain.getPlayer().getId());
        response.setDisplayName(captain.getPlayer().getDisplayName());
        response.setJerseyNumber(captain.getJerseyNumber());
        response.setActive(captain.getActive());

        return response;
    }

    @DeleteMapping("/{teamId}/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayerFromTeam(
                @PathVariable Long teamId,
                @PathVariable Long playerId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        TeamPlayer teamPlayer = teamPlayerRepository
                .findByTeamAndPlayer(team, player)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Player is not a member of this team"));

        if (!Boolean.TRUE.equals(teamPlayer.getActive())) {
                throw new IllegalArgumentException(
                        "Player is already inactive in this team");
        }

        if (team.getCaptain() != null
                && team.getCaptain().getId().equals(teamPlayer.getId())) {
                team.setCaptain(null);
                teamRepository.save(team);
        }

        teamPlayer.setActive(false);
        teamPlayer.setLeftAt(Instant.now());
        teamPlayerRepository.save(teamPlayer);
    }

    @PutMapping("/{teamId}/captain/{playerId}")
    public TeamCaptainResponse setCaptain(
            @PathVariable Long teamId,
            @PathVariable Long playerId) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        TeamPlayer teamPlayer = teamPlayerRepository
                .findByTeamAndPlayer(team, player)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Player is not a member of this team"));

        if (!Boolean.TRUE.equals(teamPlayer.getActive())) {
            throw new IllegalArgumentException(
                    "Captain must be an active player of this team");
        }

        team.setCaptain(teamPlayer);
        teamRepository.save(team);

        TeamCaptainResponse response = new TeamCaptainResponse();
        response.setTeamId(team.getId());
        response.setTeamName(team.getName());
        response.setShortName(team.getShortName());
        response.setTeamPlayerId(teamPlayer.getId());
        response.setPlayerId(player.getId());
        response.setDisplayName(player.getDisplayName());
        response.setJerseyNumber(teamPlayer.getJerseyNumber());
        response.setActive(teamPlayer.getActive());

        return response;
    }

    private TeamRosterPlayerResponse toTeamRosterPlayerResponse(
            TeamPlayer teamPlayer) {

        TeamRosterPlayerResponse response = new TeamRosterPlayerResponse();

        response.setTeamPlayerId(teamPlayer.getId());
        response.setPlayerId(teamPlayer.getPlayer().getId());
        response.setDisplayName(teamPlayer.getPlayer().getDisplayName());
        response.setJerseyNumber(teamPlayer.getJerseyNumber());
        response.setJoinedAt(teamPlayer.getJoinedAt());
        response.setLeftAt(teamPlayer.getLeftAt());
        response.setActive(teamPlayer.getActive());

        return response;
    }
}
