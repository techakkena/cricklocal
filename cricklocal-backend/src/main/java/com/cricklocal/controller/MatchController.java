package com.cricklocal.controller;

import com.cricklocal.dto.CreateMatchRequest;
import com.cricklocal.dto.MatchResponse;
import com.cricklocal.entity.Match;
import com.cricklocal.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Match createMatch(
            @Valid @RequestBody CreateMatchRequest request) {

        return matchService.createMatch(request);
    }

    @GetMapping
    public List<MatchResponse> getAllMatches() {
        return matchService.getAllMatches();
    }

    @GetMapping("/history")
    public List<MatchResponse> getMatchHistory() {
        return matchService.getMatchHistory();
    }

    @GetMapping("/{matchId}")
    public MatchResponse getMatchById(
            @PathVariable Long matchId) {

        return matchService.getMatchById(matchId);
    }
}