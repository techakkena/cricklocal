package com.cricklocal.controller;

import com.cricklocal.dto.InningsStateResponse;
import com.cricklocal.dto.SetInningsStateRequest;
import com.cricklocal.service.InningsStateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/innings")
public class InningsStateController {

    private final InningsStateService inningsStateService;

    public InningsStateController(
            InningsStateService inningsStateService) {
        this.inningsStateService = inningsStateService;
    }

    @PostMapping("/{inningsId}/state")
    @ResponseStatus(HttpStatus.CREATED)
    public InningsStateResponse setState(
            @PathVariable Long inningsId,
            @Valid @RequestBody SetInningsStateRequest request) {

        return inningsStateService.setState(
                inningsId,
                request);
    }

    @GetMapping("/{inningsId}/state")
    public InningsStateResponse getState(
            @PathVariable Long inningsId) {

        return inningsStateService.getState(inningsId);
    }
}