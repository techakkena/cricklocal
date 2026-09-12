package com.cricklocal.controller;

import com.cricklocal.dto.CreatePlayerRequest;
import com.cricklocal.dto.PlayerResponse;
import com.cricklocal.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse createPlayer(
            @Valid @RequestBody CreatePlayerRequest request) {

        return playerService.createPlayer(request);
    }

    @GetMapping
    public List<PlayerResponse> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/{playerId}")
    public PlayerResponse getPlayerById(
            @PathVariable Long playerId) {

        return playerService.getPlayerById(playerId);
    }
}