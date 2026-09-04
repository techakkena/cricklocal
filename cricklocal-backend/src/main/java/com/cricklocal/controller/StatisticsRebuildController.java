package com.cricklocal.controller;

import com.cricklocal.entity.Innings;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.service.StatisticsRebuildService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/innings")
public class StatisticsRebuildController {

    private final InningsRepository inningsRepository;
    private final StatisticsRebuildService statisticsRebuildService;

    public StatisticsRebuildController(
            InningsRepository inningsRepository,
            StatisticsRebuildService statisticsRebuildService) {
        this.inningsRepository = inningsRepository;
        this.statisticsRebuildService = statisticsRebuildService;
    }

    @PostMapping("/{inningsId}/rebuild-statistics")
    public String rebuildStatistics(
            @PathVariable Long inningsId) {

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Innings not found: " + inningsId));

        statisticsRebuildService.rebuild(innings);

        return "Statistics rebuilt successfully for innings "
                + inningsId;
    }
}