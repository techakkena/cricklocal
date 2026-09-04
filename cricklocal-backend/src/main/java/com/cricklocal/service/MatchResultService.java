package com.cricklocal.service;

import com.cricklocal.dto.MatchResultResponse;
import com.cricklocal.dto.RecordMatchResultRequest;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchResult;
import com.cricklocal.entity.Team;
import com.cricklocal.enums.MatchResultType;
import com.cricklocal.enums.MatchStatus;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.MatchResultRepository;
import com.cricklocal.repository.TeamRepository;
import com.cricklocal.repository.MatchTeamRepository;
import org.springframework.stereotype.Service;

@Service
public class MatchResultService {

    private final MatchRepository matchRepository;
    private final MatchResultRepository matchResultRepository;
    private final TeamRepository teamRepository;
    private final MatchTeamRepository matchTeamRepository;

    public MatchResultService(
            MatchRepository matchRepository,
            MatchResultRepository matchResultRepository,
            TeamRepository teamRepository,
            MatchTeamRepository matchTeamRepository) {

        this.matchRepository = matchRepository;
        this.matchResultRepository = matchResultRepository;
        this.teamRepository = teamRepository;
        this.matchTeamRepository = matchTeamRepository;
    }

    public MatchResultResponse recordResult(
            Long matchId,
            RecordMatchResultRequest request) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found: " + matchId));

        if (matchResultRepository.existsByMatch(match)) {
            throw new IllegalArgumentException(
                    "Match result already exists for match: " + matchId);
        }

        MatchResultType resultType = request.getResultType();

        validateResultRequest(match, request);

        Team winningTeam = null;
        Team losingTeam = null;

        if (request.getWinningTeamId() != null) {
            winningTeam = teamRepository.findById(
                            request.getWinningTeamId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Winning team not found: "
                                            + request.getWinningTeamId()));

            validateTeamBelongsToMatch(match, winningTeam);
        }

        if (request.getLosingTeamId() != null) {
            losingTeam = teamRepository.findById(
                            request.getLosingTeamId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Losing team not found: "
                                            + request.getLosingTeamId()));

            validateTeamBelongsToMatch(match, losingTeam);
        }

        if (winningTeam != null
                && losingTeam != null
                && winningTeam.getId().equals(losingTeam.getId())) {

            throw new IllegalArgumentException(
                    "Winning team and losing team must be different");
        }

        MatchResult result = new MatchResult();

        result.setMatch(match);
        result.setResultType(resultType);
        result.setWinningTeam(winningTeam);
        result.setLosingTeam(losingTeam);
        result.setMarginRuns(request.getMarginRuns());
        result.setMarginWickets(request.getMarginWickets());
        result.setResultText(request.getResultText());

        MatchResult savedResult =
                matchResultRepository.save(result);

                if (resultType == MatchResultType.ABANDONED) {
                        match.setStatus(MatchStatus.ABANDONED);
                } else {
                        match.setStatus(MatchStatus.COMPLETED);
                }

                matchRepository.save(match);

        return toResponse(savedResult);
    }

    public MatchResultResponse getResult(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found: " + matchId));

        MatchResult result =
                matchResultRepository.findByMatch(match)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Match result not found for match: "
                                                + matchId));

        return toResponse(result);
    }

    private void validateResultRequest(
            Match match,
            RecordMatchResultRequest request) {

        MatchResultType resultType =
                request.getResultType();

        if (resultType == MatchResultType.WON_BY_RUNS) {

            if (request.getWinningTeamId() == null) {
                throw new IllegalArgumentException(
                        "Winning team is required for WON_BY_RUNS");
            }

            if (request.getLosingTeamId() == null) {
                throw new IllegalArgumentException(
                        "Losing team is required for WON_BY_RUNS");
            }

            if (request.getMarginRuns() == null
                    || request.getMarginRuns() <= 0) {

                throw new IllegalArgumentException(
                        "Positive marginRuns is required for WON_BY_RUNS");
            }
        }

        if (resultType == MatchResultType.WON_BY_WICKETS) {

            if (request.getWinningTeamId() == null) {
                throw new IllegalArgumentException(
                        "Winning team is required for WON_BY_WICKETS");
            }

            if (request.getLosingTeamId() == null) {
                throw new IllegalArgumentException(
                        "Losing team is required for WON_BY_WICKETS");
            }

            if (request.getMarginWickets() == null
                    || request.getMarginWickets() <= 0) {

                throw new IllegalArgumentException(
                        "Positive marginWickets is required for WON_BY_WICKETS");
            }
        }

        if (resultType == MatchResultType.TIE
                || resultType == MatchResultType.NO_RESULT
                || resultType == MatchResultType.ABANDONED) {

            if (request.getWinningTeamId() != null) {
                throw new IllegalArgumentException(
                        "Winning team must be null for " + resultType);
            }

            if (request.getLosingTeamId() != null) {
                throw new IllegalArgumentException(
                        "Losing team must be null for " + resultType);
            }
        }

        if (request.getMarginRuns() != null
                && request.getMarginRuns() < 0) {

            throw new IllegalArgumentException(
                    "Margin runs cannot be negative");
        }

        if (request.getMarginWickets() != null
                && request.getMarginWickets() < 0) {

            throw new IllegalArgumentException(
                    "Margin wickets cannot be negative");
        }
    }

    private void validateTeamBelongsToMatch(
                Match match,
                Team team) {

        boolean belongsToMatch =
                matchTeamRepository.findByMatchAndTeam(match, team)
                        .isPresent();

        if (!belongsToMatch) {
                throw new IllegalArgumentException(
                        "Team " + team.getId()
                                + " does not belong to match "
                                + match.getId());
        }
    }

    private MatchResultResponse toResponse(
            MatchResult result) {

        MatchResultResponse response =
                new MatchResultResponse();

        response.setId(result.getId());
        response.setMatchId(result.getMatch().getId());
        response.setResultType(result.getResultType());

        if (result.getWinningTeam() != null) {
            response.setWinningTeamId(
                    result.getWinningTeam().getId());

            response.setWinningTeamName(
                    result.getWinningTeam().getName());
        }

        if (result.getLosingTeam() != null) {
            response.setLosingTeamId(
                    result.getLosingTeam().getId());

            response.setLosingTeamName(
                    result.getLosingTeam().getName());
        }

        response.setMarginRuns(result.getMarginRuns());
        response.setMarginWickets(result.getMarginWickets());
        response.setResultText(result.getResultText());

        return response;
    }
}