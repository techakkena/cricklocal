package com.cricklocal.controller;

import com.cricklocal.dto.DeliveryResponse;
import com.cricklocal.dto.RecordDeliveryRequest;
import com.cricklocal.service.DeliveryService;
import com.cricklocal.service.ScoreOperatorAuthorizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    public DeliveryResponse recordDelivery(
            @PathVariable Long inningsId,
            @RequestHeader(
                    name = "X-Score-Operator-Session",
                    required = false
            )
            String sessionToken,
            @Valid @RequestBody RecordDeliveryRequest request) {

        operatorAuthorizationService.requireSessionForInnings(
                sessionToken,
                inningsId
        );

        return deliveryService.recordDelivery(
                inningsId,
                request);
    }

    @GetMapping("/{inningsId}/deliveries")
    public List<DeliveryResponse> getDeliveries(
            @PathVariable Long inningsId) {

        return deliveryService.getDeliveries(inningsId);
    }

    @PostMapping("/{inningsId}/deliveries/undo")
    public void undoLastDelivery(
            @PathVariable Long inningsId,
            @RequestHeader(
                    name = "X-Score-Operator-Session",
                    required = false
            )
            String sessionToken) {

        operatorAuthorizationService.requireSessionForInnings(
                sessionToken,
                inningsId
        );

        deliveryService.undoLastDelivery(inningsId);
    }
}