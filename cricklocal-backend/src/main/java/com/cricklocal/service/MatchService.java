package com.cricklocal.service;

import com.cricklocal.dto.CreateMatchRequest;
import com.cricklocal.dto.MatchResponse;
import com.cricklocal.dto.MatchTeamResponse;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchTeam;
import com.cricklocal.entity.Team;
import com.cricklocal.enums.MatchTeamSide;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.MatchTeamRepository;
import com.cricklocal.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.entity.Series;
import com.cricklocal.repository.SeriesRepository;
import com.cricklocal.repository.SeriesTeamRepository;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final TeamRepository teamRepository;
    private final SeriesRepository seriesRepository;
    private final SeriesTeamRepository seriesTeamRepository;

    public MatchService(
        MatchRepository matchRepository,
        MatchTeamRepository matchTeamRepository,
        TeamRepository teamRepository,
        SeriesRepository seriesRepository,
        SeriesTeamRepository seriesTeamRepository) {

        this.matchRepository = matchRepository;
        this.matchTeamRepository = matchTeamRepository;
        this.teamRepository = teamRepository;
        this.seriesRepository = seriesRepository;
        this.seriesTeamRepository = seriesTeamRepository;
    }

    @Transactional
    public Match createMatch(CreateMatchRequest request) {

        if (request.getTeamAId().equals(request.getTeamBId())) {
            throw new IllegalArgumentException(
                    "Team A and Team B must be different");
        }

        Team teamA = teamRepository.findById(request.getTeamAId())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Team A not found"));

        Team teamB = teamRepository.findById(request.getTeamBId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Team B not found"));

        Series series = null;

        if (request.getSeriesId() != null) {

            series = seriesRepository.findById(request.getSeriesId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Series not found"));

            if (request.getMatchNumber() == null) {
                throw new IllegalArgumentException(
                        "Match number is required for a series match");
            }

            if (request.getMatchNumber() < 1
                    || request.getMatchNumber() > series.getTotalMatches()) {

                throw new IllegalArgumentException(
                        "Match number must be between 1 and "
                                + series.getTotalMatches());
            }

            if (matchRepository.existsBySeriesAndMatchNumber(
                    series,
                    request.getMatchNumber())) {

                throw new IllegalArgumentException(
                        "Match number is already used in this series");
            }

            boolean teamAInSeries = seriesTeamRepository
                    .existsBySeriesAndTeam(series, teamA);

            boolean teamBInSeries = seriesTeamRepository
                    .existsBySeriesAndTeam(series, teamB);

            if (!teamAInSeries || !teamBInSeries) {
                throw new IllegalArgumentException(
                        "Both match teams must belong to the series");
            }

        } else if (request.getMatchNumber() != null) {

            throw new IllegalArgumentException(
                    "Match number can only be used with a series");
        }

        Match match = new Match();

        match.setName(request.getName());
        match.setFormat(request.getFormat());
        match.setTotalOvers(request.getTotalOvers());
        match.setMaxPlayersPerTeam(request.getMaxPlayersPerTeam());
        match.setScheduledAt(request.getScheduledAt());
        match.setVenue(request.getVenue());
        match.setSeries(series);
        match.setMatchNumber(request.getMatchNumber());

        Match savedMatch = matchRepository.save(match);

        MatchTeam matchTeamA = new MatchTeam();
        matchTeamA.setMatch(savedMatch);
        matchTeamA.setTeam(teamA);
        matchTeamA.setSide(MatchTeamSide.TEAM_A);

        matchTeamRepository.save(matchTeamA);

        MatchTeam matchTeamB = new MatchTeam();
        matchTeamB.setMatch(savedMatch);
        matchTeamB.setTeam(teamB);
        matchTeamB.setSide(MatchTeamSide.TEAM_B);

        matchTeamRepository.save(matchTeamB);

        return savedMatch;
    }

    public java.util.List<MatchResponse> getAllMatches() {

        return matchRepository.findAll()
                .stream()
                .map(this::toMatchResponse)
                .toList();
    }

    private MatchResponse toMatchResponse(Match match) {

        MatchResponse response = new MatchResponse();

        response.setId(match.getId());
        response.setName(match.getName());
        response.setFormat(match.getFormat());
        response.setTotalOvers(match.getTotalOvers());
        response.setMaxPlayersPerTeam(match.getMaxPlayersPerTeam());

        if (match.getSeries() != null) {
            response.setSeriesId(match.getSeries().getId());
            response.setSeriesName(match.getSeries().getName());
        }

        response.setMatchNumber(match.getMatchNumber());
        response.setScheduledAt(match.getScheduledAt());
        response.setVenue(match.getVenue());
        response.setStatus(match.getStatus());
        response.setCreatedAt(match.getCreatedAt());

        java.util.List<MatchTeamResponse> teams = matchTeamRepository
                .findByMatch(match)
                .stream()
                .map(this::toMatchTeamResponse)
                .toList();

        response.setTeams(teams);

        return response;
    }

    private MatchTeamResponse toMatchTeamResponse(MatchTeam matchTeam) {

        MatchTeamResponse response = new MatchTeamResponse();

        response.setTeamId(matchTeam.getTeam().getId());
        response.setTeamName(matchTeam.getTeam().getName());
        response.setShortName(matchTeam.getTeam().getShortName());
        response.setSide(matchTeam.getSide());

        return response;
    }
    public MatchResponse getMatchById(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Match not found"));

        return toMatchResponse(match);
    }
}