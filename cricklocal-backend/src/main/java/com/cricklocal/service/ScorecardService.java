package com.cricklocal.service;

import com.cricklocal.dto.BattingInningsResponse;
import com.cricklocal.dto.BowlingInningsResponse;
import com.cricklocal.dto.FallOfWicketResponse;
import com.cricklocal.dto.FieldingEventResponse;
import com.cricklocal.dto.InningsScorecardResponse;
import com.cricklocal.dto.MatchResultResponse;
import com.cricklocal.dto.PartnershipResponse;
import com.cricklocal.dto.ScorecardResponse;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Match;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.repository.MatchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScorecardService {

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final BattingInningsService battingInningsService;
    private final BowlingInningsService bowlingInningsService;
    private final PartnershipService partnershipService;
    private final FallOfWicketService fallOfWicketService;
    private final FieldingEventService fieldingEventService;
    private final MatchResultService matchResultService;

    public ScorecardService(
            MatchRepository matchRepository,
            InningsRepository inningsRepository,
            BattingInningsService battingInningsService,
            BowlingInningsService bowlingInningsService,
            PartnershipService partnershipService,
            FallOfWicketService fallOfWicketService,
            FieldingEventService fieldingEventService,
            MatchResultService matchResultService) {

        this.matchRepository = matchRepository;
        this.inningsRepository = inningsRepository;
        this.battingInningsService = battingInningsService;
        this.bowlingInningsService = bowlingInningsService;
        this.partnershipService = partnershipService;
        this.fallOfWicketService = fallOfWicketService;
        this.fieldingEventService = fieldingEventService;
        this.matchResultService = matchResultService;
    }

    public ScorecardResponse getScorecard(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Match not found: " + matchId));

        List<Innings> inningsList =
                inningsRepository
                        .findByMatchOrderByInningsNumberAsc(match);

        ScorecardResponse response =
                new ScorecardResponse();

        response.setMatchId(match.getId());
        response.setMatchName(match.getName());
        response.setStatus(match.getStatus().name());

        List<InningsScorecardResponse> inningsResponses =
                inningsList.stream()
                        .map(this::toInningsScorecardResponse)
                        .toList();

        response.setInnings(inningsResponses);

        try {
            response.setResult(
                    matchResultService.getResult(matchId));
        } catch (ResourceNotFoundException exception) {
            response.setResult(null);
        }

        return response;
    }

    private InningsScorecardResponse toInningsScorecardResponse(
            Innings innings) {

        InningsScorecardResponse response =
                new InningsScorecardResponse();

        response.setInningsId(innings.getId());
        response.setInningsNumber(
                innings.getInningsNumber());

        response.setBattingTeamId(
                innings.getBattingTeam().getId());

        response.setBattingTeamName(
                innings.getBattingTeam().getName());

        response.setBowlingTeamId(
                innings.getBowlingTeam().getId());

        response.setBowlingTeamName(
                innings.getBowlingTeam().getName());

        response.setTotalRuns(innings.getTotalRuns());
        response.setWickets(innings.getWickets());
        response.setLegalBalls(innings.getLegalBalls());
        response.setStatus(innings.getStatus().name());

        List<BattingInningsResponse> batting =
                battingInningsService
                        .getResponsesByInnings(innings);

        List<BowlingInningsResponse> bowling =
                bowlingInningsService
                        .getResponsesByInnings(innings);

        List<PartnershipResponse> partnerships =
                partnershipService
                        .getResponsesByInnings(innings);

        List<FallOfWicketResponse> fallOfWickets =
                fallOfWicketService
                        .getResponsesByInnings(innings);

        List<FieldingEventResponse> fieldingEvents =
                fieldingEventService
                        .getResponsesByInnings(innings);

        response.setBatting(batting);
        response.setBowling(bowling);
        response.setPartnerships(partnerships);
        response.setFallOfWickets(fallOfWickets);
        response.setFieldingEvents(fieldingEvents);

        return response;
    }
}