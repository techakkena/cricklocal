package com.cricklocal.service;

import com.cricklocal.dto.CreateSeriesRequest;
import com.cricklocal.dto.SeriesResponse;
import com.cricklocal.dto.SeriesTeamResponse;
import com.cricklocal.entity.Series;
import com.cricklocal.entity.SeriesTeam;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.SeriesRepository;
import com.cricklocal.repository.SeriesTeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cricklocal.dto.AddTeamToSeriesRequest;
import com.cricklocal.entity.Team;
import com.cricklocal.repository.TeamRepository;


@Service
public class SeriesService {

    private final SeriesRepository seriesRepository;
    private final SeriesTeamRepository seriesTeamRepository;
    private final TeamRepository teamRepository;

    public SeriesService(
        SeriesRepository seriesRepository,
        SeriesTeamRepository seriesTeamRepository,
        TeamRepository teamRepository) {

        this.seriesRepository = seriesRepository;
        this.seriesTeamRepository = seriesTeamRepository;
        this.teamRepository = teamRepository;
    }

    @Transactional
    public SeriesResponse createSeries(CreateSeriesRequest request) {

        if (request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {

            throw new IllegalArgumentException(
                    "End date cannot be before start date");
        }

        Series series = new Series();

        series.setName(request.getName());
        series.setTotalMatches(request.getTotalMatches());
        series.setStartDate(request.getStartDate());
        series.setEndDate(request.getEndDate());

        Series savedSeries = seriesRepository.save(series);

        return toSeriesResponse(savedSeries);
    }

    @Transactional(readOnly = true)
    public java.util.List<SeriesResponse> getAllSeries() {

        return seriesRepository.findAll()
                .stream()
                .map(this::toSeriesResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SeriesResponse getSeriesById(Long seriesId) {

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Series not found"));

        return toSeriesResponse(series);
    }

    @Transactional
    public SeriesResponse addTeamToSeries(
            Long seriesId,
            AddTeamToSeriesRequest request) {

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Series not found"));

        Team team = teamRepository.findById(request.getTeamId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Team not found"));

        if (seriesTeamRepository
                .existsBySeriesAndTeam(series, team)) {

            throw new IllegalArgumentException(
                    "Team is already part of this series");
        }

        SeriesTeam seriesTeam = new SeriesTeam();

        seriesTeam.setSeries(series);
        seriesTeam.setTeam(team);

        seriesTeamRepository.save(seriesTeam);

        return toSeriesResponse(series);
    }


    private SeriesResponse toSeriesResponse(Series series) {

        SeriesResponse response = new SeriesResponse();

        response.setId(series.getId());
        response.setName(series.getName());
        response.setTotalMatches(series.getTotalMatches());
        response.setStatus(series.getStatus());
        response.setStartDate(series.getStartDate());
        response.setEndDate(series.getEndDate());
        response.setCreatedAt(series.getCreatedAt());

        java.util.List<SeriesTeamResponse> teams =
                seriesTeamRepository.findBySeries(series)
                        .stream()
                        .map(this::toSeriesTeamResponse)
                        .toList();

        response.setTeams(teams);

        return response;
    }

    @Transactional(readOnly = true)
    public SeriesResponse getSeriesTeams(Long seriesId) {

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Series not found"));

        return toSeriesResponse(series);
    }

    private SeriesTeamResponse toSeriesTeamResponse(
            SeriesTeam seriesTeam) {

        SeriesTeamResponse response = new SeriesTeamResponse();

        response.setTeamId(seriesTeam.getTeam().getId());
        response.setTeamName(seriesTeam.getTeam().getName());
        response.setShortName(seriesTeam.getTeam().getShortName());

        return response;
    }
}