package com.cricklocal.controller;

import com.cricklocal.dto.CareerStatsResponse;
import com.cricklocal.service.CareerStatisticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/players")
public class CareerStatisticsController {

    private final CareerStatisticsService careerStatisticsService;

    public CareerStatisticsController(
            CareerStatisticsService careerStatisticsService) {

        this.careerStatisticsService = careerStatisticsService;
    }

    @GetMapping("/{playerId}/career-stats")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'STATISTICS', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public CareerStatsResponse getCareerStats(
            @PathVariable Long playerId) {

        return careerStatisticsService.getCareerStats(playerId);
    }
}