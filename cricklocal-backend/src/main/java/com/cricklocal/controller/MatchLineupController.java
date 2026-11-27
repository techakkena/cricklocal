package com.cricklocal.controller;

import com.cricklocal.dto.AddPlayerToMatchRequest;
import com.cricklocal.dto.MatchLineupResponse;
import com.cricklocal.dto.PlayingXIResponse;
import com.cricklocal.dto.ReplaceMatchPlayerRequest;
import com.cricklocal.service.MatchLineupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'MATCH_PREPARATION', T(com.cricklocal.entity.PermissionAction).EDIT)")
    public MatchLineupResponse addPlayerToMatch(
            @PathVariable Long matchId,
            @Valid @RequestBody AddPlayerToMatchRequest request) {

        return matchLineupService.addPlayerToMatch(
                matchId,
                request
        );
    }

    @GetMapping("/{matchId}/lineup")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'MATCH_PREPARATION', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<MatchLineupResponse> getMatchLineup(
            @PathVariable Long matchId) {

        return matchLineupService.getMatchLineup(matchId);
    }

    @GetMapping("/{matchId}/lineup/team/{teamId}")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'MATCH_PREPARATION', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<MatchLineupResponse> getTeamMatchLineup(
            @PathVariable Long matchId,
            @PathVariable Long teamId) {

        return matchLineupService.getTeamMatchLineup(
                matchId,
                teamId
        );
    }

    @GetMapping("/{matchId}/lineup/team/{teamId}/finalized")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'MATCH_PREPARATION', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public PlayingXIResponse getFinalizedPlayingXI(
            @PathVariable Long matchId,
            @PathVariable Long teamId) {

        return matchLineupService.getFinalizedPlayingXI(
                matchId,
                teamId
        );
    }

    @PostMapping("/{matchId}/lineup/team/{teamId}/finalize")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'MATCH_PREPARATION', T(com.cricklocal.entity.PermissionAction).EDIT)")
    public PlayingXIResponse finalizePlayingXI(
            @PathVariable Long matchId,
            @PathVariable Long teamId) {

        return matchLineupService.finalizePlayingXI(
                matchId,
                teamId
        );
    }

    @PostMapping("/{matchId}/lineup/team/{teamId}/replace")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'MATCH_PREPARATION', T(com.cricklocal.entity.PermissionAction).EDIT)")
    public MatchLineupResponse replacePlayer(
            @PathVariable Long matchId,
            @PathVariable Long teamId,
            @Valid @RequestBody ReplaceMatchPlayerRequest request) {

        return matchLineupService.replacePlayer(
                matchId,
                teamId,
                request
        );
    }
}