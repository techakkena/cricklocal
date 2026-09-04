package com.cricklocal.controller;

import com.cricklocal.dto.CreateSeriesRequest;
import com.cricklocal.dto.SeriesResponse;
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

    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
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
}