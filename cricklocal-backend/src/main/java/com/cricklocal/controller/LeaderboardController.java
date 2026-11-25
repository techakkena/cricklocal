package com.cricklocal.controller;

import com.cricklocal.dto.LeaderboardResponse;
import com.cricklocal.service.LeaderboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/leaderboards")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(
            LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/top-runs")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'STATISTICS', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public LeaderboardResponse getTopRunScorers() {
        return leaderboardService.getTopRunScorers();
    }

    @GetMapping("/top-wickets")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'STATISTICS', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public LeaderboardResponse getTopWicketTakers() {
        return leaderboardService.getTopWicketTakers();
    }

    @GetMapping("/top-fielding")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'STATISTICS', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public LeaderboardResponse getTopFieldingPlayers() {
        return leaderboardService.getTopFieldingPlayers();
    }
}