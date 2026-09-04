package com.cricklocal.controller;

import com.cricklocal.dto.BattingInningsResponse;
import com.cricklocal.entity.Innings;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.service.BattingInningsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/innings")
public class BattingInningsController {

    private final InningsRepository inningsRepository;
    private final BattingInningsService battingInningsService;

    public BattingInningsController(
            InningsRepository inningsRepository,
            BattingInningsService battingInningsService) {
        this.inningsRepository = inningsRepository;
        this.battingInningsService = battingInningsService;
    }

    @GetMapping("/{inningsId}/batting")
    public List<BattingInningsResponse> getBattingStats(
            @PathVariable Long inningsId) {

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Innings not found: " + inningsId));

        return battingInningsService
                .getResponsesByInnings(innings);
    }
}