package com.cricklocal.controller;

import com.cricklocal.dto.InningsStateResponse;
import com.cricklocal.dto.SetInningsStateRequest;
import com.cricklocal.service.InningsStateService;
import com.cricklocal.service.ScoreOperatorAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/innings")
public class InningsStateController {

    private final InningsStateService inningsStateService;
    private final ScoreOperatorAuthorizationService operatorAuthorizationService;

    public InningsStateController(
            InningsStateService inningsStateService,
            ScoreOperatorAuthorizationService operatorAuthorizationService) {

        this.inningsStateService = inningsStateService;
        this.operatorAuthorizationService =
                operatorAuthorizationService;
    }

    @PostMapping("/{inningsId}/state")
    @ResponseStatus(HttpStatus.CREATED)
    public InningsStateResponse setState(
            @PathVariable Long inningsId,
            @RequestHeader(
                    name = "X-Score-Operator-Session",
                    required = false
            )
            String sessionToken,
            @Valid @RequestBody SetInningsStateRequest request) {

        operatorAuthorizationService.requireSessionForInnings(
                sessionToken,
                inningsId
        );

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