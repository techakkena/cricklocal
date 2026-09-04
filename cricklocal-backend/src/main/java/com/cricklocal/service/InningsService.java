package com.cricklocal.service;

import com.cricklocal.dto.InningsResponse;
import com.cricklocal.dto.StartInningsRequest;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.Team;
import com.cricklocal.enums.InningsStatus;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.MatchTeamRepository;
import com.cricklocal.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class InningsService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final InningsRepository inningsRepository;

    public InningsService(
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            MatchTeamRepository matchTeamRepository,
            InningsRepository inningsRepository) {

        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.matchTeamRepository = matchTeamRepository;
        this.inningsRepository = inningsRepository;
    }

    @Transactional
    public InningsResponse startInnings(
            Long matchId,
            StartInningsRequest request) {

        // 1. Match must exist.
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found"));

        // 2. Batting and bowling teams must be different.
        if (request.getBattingTeamId()
                .equals(request.getBowlingTeamId())) {

            throw new IllegalArgumentException(
                    "Batting team and bowling team must be different");
        }

        // 3. Both teams must exist.
        Team battingTeam = teamRepository.findById(
                        request.getBattingTeamId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Batting team not found"));

        Team bowlingTeam = teamRepository.findById(
                        request.getBowlingTeamId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bowling team not found"));

        // 4. Both teams must belong to this match.
        boolean battingTeamInMatch =
                matchTeamRepository
                        .findByMatchAndTeam(match, battingTeam)
                        .isPresent();

        if (!battingTeamInMatch) {
            throw new IllegalArgumentException(
                    "Batting team does not belong to this match");
        }

        boolean bowlingTeamInMatch =
                matchTeamRepository
                        .findByMatchAndTeam(match, bowlingTeam)
                        .isPresent();

        if (!bowlingTeamInMatch) {
            throw new IllegalArgumentException(
                    "Bowling team does not belong to this match");
        }

        // 5. Only innings 1 and 2 are allowed for the basic
        // two-innings match flow.
        if (request.getInningsNumber() < 1
                || request.getInningsNumber() > 2) {

            throw new IllegalArgumentException(
                    "Innings number must be 1 or 2");
        }

        // 6. Prevent duplicate innings numbers.
        if (inningsRepository.existsByMatchAndInningsNumber(
                match,
                request.getInningsNumber())) {

            throw new IllegalArgumentException(
                    "This innings number already exists for the match");
        }

        // 7. Only one innings can be LIVE at a time.
        if (inningsRepository.existsByMatchAndStatus(
                match,
                InningsStatus.LIVE)) {

            throw new IllegalArgumentException(
                    "Another innings is already live for this match");
        }

        // 8. The same team cannot bat in both innings.
        if (inningsRepository.existsByMatchAndBattingTeam(
                match,
                battingTeam)) {

            throw new IllegalArgumentException(
                    "This team has already batted in this match");
        }

        // 9. For innings 2, the batting team should be the team
        // that did not bat in innings 1.
        if (request.getInningsNumber() == 2) {

            Innings firstInnings =
                    inningsRepository
                            .findByMatchAndInningsNumber(match, 1)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Innings 1 must be started before innings 2"));

            if (firstInnings.getBowlingTeam().getId()
                    .equals(battingTeam.getId())) {

                throw new IllegalArgumentException(
                        "Innings 2 batting team must be the team that bowled in innings 1");
            }
        }

        Innings innings = new Innings();

        innings.setMatch(match);
        innings.setBattingTeam(battingTeam);
        innings.setBowlingTeam(bowlingTeam);
        innings.setInningsNumber(request.getInningsNumber());

        innings.setTotalRuns(0);
        innings.setWickets(0);
        innings.setLegalBalls(0);

        innings.setStatus(InningsStatus.LIVE);
        innings.setStartedAt(Instant.now());

        Innings savedInnings =
                inningsRepository.save(innings);

        return toResponse(savedInnings);
    }

    @Transactional(readOnly = true)
    public InningsResponse getInningsById(
            Long inningsId) {

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Innings not found"));

        return toResponse(innings);
    }

    @Transactional(readOnly = true)
    public List<InningsResponse> getMatchInnings(
            Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found"));

        return inningsRepository
                .findByMatchOrderByInningsNumberAsc(match)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private InningsResponse toResponse(Innings innings) {

        InningsResponse response = new InningsResponse();

        response.setId(innings.getId());
        response.setMatchId(innings.getMatch().getId());

        response.setInningsNumber(
                innings.getInningsNumber());

        response.setBattingTeamId(
                innings.getBattingTeam().getId());

        response.setBattingTeamName(
                innings.getBattingTeam().getName());

        response.setBattingTeamShortName(
                innings.getBattingTeam().getShortName());

        response.setBowlingTeamId(
                innings.getBowlingTeam().getId());

        response.setBowlingTeamName(
                innings.getBowlingTeam().getName());

        response.setBowlingTeamShortName(
                innings.getBowlingTeam().getShortName());

        response.setTotalRuns(
                innings.getTotalRuns());

        response.setWickets(
                innings.getWickets());

        response.setLegalBalls(
                innings.getLegalBalls());

        response.setStatus(
                innings.getStatus());

        response.setCreatedAt(
                innings.getCreatedAt());

        response.setStartedAt(
                innings.getStartedAt());

        response.setCompletedAt(
                innings.getCompletedAt());

        return response;
    }
}