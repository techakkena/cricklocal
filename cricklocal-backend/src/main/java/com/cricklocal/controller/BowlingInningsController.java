package com.cricklocal.controller;

import com.cricklocal.dto.BowlingInningsResponse;
import com.cricklocal.entity.Innings;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.service.BowlingInningsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/innings")
public class BowlingInningsController {

    private final InningsRepository inningsRepository;
    private final BowlingInningsService bowlingInningsService;

    public BowlingInningsController(
            InningsRepository inningsRepository,
            BowlingInningsService bowlingInningsService) {

        this.inningsRepository = inningsRepository;
        this.bowlingInningsService = bowlingInningsService;
    }

    @GetMapping("/{inningsId}/bowling")
    public List<BowlingInningsResponse> getBowlingStats(
            @PathVariable Long inningsId) {

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Innings not found: " + inningsId));

        return bowlingInningsService
                .getByInnings(innings)
                .stream()
                .map(bowlingInningsService::toResponse)
                .toList();
    }
}