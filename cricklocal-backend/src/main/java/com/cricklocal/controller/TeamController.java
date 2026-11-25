package com.cricklocal.controller;

import com.cricklocal.dto.CreateTeamRequest;
import com.cricklocal.dto.TeamImportResponse;
import com.cricklocal.dto.TeamResponse;
import com.cricklocal.entity.Team;
import com.cricklocal.repository.TeamRepository;
import com.cricklocal.service.TeamImportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'TEAMS', T(com.cricklocal.entity.PermissionAction).CREATE)")
    public TeamResponse createTeam(@Valid @RequestBody CreateTeamRequest request) {

        Team team = new Team();

        team.setName(request.getName());
        team.setShortName(request.getShortName());
        team.setCity(request.getCity());

        return toTeamResponse(teamRepository.save(team));
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'TEAMS', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll()
                .stream()
                .map(this::toTeamResponse)
                .toList();
    }

    @PostMapping("/import/validate")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'TEAMS', T(com.cricklocal.entity.PermissionAction).CREATE)")
    public TeamImportResponse validateTeamImport(
            @RequestParam("file") MultipartFile file) {

        return teamImportService.validateExcel(file);
    }

    @PostMapping("/import")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'TEAMS', T(com.cricklocal.entity.PermissionAction).CREATE)")
    public TeamImportResponse importTeams(
            @RequestParam("file") MultipartFile file) {

        return teamImportService.importExcel(file);
    }

    private TeamResponse toTeamResponse(Team team) {
        TeamResponse response = new TeamResponse();

        response.setId(team.getId());
        response.setName(team.getName());
        response.setShortName(team.getShortName());
        response.setCity(team.getCity());
        response.setActive(team.getActive());
        response.setCreatedAt(team.getCreatedAt());

        return response;
    }
}