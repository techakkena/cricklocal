package com.cricklocal.controller;

import com.cricklocal.dto.CreateTeamRequest;
import com.cricklocal.entity.Team;
import com.cricklocal.repository.TeamRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;

    public TeamController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Team createTeam(@Valid @RequestBody CreateTeamRequest request) {

        Team team = new Team();

        team.setName(request.getName());
        team.setShortName(request.getShortName());
        team.setCity(request.getCity());

        return teamRepository.save(team);
    }

    @GetMapping
    public java.util.List<Team> getAllTeams() {
        return teamRepository.findAll();
}
}