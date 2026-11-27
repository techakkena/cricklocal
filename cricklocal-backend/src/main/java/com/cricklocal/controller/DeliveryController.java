package com.cricklocal.controller;

import com.cricklocal.dto.DeliveryResponse;
import com.cricklocal.dto.RecordDeliveryRequest;
import com.cricklocal.service.DeliveryService;
import com.cricklocal.service.ScoreOperatorAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/innings")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final ScoreOperatorAuthorizationService operatorAuthorizationService;

    public DeliveryController(
            DeliveryService deliveryService,
            ScoreOperatorAuthorizationService operatorAuthorizationService) {

        this.deliveryService = deliveryService;
        this.operatorAuthorizationService =
                operatorAuthorizationService;
    }

    @PostMapping("/{inningsId}/deliveries")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'LIVE_SCORING', T(com.cricklocal.entity.PermissionAction).CREATE)")
    public DeliveryResponse recordDelivery(
            @PathVariable Long inningsId,
            @RequestHeader(
                    name = "X-Score-Operator-Session",
                    required = false
            )
            String sessionToken,
            @Valid @RequestBody RecordDeliveryRequest request) {

        /*
         * Permission check above controls whether the authenticated
         * user has LIVE_SCORING.CREATE access.
         *
         * Operator session validation remains mandatory for the
         * actual live-scoring operation.
         */
        operatorAuthorizationService.requireSessionForInnings(
                sessionToken,
                inningsId
        );

        return deliveryService.recordDelivery(
                inningsId,
                request);
    }

    @GetMapping("/{inningsId}/deliveries")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'LIVE_SCORING', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<DeliveryResponse> getDeliveries(
            @PathVariable Long inningsId) {

        return deliveryService.getDeliveries(inningsId);
    }

    @PostMapping("/{inningsId}/deliveries/undo")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'LIVE_SCORING', T(com.cricklocal.entity.PermissionAction).DELETE)")
    public void undoLastDelivery(
            @PathVariable Long inningsId,
            @RequestHeader(
                    name = "X-Score-Operator-Session",
                    required = false
            )
            String sessionToken) {

        /*
         * Undo is treated as a DELETE-style live-scoring action.
         * The existing operator-session authorization remains
         * mandatory.
         */
        operatorAuthorizationService.requireSessionForInnings(
                sessionToken,
                inningsId
        );

        deliveryService.undoLastDelivery(inningsId);
    }
}