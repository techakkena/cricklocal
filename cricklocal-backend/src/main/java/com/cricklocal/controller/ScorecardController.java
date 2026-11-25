package com.cricklocal.controller;

import com.cricklocal.dto.ScorecardResponse;
import com.cricklocal.service.ScorecardService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/matches")
public class ScorecardController {

    private final ScorecardService scorecardService;

    public ScorecardController(ScorecardService scorecardService) {
        this.scorecardService = scorecardService;
    }

    @GetMapping("/{matchId}/scorecard")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'SCORECARDS', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public ScorecardResponse getScorecard(
            @PathVariable Long matchId) {

        return scorecardService.getScorecard(matchId);
    }
}