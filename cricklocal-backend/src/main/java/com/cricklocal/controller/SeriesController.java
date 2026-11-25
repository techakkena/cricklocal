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
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'SERIES', T(com.cricklocal.entity.PermissionAction).CREATE)")
    public SeriesResponse createSeries(
            @Valid @RequestBody CreateSeriesRequest request) {

        return seriesService.createSeries(request);
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'SERIES', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<SeriesResponse> getAllSeries() {
        return seriesService.getAllSeries();
    }

    @GetMapping("/{seriesId}")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'SERIES', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public SeriesResponse getSeriesById(
            @PathVariable Long seriesId) {

        return seriesService.getSeriesById(seriesId);
    }

    @PostMapping("/{seriesId}/teams")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'SERIES', T(com.cricklocal.entity.PermissionAction).EDIT)")
    public SeriesResponse addTeamToSeries(
            @PathVariable Long seriesId,
            @Valid @RequestBody AddTeamToSeriesRequest request) {

        return seriesService.addTeamToSeries(seriesId, request);
    }

    @PostMapping("/{seriesId}/players")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'SERIES', T(com.cricklocal.entity.PermissionAction).EDIT)")
    public void addPlayerToSeries(
            @PathVariable Long seriesId,
            @Valid @RequestBody AddPlayerToSeriesRequest request) {

        seriesParticipationService.addPlayerToSeries(
                seriesId,
                request.getPlayerId()
        );
    }

    @GetMapping("/{seriesId}/players")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'SERIES', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<SeriesParticipationResponse> getSeriesParticipants(
            @PathVariable Long seriesId) {

        return seriesParticipationService.getSeriesParticipants(seriesId);
    }

    @DeleteMapping("/{seriesId}/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'SERIES', T(com.cricklocal.entity.PermissionAction).DELETE)")
    public void removePlayerFromSeries(
            @PathVariable Long seriesId,
            @PathVariable Long playerId) {

        seriesParticipationService.removePlayerFromSeries(
                seriesId,
                playerId
        );
    }
}