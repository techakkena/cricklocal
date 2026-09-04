package com.cricklocal.service;

import com.cricklocal.dto.AddPlayerToMatchRequest;
import com.cricklocal.dto.MatchLineupResponse;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchLineup;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.Team;
import com.cricklocal.entity.TeamPlayer;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.MatchLineupRepository;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.MatchTeamRepository;
import com.cricklocal.repository.PlayerRepository;
import com.cricklocal.repository.TeamPlayerRepository;
import com.cricklocal.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MatchLineupService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final MatchLineupRepository matchLineupRepository;

    public MatchLineupService(
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            PlayerRepository playerRepository,
            MatchTeamRepository matchTeamRepository,
            TeamPlayerRepository teamPlayerRepository,
            MatchLineupRepository matchLineupRepository) {

        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.matchTeamRepository = matchTeamRepository;
        this.teamPlayerRepository = teamPlayerRepository;
        this.matchLineupRepository = matchLineupRepository;
    }

    @Transactional
    public MatchLineupResponse addPlayerToMatch(
            Long matchId,
            AddPlayerToMatchRequest request) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found"));

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Team not found"));

        Player player = playerRepository.findById(request.getPlayerId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Player not found"));

        // 1. Team must belong to this match.
        boolean teamBelongsToMatch =
                matchTeamRepository
                        .findByMatchAndTeam(match, team)
                        .isPresent();

        if (!teamBelongsToMatch) {
            throw new IllegalArgumentException(
                    "Team does not belong to this match");
        }

        // 2. Player must belong to this team.
        TeamPlayer teamPlayer =
                teamPlayerRepository
                        .findByTeamAndPlayer(team, player)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Player does not belong to this team"));

        // 3. Player must be an active team member.
        if (!teamPlayer.getActive()) {
            throw new IllegalArgumentException(
                    "Player is not an active member of this team");
        }

        // 4. Player cannot be added twice to the same match.
        if (matchLineupRepository
                .existsByMatchAndPlayer(match, player)) {

            throw new IllegalArgumentException(
                    "Player is already in this match lineup");
        }

        // 5. Jersey number must be unique within the team
        // for this particular match.
        if (matchLineupRepository
                .existsByMatchAndTeamAndJerseyNumber(
                        match,
                        team,
                        teamPlayer.getJerseyNumber())) {

            throw new IllegalArgumentException(
                    "Jersey number is already used in this match lineup");
        }

        // 6. Only one captain is allowed per team in a match.
        if (Boolean.TRUE.equals(request.getCaptain())
                && matchLineupRepository
                        .existsByMatchAndTeamAndCaptainTrue(
                                match,
                                team)) {

            throw new IllegalArgumentException(
                    "A captain is already assigned for this team in this match");
        }

        // 7. Only one wicketkeeper is allowed per team in a match.
        if (Boolean.TRUE.equals(request.getWicketKeeper())
                && matchLineupRepository
                        .existsByMatchAndTeamAndWicketKeeperTrue(
                                match,
                                team)) {

            throw new IllegalArgumentException(
                    "A wicketkeeper is already assigned for this team in this match");
        }

        // 8. Respect the maximum squad size configured for the match.
        long currentTeamPlayers =
                matchLineupRepository
                        .findByMatchAndTeam(match, team)
                        .size();

        if (currentTeamPlayers >= match.getMaxPlayersPerTeam()) {
            throw new IllegalArgumentException(
                    "Maximum players per team has been reached for this match");
        }

        MatchLineup lineup = new MatchLineup();

        lineup.setMatch(match);
        lineup.setTeam(team);
        lineup.setPlayer(player);
        lineup.setJerseyNumber(teamPlayer.getJerseyNumber());
        lineup.setPlaying(request.getPlaying());
        lineup.setCaptain(Boolean.TRUE.equals(request.getCaptain()));
        lineup.setWicketKeeper(
                Boolean.TRUE.equals(request.getWicketKeeper()));

        MatchLineup savedLineup =
                matchLineupRepository.save(lineup);

        return toResponse(savedLineup);
    }

    @Transactional(readOnly = true)
    public List<MatchLineupResponse> getMatchLineup(
            Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found"));

        return matchLineupRepository
                .findByMatch(match)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MatchLineupResponse> getTeamMatchLineup(
            Long matchId,
            Long teamId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found"));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Team not found"));

        boolean teamBelongsToMatch =
                matchTeamRepository
                        .findByMatchAndTeam(match, team)
                        .isPresent();

        if (!teamBelongsToMatch) {
            throw new IllegalArgumentException(
                    "Team does not belong to this match");
        }

        return matchLineupRepository
                .findByMatchAndTeam(match, team)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private MatchLineupResponse toResponse(
            MatchLineup lineup) {

        MatchLineupResponse response =
                new MatchLineupResponse();

        response.setId(lineup.getId());
        response.setMatchId(lineup.getMatch().getId());

        response.setTeamId(lineup.getTeam().getId());
        response.setTeamName(lineup.getTeam().getName());
        response.setTeamShortName(
                lineup.getTeam().getShortName());

        response.setPlayerId(lineup.getPlayer().getId());
        response.setPlayerName(
                lineup.getPlayer().getDisplayName());

        response.setJerseyNumber(lineup.getJerseyNumber());
        response.setPlaying(lineup.getPlaying());
        response.setCaptain(lineup.getCaptain());
        response.setWicketKeeper(lineup.getWicketKeeper());

        return response;
    }
}