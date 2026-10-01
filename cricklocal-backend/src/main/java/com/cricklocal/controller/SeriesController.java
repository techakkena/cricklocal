package com.cricklocal.controller;

import com.cricklocal.dto.CreateSeriesRequest;
import com.cricklocal.dto.SeriesResponse;
import com.cricklocal.dto.AddPlayerToSeriesRequest;
import com.cricklocal.dto.SeriesParticipationResponse;
import com.cricklocal.service.SeriesParticipationService;
import com.cricklocal.service.SeriesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.cricklocal.dto.AddTeamToSeriesRequest;

import java.util.List;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final SeriesService seriesService;

    private final SeriesParticipationService seriesParticipationService;

    public SeriesController(
            SeriesService seriesService,
            SeriesParticipationService seriesParticipationService) {

        this.seriesService = seriesService;
        this.seriesParticipationService = seriesParticipationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SeriesResponse createSeries(
            @Valid @RequestBody CreateSeriesRequest request) {

        return seriesService.createSeries(request);
    }

    @GetMapping
    public List<SeriesResponse> getAllSeries() {
        return seriesService.getAllSeries();
    }

    @GetMapping("/{seriesId}")
    public SeriesResponse getSeriesById(
            @PathVariable Long seriesId) {

        return seriesService.getSeriesById(seriesId);
    }

    @PostMapping("/{seriesId}/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public SeriesResponse addTeamToSeries(
            @PathVariable Long seriesId,
            @Valid @RequestBody AddTeamToSeriesRequest request) {

        return seriesService.addTeamToSeries(seriesId, request);
    }

    @PostMapping("/{seriesId}/players")
    @ResponseStatus(HttpStatus.CREATED)
    public void addPlayerToSeries(
            @PathVariable Long seriesId,
            @Valid @RequestBody AddPlayerToSeriesRequest request) {

        seriesParticipationService.addPlayerToSeries(
                seriesId,
                request.getPlayerId()
        );
    }

    @GetMapping("/{seriesId}/players")
    public List<SeriesParticipationResponse> getSeriesParticipants(
            @PathVariable Long seriesId) {

        return seriesParticipationService.getSeriesParticipants(seriesId);
    }

    @DeleteMapping("/{seriesId}/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removePlayerFromSeries(
            @PathVariable Long seriesId,
            @PathVariable Long playerId) {

        seriesParticipationService.removePlayerFromSeries(
                seriesId,
                playerId
        );
    }
}