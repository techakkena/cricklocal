package com.cricklocal.controller;

import com.cricklocal.dto.CreateTeamRequest;
import com.cricklocal.dto.TeamImportResponse;
import com.cricklocal.entity.Team;
import com.cricklocal.repository.TeamRepository;
import com.cricklocal.service.TeamImportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;
    private final TeamImportService teamImportService;

    public TeamController(
            TeamRepository teamRepository,
            TeamImportService teamImportService) {

        this.teamRepository = teamRepository;
        this.teamImportService = teamImportService;
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

    @PostMapping("/import/validate")
    public TeamImportResponse validateTeamImport(
            @RequestParam("file") MultipartFile file) {

        return teamImportService.validateExcel(file);
    }
    @PostMapping("/import")
    public TeamImportResponse importTeams(
            @RequestParam("file") MultipartFile file) {

        return teamImportService.importExcel(file);
    }
}