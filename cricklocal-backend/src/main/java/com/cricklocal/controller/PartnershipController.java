package com.cricklocal.controller;

import com.cricklocal.dto.PartnershipResponse;
import com.cricklocal.entity.Innings;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.service.PartnershipService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/innings")
public class PartnershipController {

    private final InningsRepository inningsRepository;
    private final PartnershipService partnershipService;

    public PartnershipController(
            InningsRepository inningsRepository,
            PartnershipService partnershipService) {

        this.inningsRepository = inningsRepository;
        this.partnershipService = partnershipService;
    }

    @GetMapping("/{inningsId}/partnerships")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'LIVE_SCORING', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<PartnershipResponse> getPartnerships(
            @PathVariable Long inningsId) {

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Innings not found: " + inningsId));

        return partnershipService
                .getResponsesByInnings(innings);
    }
}