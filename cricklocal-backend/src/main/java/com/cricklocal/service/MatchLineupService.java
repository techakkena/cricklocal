package com.cricklocal.service;

import com.cricklocal.repository.DeliveryRepository;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.dto.ReplaceMatchPlayerRequest;
import com.cricklocal.dto.AddPlayerToMatchRequest;
import com.cricklocal.dto.MatchLineupResponse;
import com.cricklocal.dto.PlayingXIResponse;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchLineup;
import com.cricklocal.entity.PlayingXI;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.Team;
import com.cricklocal.entity.TeamPlayer;
import com.cricklocal.entity.Innings;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.MatchLineupRepository;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.MatchTeamRepository;
import com.cricklocal.repository.PlayerRepository;
import com.cricklocal.repository.PlayingXIRepository;
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
    private final PlayingXIRepository playingXIRepository;
    private final DeliveryRepository deliveryRepository;
    private final InningsRepository inningsRepository;


    public MatchLineupService(
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            PlayerRepository playerRepository,
            MatchTeamRepository matchTeamRepository,
            TeamPlayerRepository teamPlayerRepository,
            MatchLineupRepository matchLineupRepository,
            PlayingXIRepository playingXIRepository,
            DeliveryRepository deliveryRepository,
            InningsRepository inningsRepository) {

        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.matchTeamRepository = matchTeamRepository;
        this.teamPlayerRepository = teamPlayerRepository;
        this.matchLineupRepository = matchLineupRepository;
        this.playingXIRepository = playingXIRepository;
        this.deliveryRepository = deliveryRepository;
        this.inningsRepository = inningsRepository;
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

        // Once the Playing XI is locked, no further lineup changes are allowed.
        if (playingXIRepository.existsByMatchAndTeam(match, team)) {
                throw new IllegalArgumentException(
                        "Playing XI has already been finalized for this team");
        }

        // 1. Team must belong to this match.
        if (matchTeamRepository.findByMatchAndTeam(match, team).isEmpty()) {
                throw new IllegalArgumentException(
                        "Team does not belong to this match");
        }

        // 2. Player must belong to this team.
        TeamPlayer teamPlayer =
                teamPlayerRepository.findByTeamAndPlayer(team, player)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Player does not belong to this team"));

        // 3. Player must be active.
        if (!teamPlayer.getActive()) {
                throw new IllegalArgumentException(
                        "Player is inactive for this team");
        }

        // 4. Prevent the same player from being added to another team
        //    in this match. An existing row for the SAME team is allowed
        //    because it represents a select/deselect update.
        boolean playerAlreadyInAnotherTeam =
                matchLineupRepository.findByMatch(match)
                        .stream()
                        .anyMatch(lineup ->
                                lineup.getPlayer().getId().equals(player.getId())
                                        && !lineup.getTeam().getId().equals(team.getId()));

        if (playerAlreadyInAnotherTeam) {
        throw new IllegalArgumentException(
                "Player is already in another team lineup");
        }

        // Find the existing lineup row. This is the key fix:
        // select/deselect now updates the existing record instead of
        // creating duplicate MatchLineup records.
        MatchLineup lineup =
                matchLineupRepository.findByMatchAndTeam(match, team)
                        .stream()
                        .filter(item -> item.getPlayer().getId()
                                .equals(player.getId()))
                        .findFirst()
                        .orElse(null);

        boolean isNewLineup = lineup == null;

        // 5. Maximum PLAYING players based on the match configuration.
        long currentPlayingPlayers =
                matchLineupRepository.findByMatchAndTeam(match, team)
                        .stream()
                        .filter(item -> Boolean.TRUE.equals(item.getPlaying()))
                        .count();

        if (isNewLineup
                && Boolean.TRUE.equals(request.getPlaying())
                && currentPlayingPlayers >= match.getMaxPlayersPerTeam()) {
        throw new IllegalArgumentException(
                "A Playing XI can contain only "
                        + match.getMaxPlayersPerTeam()
                        + " players");
        }

        // 6. A player being made Captain/WK must be playing.
        if ((Boolean.TRUE.equals(request.getCaptain())
                || Boolean.TRUE.equals(request.getWicketKeeper()))
                && !Boolean.TRUE.equals(request.getPlaying())) {
        throw new IllegalArgumentException(
                "Captain and wicketkeeper must be part of the Playing XI");
        }

        // Create the row only when the player has never been added.
        if (isNewLineup) {
        lineup = new MatchLineup();
        lineup.setMatch(match);
        lineup.setTeam(team);
        lineup.setPlayer(player);
        lineup.setJerseyNumber(teamPlayer.getJerseyNumber());
        }

        // 7. Apply the player's new state.
        lineup.setPlaying(
                Boolean.TRUE.equals(request.getPlaying()));

        lineup.setCaptain(
                Boolean.TRUE.equals(request.getCaptain()));

        lineup.setWicketKeeper(
                Boolean.TRUE.equals(request.getWicketKeeper()));

        // 8. If this player is being selected as captain,
        //    automatically remove captain from the previous player.
        if (Boolean.TRUE.equals(request.getCaptain())) {
                matchLineupRepository.findByMatchAndTeam(match, team)
                        .stream()
                        .filter(item ->
                                !item.getPlayer().getId().equals(player.getId()))
                        .filter(item -> Boolean.TRUE.equals(item.getCaptain()))
                        .forEach(item -> {
                        item.setCaptain(false);
                        matchLineupRepository.save(item);
                        });
        }

        // 9. If this player is being selected as wicketkeeper,
        //    automatically remove WK from the previous player.
        if (Boolean.TRUE.equals(request.getWicketKeeper())) {
                matchLineupRepository.findByMatchAndTeam(match, team)
                        .stream()
                        .filter(item ->
                                !item.getPlayer().getId().equals(player.getId()))
                        .filter(item -> Boolean.TRUE.equals(item.getWicketKeeper()))
                        .forEach(item -> {
                        item.setWicketKeeper(false);
                        matchLineupRepository.save(item);
                        });
        }

        // 10. If the player is deselected, they cannot remain Captain/WK.
        if (!Boolean.TRUE.equals(request.getPlaying())) {
                lineup.setCaptain(false);
                lineup.setWicketKeeper(false);
        }

        MatchLineup savedLineup =
                matchLineupRepository.save(lineup);

        return toResponse(savedLineup);
    }

    @Transactional
    public MatchLineupResponse replacePlayer(
                        Long matchId,
                        Long teamId,
                        ReplaceMatchPlayerRequest request) {

                Match match = matchRepository.findById(matchId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Match not found"));

                Team team = teamRepository.findById(teamId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Team not found"));

                if (matchTeamRepository.findByMatchAndTeam(match, team).isEmpty()) {
                        throw new IllegalArgumentException(
                                "Team does not belong to this match");
                }

                // Replacement is only possible after the Playing XI
                // has been finalized.
                if (!playingXIRepository.existsByMatchAndTeam(match, team)) {
                        throw new IllegalArgumentException(
                                "Playing XI must be finalized before replacing a player");
                }

                if (request.getOutPlayerId().equals(request.getInPlayerId())) {
                        throw new IllegalArgumentException(
                                "Outgoing and incoming players must be different");
                }

                Player outPlayer = playerRepository.findById(
                        request.getOutPlayerId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Outgoing player not found"));

                Player inPlayer = playerRepository.findById(
                        request.getInPlayerId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Incoming player not found"));

                // Outgoing player must currently be part of this team's
                // finalized Playing XI.
                MatchLineup outgoingLineup =
                        matchLineupRepository.findByMatchAndTeam(match, team)
                                .stream()
                                .filter(lineup ->
                                        Boolean.TRUE.equals(lineup.getPlaying())
                                                && lineup.getPlayer().getId()
                                                .equals(outPlayer.getId()))
                                .findFirst()
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Outgoing player is not in this team's Playing XI"));

                // Incoming player must belong to the same team.
                TeamPlayer incomingTeamPlayer =
                        teamPlayerRepository.findByTeamAndPlayer(team, inPlayer)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Incoming player does not belong to this team"));

                // Incoming player must be active.
                if (!Boolean.TRUE.equals(incomingTeamPlayer.getActive())) {
                        throw new IllegalArgumentException(
                                "Incoming player is inactive for this team");
                }

                // Incoming player cannot already be selected anywhere in this match.
                if (matchLineupRepository.existsByMatchAndPlayer(match,inPlayer)) {
                        throw new IllegalArgumentException(
                                "Incoming player is already part of this match lineup");
                }

                // A player who has already participated as a batter or bowler
                // in this match cannot be replaced.
                List<Innings> matchInnings =
                        inningsRepository.findByMatchOrderByInningsNumberAsc(match);

                boolean hasBatted = matchInnings.stream()
                        .anyMatch(innings ->
                                !deliveryRepository.findByInningsAndBatter(
                                        innings,
                                        outPlayer)
                                        .isEmpty());

                if (hasBatted) {
                        throw new IllegalArgumentException(
                                "Player cannot be replaced because they have already batted");
                }

                boolean hasBowled = matchInnings.stream()
                        .anyMatch(innings ->
                                !deliveryRepository.findByInningsAndBowler(
                                        innings,
                                        outPlayer)
                                        .isEmpty());

                if (hasBowled) {
                        throw new IllegalArgumentException(
                                "Player cannot be replaced because they have already bowled");
                }

                // The incoming player must not have a finalized Playing XI
                // conflict at the same scheduled time.
                if (match.getScheduledAt() != null
                        && matchLineupRepository.existsFinalizedScheduleConflict(
                                inPlayer,
                                match,
                                match.getScheduledAt())) {

                        throw new IllegalArgumentException(
                                "Incoming player has a finalized Playing XI conflict at the same scheduled time: "
                                        + inPlayer.getDisplayName());
                }

                /*
                * Replace only the player reference.
                *
                * Keep:
                * - lineup row
                * - jersey number
                * - captain status
                * - wicket keeper status
                * - playing status
                *
                * This keeps the configured Playing XI size unchanged.
                */
                outgoingLineup.setPlayer(inPlayer);

                MatchLineup savedLineup =
                        matchLineupRepository.save(outgoingLineup);

        return toResponse(savedLineup);
    }

    @Transactional(readOnly = true)
    public PlayingXIResponse getFinalizedPlayingXI(
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

        PlayingXI savedXI =
                playingXIRepository
                        .findByMatchAndTeam(match, team)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Playing XI has not been finalized for this team"));

        List<MatchLineup> playingXI =
                matchLineupRepository
                        .findByMatchAndTeam(match, team)
                        .stream()
                        .filter(lineup ->
                                Boolean.TRUE.equals(lineup.getPlaying()))
                        .toList();

        MatchLineup captain =
                playingXI.stream()
                        .filter(lineup ->
                                Boolean.TRUE.equals(lineup.getCaptain()))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Captain must be part of the Playing XI"));

        PlayingXIResponse response =
                new PlayingXIResponse();

        response.setPlayingXIId(savedXI.getId());
        response.setMatchId(match.getId());
        response.setTeamId(team.getId());
        response.setTeamName(team.getName());
        response.setTeamShortName(team.getShortName());
        response.setFinalizedAt(savedXI.getCreatedAt());
        response.setPlayerCount(playingXI.size());
        response.setCaptainPlayerId(
                captain.getPlayer().getId());
        response.setCaptainName(
                captain.getPlayer().getDisplayName());

        return response;
    }

    @Transactional
    public PlayingXIResponse finalizePlayingXI(
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

        if (playingXIRepository.existsByMatchAndTeam(match, team)) {
            throw new IllegalArgumentException(
                    "Playing XI has already been finalized for this team");
        }

        List<MatchLineup> teamLineup =
                matchLineupRepository.findByMatchAndTeam(match, team);

        List<MatchLineup> playingXI =
                teamLineup.stream()
                        .filter(lineup ->
                                Boolean.TRUE.equals(lineup.getPlaying()))
                        .toList();

        if (playingXI.size() != match.getMaxPlayersPerTeam()) {
        throw new IllegalArgumentException(
                "Exactly "
                        + match.getMaxPlayersPerTeam()
                        + " players must be selected for the Playing XI");
        }

        MatchLineup captain =
                playingXI.stream()
                        .filter(lineup ->
                                Boolean.TRUE.equals(lineup.getCaptain()))
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Captain must be part of the Playing XI"));

        playingXI.stream()
                .filter(lineup ->
                        Boolean.TRUE.equals(lineup.getWicketKeeper()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Wicket Keeper must be part of the Playing XI"));

        for (MatchLineup lineup : playingXI) {

            TeamPlayer teamPlayer =
                    teamPlayerRepository
                            .findByTeamAndPlayer(
                                    team,
                                    lineup.getPlayer())
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Player does not belong to this team"));

            if (!teamPlayer.getActive()) {
                throw new IllegalArgumentException(
                        "Playing XI contains an inactive team member: "
                                + lineup.getPlayer().getDisplayName());
            }

            if (match.getScheduledAt() != null
                    && matchLineupRepository
                    .existsFinalizedScheduleConflict(
                            lineup.getPlayer(),
                            match,
                            match.getScheduledAt())) {

                throw new IllegalArgumentException(
                        "Player has a finalized Playing XI conflict at the same scheduled time: "
                                + lineup.getPlayer().getDisplayName());
            }
        }

        PlayingXI finalizedXI = new PlayingXI();
        finalizedXI.setMatch(match);
        finalizedXI.setTeam(team);

        PlayingXI savedXI =
                playingXIRepository.save(finalizedXI);

        PlayingXIResponse response =
                new PlayingXIResponse();

        response.setPlayingXIId(savedXI.getId());
        response.setMatchId(match.getId());
        response.setTeamId(team.getId());
        response.setTeamName(team.getName());
        response.setTeamShortName(team.getShortName());
        response.setFinalizedAt(savedXI.getCreatedAt());
        response.setPlayerCount(playingXI.size());
        response.setCaptainPlayerId(captain.getPlayer().getId());
        response.setCaptainName(
                captain.getPlayer().getDisplayName());

        return response;
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
