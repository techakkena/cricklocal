package com.cricklocal.controller;

import com.cricklocal.dto.MatchResultResponse;
import com.cricklocal.dto.RecordMatchResultRequest;
import com.cricklocal.service.MatchResultService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class MatchResultController {

    private final MatchResultService matchResultService;

    public MatchResultController(
            MatchResultService matchResultService) {
        this.matchResultService = matchResultService;
    }

    @PostMapping("/{matchId}/result")
    public MatchResultResponse recordResult(
            @PathVariable Long matchId,
            @Valid @RequestBody RecordMatchResultRequest request) {

        return matchResultService.recordResult(
                matchId,
                request);
    }

    @GetMapping("/{matchId}/result")
    public MatchResultResponse getResult(
            @PathVariable Long matchId) {

        return matchResultService.getResult(matchId);
    }
}