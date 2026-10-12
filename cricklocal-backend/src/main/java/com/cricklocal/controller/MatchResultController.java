package com.cricklocal.controller;

import com.cricklocal.dto.MatchResultResponse;
import com.cricklocal.service.MatchResultService;
import com.cricklocal.service.ScoreOperatorAuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/matches")
public class MatchResultController {

    private final MatchResultService matchResultService;
    private final ScoreOperatorAuthorizationService operatorAuthorizationService;

    public MatchResultController(
            MatchResultService matchResultService,
            ScoreOperatorAuthorizationService operatorAuthorizationService) {
        this.matchResultService = matchResultService;
        this.operatorAuthorizationService = operatorAuthorizationService;
    }

    @PostMapping("/{matchId}/complete")
    public MatchResultResponse completeMatch(
            @PathVariable Long matchId,
            @RequestHeader(
                    name = "X-Score-Operator-Session",
                    required = false) String sessionToken) {

        operatorAuthorizationService.requireSessionForMatch(
                sessionToken, matchId);

        return matchResultService.completeMatch(matchId);
    }

    // Legacy endpoint disabled: result confirmation must use /complete.
    @PostMapping("/{matchId}/result")
    public MatchResultResponse recordResult(@PathVariable Long matchId) {
        throw new ResponseStatusException(
                HttpStatus.GONE,
                "Use POST /api/matches/{matchId}/complete to finalize a match");
    }

    @GetMapping("/{matchId}/result")
    public MatchResultResponse getResult(@PathVariable Long matchId) {
        return matchResultService.getResult(matchId);
    }
}