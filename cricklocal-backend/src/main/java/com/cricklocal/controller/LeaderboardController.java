package com.cricklocal.controller;

import com.cricklocal.dto.LeaderboardResponse;
import com.cricklocal.service.LeaderboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboards")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(
            LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/top-runs")
    public LeaderboardResponse getTopRunScorers() {
        return leaderboardService.getTopRunScorers();
    }

    @GetMapping("/top-wickets")
    public LeaderboardResponse getTopWicketTakers() {
        return leaderboardService.getTopWicketTakers();
    }

    @GetMapping("/top-fielding")
    public LeaderboardResponse getTopFieldingPlayers() {
        return leaderboardService.getTopFieldingPlayers();
    }
}