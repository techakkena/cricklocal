package com.cricklocal.controller;

import com.cricklocal.dto.InningsResponse;
import com.cricklocal.dto.StartInningsRequest;
import com.cricklocal.service.InningsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public class InningsController {

    private final InningsService inningsService;

    public InningsController(InningsService inningsService) {
        this.inningsService = inningsService;
    }

    @PostMapping("/{matchId}/innings")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'LIVE_SCORING', T(com.cricklocal.entity.PermissionAction).CREATE)")
    public InningsResponse startInnings(
            @PathVariable Long matchId,
            @Valid @RequestBody StartInningsRequest request) {

        return inningsService.startInnings(
                matchId,
                request
        );
    }

    @GetMapping("/{matchId}/innings")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'LIVE_SCORING', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<InningsResponse> getMatchInnings(
            @PathVariable Long matchId) {

        return inningsService.getMatchInnings(matchId);
    }

    @GetMapping("/innings/{inningsId}")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'LIVE_SCORING', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public InningsResponse getInningsById(
            @PathVariable Long inningsId) {

        return inningsService.getInningsById(inningsId);
    }

    @PostMapping("/innings/{inningsId}/declare")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'LIVE_SCORING', T(com.cricklocal.entity.PermissionAction).EDIT)")
    public InningsResponse declareInnings(
            @PathVariable Long inningsId) {

        return inningsService.declareInnings(inningsId);
    }

    @PostMapping("/innings/{inningsId}/abandon")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'LIVE_SCORING', T(com.cricklocal.entity.PermissionAction).EDIT)")
    public InningsResponse abandonInnings(
            @PathVariable Long inningsId) {

        return inningsService.abandonInnings(inningsId);
    }
}