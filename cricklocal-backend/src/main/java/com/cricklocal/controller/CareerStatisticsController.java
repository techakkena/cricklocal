package com.cricklocal.controller;

import com.cricklocal.dto.CareerStatsResponse;
import com.cricklocal.service.CareerStatisticsService;
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
    public CareerStatsResponse getCareerStats(
            @PathVariable Long playerId) {

        return careerStatisticsService.getCareerStats(playerId);
    }
}