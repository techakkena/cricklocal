package com.cricklocal.controller;

import com.cricklocal.dto.FieldingEventResponse;
import com.cricklocal.dto.FieldingStatsResponse;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.repository.PlayerRepository;
import com.cricklocal.service.FieldingEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FieldingEventController {

    private final InningsRepository inningsRepository;
    private final FieldingEventService fieldingEventService;
    private final PlayerRepository playerRepository;

    public FieldingEventController(
            InningsRepository inningsRepository,
            FieldingEventService fieldingEventService,
            PlayerRepository playerRepository) {

        this.inningsRepository = inningsRepository;
        this.fieldingEventService = fieldingEventService;
        this.playerRepository = playerRepository;
    }

    @GetMapping("/innings/{inningsId}/fielding")
    public List<FieldingEventResponse> getFieldingEvents(
            @PathVariable Long inningsId) {

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Innings not found: " + inningsId));

        return fieldingEventService
                .getResponsesByInnings(innings);
    }

    @GetMapping("/players/{playerId}/fielding")
    public FieldingStatsResponse getPlayerFieldingStats(
            @PathVariable Long playerId) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Player not found: " + playerId));

        return fieldingEventService.getPlayerStats(player);
    }
}