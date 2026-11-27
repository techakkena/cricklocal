package com.cricklocal.controller;

import com.cricklocal.dto.PlayerMatchHistoryResponse;
import com.cricklocal.service.PlayerHistoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerHistoryController {

    private final PlayerHistoryService playerHistoryService;

    public PlayerHistoryController(
            PlayerHistoryService playerHistoryService) {

        this.playerHistoryService = playerHistoryService;
    }

    @GetMapping("/{playerId}/history")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'HISTORY', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<PlayerMatchHistoryResponse> getPlayerMatchHistory(
            @PathVariable Long playerId) {

        return playerHistoryService.getPlayerMatchHistory(playerId);
    }
}