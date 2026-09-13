package com.cricklocal.controller;

import com.cricklocal.dto.CreatePlayerRequest;
import com.cricklocal.dto.PlayerImportResponse;
import com.cricklocal.dto.PlayerResponse;
import com.cricklocal.service.PlayerImportService;
import com.cricklocal.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;
    private final PlayerImportService playerImportService;

    public PlayerController(
            PlayerService playerService,
            PlayerImportService playerImportService) {

        this.playerService = playerService;
        this.playerImportService = playerImportService;
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

    @PostMapping("/import/validate")
    public PlayerImportResponse validatePlayerImport(
            @RequestParam("file") MultipartFile file) {

        return playerImportService.validateExcel(file);
    }

    @PostMapping("/import")
    public PlayerImportResponse importPlayers(
            @RequestParam("file") MultipartFile file) {

        return playerImportService.importExcel(file);
    }
}