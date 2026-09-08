package com.cricklocal.controller;

import com.cricklocal.dto.PlayerMatchHistoryResponse;
import com.cricklocal.service.PlayerHistoryService;
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
    public List<PlayerMatchHistoryResponse> getPlayerMatchHistory(
            @PathVariable Long playerId) {

        return playerHistoryService.getPlayerMatchHistory(playerId);
    }
}