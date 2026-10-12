package com.cricklocal.service;

import com.cricklocal.dto.ShortRunAdjustmentRequest;
import com.cricklocal.dto.ShortRunAdjustmentResponse;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.ShortRunAdjustment;
import com.cricklocal.entity.Delivery;
import com.cricklocal.enums.MatchStatus;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.ShortRunAdjustmentRepository;
import com.cricklocal.repository.DeliveryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ShortRunAdjustmentService {

    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;
    private final ShortRunAdjustmentRepository adjustmentRepository;
    private final DeliveryRepository deliveryRepository;

    public ShortRunAdjustmentService(
            MatchRepository matchRepository,
            InningsRepository inningsRepository,
            ShortRunAdjustmentRepository adjustmentRepository,
            DeliveryRepository deliveryRepository) {
        this.matchRepository = matchRepository;
        this.inningsRepository = inningsRepository;
        this.adjustmentRepository = adjustmentRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public ShortRunAdjustmentResponse deductRuns(
            Long inningsId,
            ShortRunAdjustmentRequest request) {

        if (request == null || request.getRunsDeducted() == null) {
            throw new IllegalArgumentException(
                    "Runs deducted is required");
        }

        int deduction = request.getRunsDeducted();

        if (deduction < 1 || deduction > 5) {
            throw new IllegalArgumentException(
                    "You can deduct between 1 and 5 runs at a time");
        }

        Innings initialInnings = inningsRepository.findById(inningsId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Innings not found: " + inningsId));

        Long matchId = initialInnings.getMatch().getId();

        Match match = matchRepository.findByIdForUpdate(matchId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Match not found: " + matchId));

        if (match.getStatus() == MatchStatus.COMPLETED) {
            throw new IllegalArgumentException(
                    "Cannot adjust runs after the match is completed");
        }

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Innings not found: " + inningsId));

        int currentTotal = innings.getTotalRuns() == null
                ? 0 : innings.getTotalRuns();

        if (currentTotal < deduction) {
            throw new IllegalArgumentException(
                    "Cannot deduct " + deduction
                            + " runs from a total of " + currentTotal);
        }

        ShortRunAdjustment adjustment = new ShortRunAdjustment();
        Delivery currentDelivery =
        deliveryRepository
                .findTopByInningsOrderByDeliveryNumberDesc(innings)
                .orElse(null);

        adjustment.setDelivery(currentDelivery);
        adjustment.setInnings(innings);
        adjustment.setRunsDeducted(deduction);

        Integer adjustmentOver =
                currentDelivery != null
                        ? currentDelivery.getOverNumber()
                        : null;

        adjustment.setOverNumber(adjustmentOver);

        String reason = request.getReason();
        adjustment.setReason(
                reason == null || reason.isBlank()
                        ? "Short runs penalty"
                        : reason.trim());

        ShortRunAdjustment saved =
                adjustmentRepository.save(adjustment);

        int updatedTotal = currentTotal - deduction;
        innings.setTotalRuns(updatedTotal);
        inningsRepository.save(innings);

        ShortRunAdjustmentResponse response =
                new ShortRunAdjustmentResponse();
        response.setId(saved.getId());
        response.setInningsId(inningsId);
        response.setRunsDeducted(deduction);
        response.setReason(saved.getReason());
        response.setCreatedAt(saved.getCreatedAt());
        response.setUpdatedTotalRuns(updatedTotal);

        return response;
    }
}