package com.cricklocal.controller;

import com.cricklocal.dto.AddPlayerToMatchRequest;
import com.cricklocal.dto.MatchLineupResponse;
import com.cricklocal.service.MatchLineupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchLineupController {

    private final MatchLineupService matchLineupService;

    public MatchLineupController(
            MatchLineupService matchLineupService) {

        this.matchLineupService = matchLineupService;
    }

    @PostMapping("/{matchId}/lineup")
    @ResponseStatus(HttpStatus.CREATED)
    public MatchLineupResponse addPlayerToMatch(
            @PathVariable Long matchId,
            @Valid @RequestBody AddPlayerToMatchRequest request) {

        return matchLineupService.addPlayerToMatch(
                matchId,
                request
        );
    }

    @GetMapping("/{matchId}/lineup")
    public List<MatchLineupResponse> getMatchLineup(
            @PathVariable Long matchId) {

        return matchLineupService.getMatchLineup(matchId);
    }

    @GetMapping("/{matchId}/lineup/team/{teamId}")
    public List<MatchLineupResponse> getTeamMatchLineup(
            @PathVariable Long matchId,
            @PathVariable Long teamId) {

        return matchLineupService.getTeamMatchLineup(
                matchId,
                teamId
        );
    }
}