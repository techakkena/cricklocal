package com.cricklocal.controller;

import com.cricklocal.dto.FallOfWicketResponse;
import com.cricklocal.entity.Innings;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.service.FallOfWicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/innings")
public class FallOfWicketController {

    private final InningsRepository inningsRepository;
    private final FallOfWicketService fallOfWicketService;

    public FallOfWicketController(
            InningsRepository inningsRepository,
            FallOfWicketService fallOfWicketService) {

        this.inningsRepository = inningsRepository;
        this.fallOfWicketService = fallOfWicketService;
    }

    @GetMapping("/{inningsId}/fall-of-wickets")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'LIVE_SCORING', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<FallOfWicketResponse> getFallOfWickets(
            @PathVariable Long inningsId) {

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Innings not found: " + inningsId));

        return fallOfWicketService
                .getResponsesByInnings(innings);
    }
}