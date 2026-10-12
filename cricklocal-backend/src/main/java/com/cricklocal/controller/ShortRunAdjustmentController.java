package com.cricklocal.controller;

import com.cricklocal.dto.ShortRunAdjustmentRequest;
import com.cricklocal.dto.ShortRunAdjustmentResponse;
import com.cricklocal.service.ScoreOperatorAuthorizationService;
import com.cricklocal.service.ShortRunAdjustmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/innings")
public class ShortRunAdjustmentController {

    private final ShortRunAdjustmentService adjustmentService;
    private final ScoreOperatorAuthorizationService authorizationService;

    public ShortRunAdjustmentController(
            ShortRunAdjustmentService adjustmentService,
            ScoreOperatorAuthorizationService authorizationService) {
        this.adjustmentService = adjustmentService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/{inningsId}/short-run-adjustments")
    @ResponseStatus(HttpStatus.CREATED)
    public ShortRunAdjustmentResponse deductRuns(
            @PathVariable Long inningsId,
            @RequestHeader(
                    name = "X-Score-Operator-Session",
                    required = false) String sessionToken,
            @Valid @RequestBody ShortRunAdjustmentRequest request) {

        authorizationService.requireSessionForInnings(
                sessionToken, inningsId);

        return adjustmentService.deductRuns(inningsId, request);
    }
}