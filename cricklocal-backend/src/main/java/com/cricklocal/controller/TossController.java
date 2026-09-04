package com.cricklocal.controller;

import com.cricklocal.dto.RecordTossRequest;
import com.cricklocal.dto.TossResponse;
import com.cricklocal.service.TossService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class TossController {

    private final TossService tossService;

    public TossController(TossService tossService) {
        this.tossService = tossService;
    }

    @PostMapping("/{matchId}/toss")
    @ResponseStatus(HttpStatus.CREATED)
    public TossResponse recordToss(
            @PathVariable Long matchId,
            @Valid @RequestBody RecordTossRequest request) {

        return tossService.recordToss(matchId, request);
    }

    @GetMapping("/{matchId}/toss")
    public TossResponse getToss(
            @PathVariable Long matchId) {

        return tossService.getTossByMatchId(matchId);
    }
}