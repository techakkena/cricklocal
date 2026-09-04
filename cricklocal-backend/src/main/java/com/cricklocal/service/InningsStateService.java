package com.cricklocal.service;

import com.cricklocal.dto.InningsStateResponse;
import com.cricklocal.dto.SetInningsStateRequest;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.InningsState;
import com.cricklocal.entity.MatchLineup;
import com.cricklocal.entity.Player;
import com.cricklocal.enums.InningsStatus;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.repository.InningsStateRepository;
import com.cricklocal.repository.MatchLineupRepository;
import com.cricklocal.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InningsStateService {

    private final InningsRepository inningsRepository;
    private final InningsStateRepository inningsStateRepository;
    private final PlayerRepository playerRepository;
    private final MatchLineupRepository matchLineupRepository;

    public InningsStateService(
            InningsRepository inningsRepository,
            InningsStateRepository inningsStateRepository,
            PlayerRepository playerRepository,
            MatchLineupRepository matchLineupRepository) {

        this.inningsRepository = inningsRepository;
        this.inningsStateRepository = inningsStateRepository;
        this.playerRepository = playerRepository;
        this.matchLineupRepository = matchLineupRepository;
    }

    @Transactional
    public InningsStateResponse setState(
            Long inningsId,
            SetInningsStateRequest request) {

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Innings not found"));

        if (innings.getStatus() != InningsStatus.LIVE) {
            throw new IllegalArgumentException("Innings is not live");
        }

        if (request.getStrikerId().equals(request.getNonStrikerId())) {
            throw new IllegalArgumentException(
                    "Striker and non-striker must be different players");
        }

        Player striker = findPlayer(request.getStrikerId());
        Player nonStriker = findPlayer(request.getNonStrikerId());
        Player bowler = findPlayer(request.getBowlerId());

        validateBattingPlayer(
                innings,
                striker,
                "Striker");

        validateBattingPlayer(
                innings,
                nonStriker,
                "Non-striker");

        validateBowler(
                innings,
                bowler);

        InningsState state = inningsStateRepository
                .findByInnings(innings)
                .orElseGet(InningsState::new);

        state.setInnings(innings);
        state.setStriker(striker);
        state.setNonStriker(nonStriker);
        state.setCurrentBowler(bowler);

        InningsState savedState =
                inningsStateRepository.save(state);

        return toResponse(savedState);
    }

    @Transactional(readOnly = true)
    public InningsStateResponse getState(Long inningsId) {

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Innings not found"));

        InningsState state = inningsStateRepository
                .findByInnings(innings)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Innings state not found"));

        return toResponse(state);
    }

    private Player findPlayer(Long playerId) {

        return playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Player not found"));
    }

    private void validateBattingPlayer(
            Innings innings,
            Player player,
            String role) {

        MatchLineup lineup = matchLineupRepository
                .findByMatchAndPlayer(
                        innings.getMatch(),
                        player)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                role + " is not in the match lineup"));

        if (!Boolean.TRUE.equals(lineup.getPlaying())) {
            throw new IllegalArgumentException(
                    role + " is not marked as playing");
        }

        if (!lineup.getTeam().getId()
                .equals(innings.getBattingTeam().getId())) {

            throw new IllegalArgumentException(
                    role + " does not belong to the batting team");
        }
    }

    private void validateBowler(
            Innings innings,
            Player player) {

        MatchLineup lineup = matchLineupRepository
                .findByMatchAndPlayer(
                        innings.getMatch(),
                        player)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Bowler is not in the match lineup"));

        if (!Boolean.TRUE.equals(lineup.getPlaying())) {
            throw new IllegalArgumentException(
                    "Bowler is not marked as playing");
        }

        if (!lineup.getTeam().getId()
                .equals(innings.getBowlingTeam().getId())) {

            throw new IllegalArgumentException(
                    "Bowler does not belong to the bowling team");
        }
    }

    private InningsStateResponse toResponse(
            InningsState state) {

        InningsStateResponse response =
                new InningsStateResponse();

        response.setId(state.getId());

        response.setInningsId(
                state.getInnings().getId());

        response.setInningsNumber(
                state.getInnings().getInningsNumber());

        response.setStrikerId(
                state.getStriker().getId());

        response.setStrikerName(
                state.getStriker().getDisplayName());

        response.setNonStrikerId(
                state.getNonStriker().getId());

        response.setNonStrikerName(
                state.getNonStriker().getDisplayName());

        response.setCurrentBowlerId(
                state.getCurrentBowler().getId());

        response.setCurrentBowlerName(
                state.getCurrentBowler().getDisplayName());

        response.setCurrentOver(
                state.getCurrentOver());

        response.setLegalBallsInOver(
                state.getLegalBallsInOver());

        return response;
    }
}