package com.cricklocal.controller;

import com.cricklocal.dto.InningsResponse;
import com.cricklocal.dto.StartInningsRequest;
import com.cricklocal.service.InningsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public InningsResponse startInnings(
            @PathVariable Long matchId,
            @Valid @RequestBody StartInningsRequest request) {

        return inningsService.startInnings(
                matchId,
                request
        );
    }

    @GetMapping("/{matchId}/innings")
    public List<InningsResponse> getMatchInnings(
            @PathVariable Long matchId) {

        return inningsService.getMatchInnings(matchId);
    }

    @GetMapping("/innings/{inningsId}")
    public InningsResponse getInningsById(
            @PathVariable Long inningsId) {

        return inningsService.getInningsById(inningsId);
    }
}