package com.cricklocal.controller;

import com.cricklocal.dto.CreatePlayerRequest;
import com.cricklocal.entity.Player;
import com.cricklocal.repository.PlayerRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerRepository playerRepository;

    public PlayerController(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Player createPlayer(
            @Valid @RequestBody CreatePlayerRequest request) {

        Player player = new Player();

        player.setFirstName(request.getFirstName());
        player.setLastName(request.getLastName());
        player.setDisplayName(request.getDisplayName());
        player.setPhone(request.getPhone());
        player.setBattingStyle(request.getBattingStyle());
        player.setBowlingStyle(request.getBowlingStyle());
        player.setRole(request.getRole());

        return playerRepository.save(player);
    }
    @GetMapping
    public java.util.List<Player> getAllPlayers() {
        return playerRepository.findAll();
}
}