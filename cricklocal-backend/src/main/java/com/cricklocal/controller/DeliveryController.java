package com.cricklocal.controller;

import com.cricklocal.dto.DeliveryResponse;
import com.cricklocal.dto.RecordDeliveryRequest;
import com.cricklocal.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/innings")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(
            DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @PostMapping("/{inningsId}/deliveries")
    @ResponseStatus(HttpStatus.CREATED)
    public DeliveryResponse recordDelivery(
            @PathVariable Long inningsId,
            @Valid @RequestBody RecordDeliveryRequest request) {

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
            @PathVariable Long inningsId) {

        deliveryService.undoLastDelivery(inningsId);
    }
}