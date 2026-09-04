package com.cricklocal.service;

import com.cricklocal.dto.RecordTossRequest;
import com.cricklocal.dto.TossResponse;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchTeam;
import com.cricklocal.entity.Team;
import com.cricklocal.entity.Toss;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.MatchTeamRepository;
import com.cricklocal.repository.TeamRepository;
import com.cricklocal.repository.TossRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TossService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final TossRepository tossRepository;

    public TossService(
            MatchRepository matchRepository,
            TeamRepository teamRepository,
            MatchTeamRepository matchTeamRepository,
            TossRepository tossRepository) {

        this.matchRepository = matchRepository;
        this.teamRepository = teamRepository;
        this.matchTeamRepository = matchTeamRepository;
        this.tossRepository = tossRepository;
    }

    @Transactional
    public TossResponse recordToss(
            Long matchId,
            RecordTossRequest request) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found"));

        if (tossRepository.existsByMatch(match)) {
            throw new IllegalArgumentException(
                    "Toss has already been recorded for this match");
        }

        Team winningTeam = teamRepository.findById(
                        request.getWinningTeamId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Winning team not found"));

        boolean teamBelongsToMatch = matchTeamRepository
                .findByMatchAndTeam(match, winningTeam)
                .isPresent();

        if (!teamBelongsToMatch) {
            throw new IllegalArgumentException(
                    "Winning team does not belong to this match");
        }

        Toss toss = new Toss();

        toss.setMatch(match);
        toss.setWinningTeam(winningTeam);
        toss.setDecision(request.getDecision());

        Toss savedToss = tossRepository.save(toss);

        return toTossResponse(savedToss);
    }

    @Transactional(readOnly = true)
    public TossResponse getTossByMatchId(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found"));

        Toss toss = tossRepository.findByMatch(match)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Toss not found for this match"));

        return toTossResponse(toss);
    }

    private TossResponse toTossResponse(Toss toss) {

        TossResponse response = new TossResponse();

        response.setId(toss.getId());
        response.setMatchId(toss.getMatch().getId());
        response.setWinningTeamId(toss.getWinningTeam().getId());
        response.setWinningTeamName(
                toss.getWinningTeam().getName());
        response.setWinningTeamShortName(
                toss.getWinningTeam().getShortName());
        response.setDecision(toss.getDecision());
        response.setCreatedAt(toss.getCreatedAt());

        return response;
    }
}