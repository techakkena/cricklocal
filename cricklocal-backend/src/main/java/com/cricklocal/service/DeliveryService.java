package com.cricklocal.service;

import com.cricklocal.dto.DeliveryResponse;
import com.cricklocal.dto.RecordDeliveryRequest;
import com.cricklocal.entity.BattingInnings;
import com.cricklocal.entity.BowlingInnings;
import com.cricklocal.entity.Delivery;
import com.cricklocal.entity.FieldingEvent;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.InningsState;
import com.cricklocal.entity.MatchLineup;
import com.cricklocal.entity.Partnership;
import com.cricklocal.entity.Player;
import com.cricklocal.enums.DeliveryResult;
import com.cricklocal.enums.DismissalEnd;
import com.cricklocal.enums.ExtraType;
import com.cricklocal.enums.InningsStatus;
import com.cricklocal.enums.WicketType;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.DeliveryRepository;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.repository.InningsStateRepository;
import com.cricklocal.repository.MatchLineupRepository;
import com.cricklocal.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cricklocal.repository.FieldingEventRepository;


@Service
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final InningsRepository inningsRepository;
    private final PlayerRepository playerRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final InningsStateRepository inningsStateRepository;
    private final BattingInningsService battingInningsService;
    private final BowlingInningsService bowlingInningsService;
    private final FieldingEventRepository fieldingEventRepository;
    private final PartnershipService partnershipService;
    private final FallOfWicketService fallOfWicketService;

    public DeliveryService(
                DeliveryRepository deliveryRepository,
                InningsRepository inningsRepository,
                PlayerRepository playerRepository,
                MatchLineupRepository matchLineupRepository,
                InningsStateRepository inningsStateRepository,
                BattingInningsService battingInningsService,
                BowlingInningsService bowlingInningsService,
                FieldingEventRepository fieldingEventRepository,
                PartnershipService partnershipService,
                FallOfWicketService fallOfWicketService) {

                this.deliveryRepository = deliveryRepository;
                this.inningsRepository = inningsRepository;
                this.playerRepository = playerRepository;
                this.matchLineupRepository = matchLineupRepository;
                this.inningsStateRepository = inningsStateRepository;
                this.battingInningsService = battingInningsService;
                this.bowlingInningsService = bowlingInningsService;
                this.fieldingEventRepository = fieldingEventRepository;
                this.partnershipService = partnershipService;
                this.fallOfWicketService = fallOfWicketService;
    }

    @Transactional
    public DeliveryResponse recordDelivery(
                        Long inningsId,
                        RecordDeliveryRequest request) {

                // 1. Find innings
                Innings innings = inningsRepository.findById(inningsId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Innings not found"));

                // 2. Innings must be live
                if (innings.getStatus() != InningsStatus.LIVE) {
                        throw new IllegalArgumentException(
                                "Innings is not live");
                }

                // 3. Find batter
                Player batter = playerRepository.findById(
                                request.getBatterId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Batter not found"));

                // 4. Find bowler
                Player bowler = playerRepository.findById(
                                request.getBowlerId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Bowler not found"));

                // 5. Find current innings state
                InningsState state = inningsStateRepository
                        .findByInnings(innings)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Innings state not found"));

                // 6. Batter must be the current striker
                if (state.getStriker() == null
                        || !state.getStriker().getId()
                        .equals(batter.getId())) {

                        throw new IllegalArgumentException(
                                "Batter is not the current striker");
                }

                // 7. Bowler must be the current bowler
                if (state.getCurrentBowler() == null
                        || !state.getCurrentBowler().getId()
                        .equals(bowler.getId())) {

                        throw new IllegalArgumentException(
                                "Bowler is not the current bowler");
                }

                // 8. Batter and bowler must be different
                if (batter.getId().equals(bowler.getId())) {
                        throw new IllegalArgumentException(
                                "Batter and bowler must be different players");
                }

                // 9. Verify batter is in the match lineup
                MatchLineup batterLineup =
                        matchLineupRepository
                                .findByMatchAndPlayer(
                                        innings.getMatch(),
                                        batter)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Batter is not in the match lineup"));

                // 10. Verify bowler is in the match lineup
                MatchLineup bowlerLineup =
                        matchLineupRepository
                                .findByMatchAndPlayer(
                                        innings.getMatch(),
                                        bowler)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Bowler is not in the match lineup"));

                // 11. Batter must belong to batting team
                if (!batterLineup.getTeam().getId()
                        .equals(innings.getBattingTeam().getId())) {

                        throw new IllegalArgumentException(
                                "Batter does not belong to the batting team");
                }

                // 12. Bowler must belong to bowling team
                if (!bowlerLineup.getTeam().getId()
                        .equals(innings.getBowlingTeam().getId())) {

                        throw new IllegalArgumentException(
                                "Bowler does not belong to the bowling team");
                }

                // 13. Batter must be playing
                if (!batterLineup.getPlaying()) {
                        throw new IllegalArgumentException(
                                "Batter is not in the playing XI");
                }

                // 14. Bowler must be playing
                if (!bowlerLineup.getPlaying()) {
                        throw new IllegalArgumentException(
                                "Bowler is not in the playing XI");
                }

                // 15. Validate extra runs
                validateExtras(request);

                // 16. Validate wicket information
                validateWicket(request, innings);

                // 17. Calculate delivery values
                boolean legalDelivery =
                        request.getExtraType() != ExtraType.WIDE
                                && request.getExtraType() != ExtraType.NO_BALL;

                int totalRuns =
                        request.getRunsOffBat()
                                + request.getExtraRuns();

                DeliveryResult result =
                        determineDeliveryResult(request);

                // 18. Calculate delivery position
                int deliveryNumber =
                        (int) deliveryRepository.countByInnings(innings) + 1;

                int legalBallsBefore =
                        innings.getLegalBalls();

                int overNumber =
                        (legalBallsBefore / 6) + 1;

                int ballInOver =
                        (legalBallsBefore % 6) + 1;

                // 19. Create delivery
                Delivery delivery = new Delivery();

                delivery.setInnings(innings);
                delivery.setBatter(batter);
                delivery.setBowler(bowler);

                delivery.setDeliveryNumber(deliveryNumber);
                delivery.setOverNumber(overNumber);
                delivery.setBallInOver(ballInOver);

                delivery.setLegalDelivery(legalDelivery);

                delivery.setRunsOffBat(
                        request.getRunsOffBat());

                delivery.setExtraRuns(
                        request.getExtraRuns());

                delivery.setTotalRuns(totalRuns);

                delivery.setExtraType(
                        request.getExtraType());

                delivery.setResult(result);

                delivery.setWicket(
                        request.getWicket());

                delivery.setWicketType(
                        request.getWicketType());

                delivery.setDismissalEnd(
                        request.getDismissalEnd());

                // 20. Set dismissed player
                Player dismissedPlayer = null;

                if (request.getDismissedPlayerId() != null) {

                        dismissedPlayer =
                                playerRepository.findById(
                                        request.getDismissedPlayerId())
                                        .orElseThrow(() ->
                                                new ResourceNotFoundException(
                                                        "Dismissed player not found"));

                        delivery.setDismissedPlayer(
                                dismissedPlayer);
                }

                // 21. Set fielder
                if (request.getFielderId() != null) {

                        Player fielder =
                                playerRepository.findById(
                                        request.getFielderId())
                                        .orElseThrow(() ->
                                                new ResourceNotFoundException(
                                                        "Fielder not found"));

                        delivery.setFielder(fielder);
                }

                initializePartnershipIfNeeded(innings);

                // 22. Save delivery
                Delivery savedDelivery =
                        deliveryRepository.save(delivery);

                updatePartnership(innings, savedDelivery);

                // 23. Update innings score
                innings.setTotalRuns(
                        innings.getTotalRuns()
                                + savedDelivery.getTotalRuns());

                if (savedDelivery.getLegalDelivery()) {

                        innings.setLegalBalls(
                                innings.getLegalBalls() + 1);
                }

                if (savedDelivery.getWicket()) {

                        innings.setWickets(
                                innings.getWickets() + 1);
                }

                inningsRepository.save(innings);

                createFallOfWicket(innings, savedDelivery);

                // 24. Update batting statistics
                updateBattingStatistics(
                        innings,
                        savedDelivery);

                updateBowlingStatistics(innings, savedDelivery);

                createFieldingEvent(savedDelivery);

                // 25. Find new batter
                Player newBatter = null;

                if (Boolean.TRUE.equals(
                        savedDelivery.getWicket())
                        && request.getNewBatterId() != null) {

                        newBatter =
                                playerRepository.findById(
                                        request.getNewBatterId())
                                        .orElseThrow(() ->
                                                new ResourceNotFoundException(
                                                        "New batter not found"));
                }

                // 25. Update innings state
                updateInningsStateAfterDelivery(
                        state,
                        savedDelivery,
                        newBatter);

                if (Boolean.TRUE.equals(savedDelivery.getWicket())
                        && newBatter != null) {

                        initializePartnershipIfNeeded(innings);
                }

                // 26. Build response
                return toDeliveryResponse(
                        savedDelivery,
                        innings);
    }


    @Transactional(readOnly = true)
    public java.util.List<DeliveryResponse> getDeliveries(
                Long inningsId) {

        Innings innings = inningsRepository.findById(inningsId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Innings not found"));

        return deliveryRepository
                .findByInningsOrderByDeliveryNumberAsc(innings)
                .stream()
                .map(delivery ->
                        toDeliveryResponse(delivery, innings))
                .toList();
   }

    private void validateExtras(
            RecordDeliveryRequest request) {

        ExtraType extraType = request.getExtraType();
        Integer extraRuns = request.getExtraRuns();

        if (extraType == ExtraType.NONE && extraRuns != 0) {
            throw new IllegalArgumentException(
                    "Extra runs must be 0 when extra type is NONE");
        }

        if (extraType != ExtraType.NONE && extraRuns < 1) {
            throw new IllegalArgumentException(
                    "Extra runs must be at least 1 for an extra delivery");
        }
    }

    private void validateWicket(
                RecordDeliveryRequest request,
                Innings innings) {

        // --------------------------------------------------
        // No wicket
        // --------------------------------------------------
        if (!Boolean.TRUE.equals(request.getWicket())) {

                if (request.getWicketType() != null) {
                throw new IllegalArgumentException(
                        "Wicket type cannot be provided when there is no wicket");
                }

                if (request.getDismissedPlayerId() != null) {
                throw new IllegalArgumentException(
                        "Dismissed player cannot be provided when there is no wicket");
                }

                if (request.getNewBatterId() != null) {
                throw new IllegalArgumentException(
                        "New batter cannot be provided when there is no wicket");
                }

                if (request.getDismissalEnd() != null) {
                throw new IllegalArgumentException(
                        "Dismissal end cannot be provided when there is no wicket");
                }

                if (request.getFielderId() != null) {
                throw new IllegalArgumentException(
                        "Fielder cannot be provided when there is no wicket");
                }

                return;
        }

        // --------------------------------------------------
        // Wicket is present
        // --------------------------------------------------

        if (request.getWicketType() == null) {
                throw new IllegalArgumentException(
                        "Wicket type is required when there is a wicket");
        }

        if (request.getDismissedPlayerId() == null) {
                throw new IllegalArgumentException(
                        "Dismissed player is required when there is a wicket");
        }

        if (request.getNewBatterId() == null) {
                throw new IllegalArgumentException(
                        "New batter is required when there is a wicket");
        }

        // RUN_OUT can happen at either end, so the end
        // must be explicitly supplied.
        if (request.getWicketType() == WicketType.RUN_OUT
                && request.getDismissalEnd() == null) {

                throw new IllegalArgumentException(
                        "Dismissal end is required for a run-out");
        }

        // All other dismissal types are striker dismissals.
        if (request.getWicketType() != WicketType.RUN_OUT
                && request.getDismissalEnd() == null) {

                request.setDismissalEnd(
                        DismissalEnd.STRIKER);
        }

        // --------------------------------------------------
        // Find dismissed player
        // --------------------------------------------------

        Player dismissedPlayer =
                playerRepository.findById(
                        request.getDismissedPlayerId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Dismissed player not found"));

        // --------------------------------------------------
        // Validate dismissed player's lineup
        // --------------------------------------------------

        MatchLineup dismissedPlayerLineup =
                matchLineupRepository
                        .findByMatchAndPlayer(
                                innings.getMatch(),
                                dismissedPlayer)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Dismissed player is not in the match lineup"));

        if (!dismissedPlayerLineup.getTeam().getId()
                .equals(innings.getBattingTeam().getId())) {

                throw new IllegalArgumentException(
                        "Dismissed player does not belong to the batting team");
        }

        if (!dismissedPlayerLineup.getPlaying()) {
                throw new IllegalArgumentException(
                        "Dismissed player is not in the playing XI");
        }

        // --------------------------------------------------
        // Find new batter
        // --------------------------------------------------

        Player newBatter =
                playerRepository.findById(
                        request.getNewBatterId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "New batter not found"));

        if (newBatter.getId().equals(
                dismissedPlayer.getId())) {

                throw new IllegalArgumentException(
                        "New batter cannot be the dismissed player");
        }

        // --------------------------------------------------
        // Validate new batter lineup
        // --------------------------------------------------

        MatchLineup newBatterLineup =
                matchLineupRepository
                        .findByMatchAndPlayer(
                                innings.getMatch(),
                                newBatter)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "New batter is not in the match lineup"));

        if (!newBatterLineup.getTeam().getId()
                .equals(innings.getBattingTeam().getId())) {

                throw new IllegalArgumentException(
                        "New batter does not belong to the batting team");
        }

        if (!newBatterLineup.getPlaying()) {
                throw new IllegalArgumentException(
                        "New batter is not in the playing XI");
        }

        // --------------------------------------------------
        // Get current innings state
        // --------------------------------------------------

        InningsState state =
                inningsStateRepository
                        .findByInnings(innings)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Innings state not found"));

        // --------------------------------------------------
        // Validate dismissal position
        // --------------------------------------------------

        if (request.getDismissalEnd()
                == DismissalEnd.STRIKER) {

                if (state.getStriker() == null
                        || !state.getStriker().getId()
                        .equals(dismissedPlayer.getId())) {

                throw new IllegalArgumentException(
                        "Dismissed player is not the current striker");
                }
        }

        if (request.getDismissalEnd()
                == DismissalEnd.NON_STRIKER) {

                if (state.getNonStriker() == null
                        || !state.getNonStriker().getId()
                        .equals(dismissedPlayer.getId())) {

                throw new IllegalArgumentException(
                        "Dismissed player is not the current non-striker");
                }
        }

        // --------------------------------------------------
        // New batter cannot already occupy either end
        // --------------------------------------------------

        if (state.getStriker() != null
                && state.getStriker().getId()
                .equals(newBatter.getId())) {

                throw new IllegalArgumentException(
                        "New batter is already the current striker");
        }

        if (state.getNonStriker() != null
                && state.getNonStriker().getId()
                .equals(newBatter.getId())) {

                throw new IllegalArgumentException(
                        "New batter is already the current non-striker");
        }

        // --------------------------------------------------
        // Validate fielder
        // --------------------------------------------------

        if (request.getFielderId() != null) {

                Player fielder =
                        playerRepository.findById(
                                request.getFielderId())
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Fielder not found"));

                MatchLineup fielderLineup =
                        matchLineupRepository
                                .findByMatchAndPlayer(
                                        innings.getMatch(),
                                        fielder)
                                .orElseThrow(() ->
                                        new IllegalArgumentException(
                                                "Fielder is not in the match lineup"));

                if (!fielderLineup.getTeam().getId()
                        .equals(innings.getBowlingTeam().getId())) {

                throw new IllegalArgumentException(
                        "Fielder must belong to the bowling team");
                }

                if (!fielderLineup.getPlaying()) {
                throw new IllegalArgumentException(
                        "Fielder is not in the playing XI");
                }
        }
    }

    private DeliveryResult determineDeliveryResult(
            RecordDeliveryRequest request) {

        if (Boolean.TRUE.equals(request.getWicket())) {
            return DeliveryResult.WICKET;
        }

        if (request.getExtraType() != ExtraType.NONE) {
            return DeliveryResult.EXTRA;
        }

        if (request.getRunsOffBat() == 0) {
            return DeliveryResult.DOT_BALL;
        }

        if (request.getRunsOffBat() == 4) {
            return DeliveryResult.FOUR;
        }

        if (request.getRunsOffBat() == 6) {
            return DeliveryResult.SIX;
        }

        return DeliveryResult.RUNS;
    }

    private void updateBattingStatistics(
                        Innings innings,
                        Delivery delivery) {

                BattingInnings battingInnings =
                        battingInningsService.getOrCreate(
                                innings,
                                delivery.getBatter());

                int runs = delivery.getRunsOffBat();

                battingInnings.setRuns(
                        battingInnings.getRuns() + runs);

                // A batter faces a ball only when it is a legal delivery.
                if (delivery.getLegalDelivery()
                        && delivery.getExtraType() == ExtraType.NONE) {

                        battingInnings.setBallsFaced(
                                battingInnings.getBallsFaced() + 1);
                }

                if (runs == 4) {

                        battingInnings.setFours(
                                battingInnings.getFours() + 1);
                }

                if (runs == 6) {

                        battingInnings.setSixes(
                                battingInnings.getSixes() + 1);
                }

                if (delivery.getLegalDelivery()
                        && delivery.getExtraType() == ExtraType.NONE
                        && runs == 0) {

                        battingInnings.setDots(
                                battingInnings.getDots() + 1);
                }

                if (Boolean.TRUE.equals(delivery.getWicket())
                        && delivery.getDismissedPlayer() != null
                        && delivery.getDismissedPlayer().getId()
                                .equals(delivery.getBatter().getId())) {

                        battingInnings.setDismissed(true);

                        if (delivery.getWicketType() != null) {

                        battingInnings.setDismissalType(
                                delivery.getWicketType().name());
                        }

                        battingInnings.setDismissedByPlayer(
                                delivery.getFielder());
                }

                battingInningsService.save(battingInnings);
        }

    private void updateBowlingStatistics(
                        Innings innings,
                        Delivery delivery) {

                BowlingInnings bowlingInnings =
                        bowlingInningsService.getOrCreate(
                                innings,
                                delivery.getBowler());

                int runsConceded = delivery.getRunsOffBat();

                // Byes and leg-byes are not charged to the bowler.
                if (delivery.getExtraType() != ExtraType.BYE
                        && delivery.getExtraType() != ExtraType.LEG_BYE) {

                        runsConceded += delivery.getExtraRuns();
                }

                bowlingInnings.setRunsConceded(
                        bowlingInnings.getRunsConceded() + runsConceded);

                // Only legal deliveries count as balls bowled.
                if (delivery.getLegalDelivery()) {

                        int ballsBowled =
                                bowlingInnings.getBallsBowled() + 1;

                        bowlingInnings.setBallsBowled(ballsBowled);

                        bowlingInnings.setOvers(ballsBowled / 6);
                }

                if (delivery.getRunsOffBat() == 4) {

                        bowlingInnings.setFoursConceded(
                                bowlingInnings.getFoursConceded() + 1);
                }

                if (delivery.getRunsOffBat() == 6) {

                        bowlingInnings.setSixesConceded(
                                bowlingInnings.getSixesConceded() + 1);
                }

                if (delivery.getExtraType() == ExtraType.WIDE) {

                        bowlingInnings.setWides(
                                bowlingInnings.getWides() + delivery.getExtraRuns());
                }

                if (delivery.getExtraType() == ExtraType.NO_BALL) {

                        bowlingInnings.setNoBalls(
                                bowlingInnings.getNoBalls() + 1);
                }

                if (Boolean.TRUE.equals(delivery.getWicket())) {

                        WicketType wicketType =
                                delivery.getWicketType();

                        // These dismissals are credited to the bowler.
                        if (wicketType == WicketType.BOWLED
                                || wicketType == WicketType.CAUGHT
                                || wicketType == WicketType.LBW
                                || wicketType == WicketType.STUMPED
                                || wicketType == WicketType.HIT_WICKET) {

                        bowlingInnings.setWickets(
                                bowlingInnings.getWickets() + 1);
                        }
                }

                bowlingInningsService.save(bowlingInnings);
    }

    private DeliveryResponse toDeliveryResponse(
            Delivery delivery,
            Innings innings) {

        DeliveryResponse response = new DeliveryResponse();

        response.setId(delivery.getId());

        response.setInningsId(
                innings.getId());

        response.setInningsNumber(
                innings.getInningsNumber());

        response.setDeliveryNumber(
                delivery.getDeliveryNumber());

        response.setOverNumber(
                delivery.getOverNumber());

        response.setBallInOver(
                delivery.getBallInOver());

        response.setBatterId(
                delivery.getBatter().getId());

        response.setBatterName(
                delivery.getBatter().getDisplayName());

        response.setBowlerId(
                delivery.getBowler().getId());

        response.setBowlerName(
                delivery.getBowler().getDisplayName());

        response.setLegalDelivery(
                delivery.getLegalDelivery());

        response.setRunsOffBat(
                delivery.getRunsOffBat());

        response.setExtraRuns(
                delivery.getExtraRuns());

        response.setTotalRuns(
                delivery.getTotalRuns());

        response.setExtraType(
                delivery.getExtraType());

        response.setResult(
                delivery.getResult());

        response.setWicket(
                delivery.getWicket());

        response.setWicketType(
                delivery.getWicketType());

        response.setDismissalEnd(delivery.getDismissalEnd());

        if (delivery.getDismissedPlayer() != null) {

            response.setDismissedPlayerId(
                    delivery.getDismissedPlayer().getId());

            response.setDismissedPlayerName(
                    delivery.getDismissedPlayer().getDisplayName());
        }

        if (delivery.getFielder() != null) {

            response.setFielderId(
                    delivery.getFielder().getId());

            response.setFielderName(
                    delivery.getFielder().getDisplayName());
        }

        response.setInningsTotalRuns(
                innings.getTotalRuns());

        response.setInningsWickets(
                innings.getWickets());

        response.setInningsLegalBalls(
                innings.getLegalBalls());

        response.setCreatedAt(
                delivery.getCreatedAt());

        return response;
    }

    private void updateInningsStateAfterDelivery(
                InningsState state,
                Delivery delivery,
                Player newBatter) {

        // If a wicket falls, replace the dismissed batter
        // at the correct end.
        if (Boolean.TRUE.equals(delivery.getWicket())
                && newBatter != null) {

        Player dismissedPlayer =
                delivery.getDismissedPlayer();

        DismissalEnd dismissalEnd =
                delivery.getDismissalEnd();

        if (dismissalEnd == DismissalEnd.STRIKER) {

                state.setStriker(newBatter);

        } else if (dismissalEnd == DismissalEnd.NON_STRIKER) {

                state.setNonStriker(newBatter);
        }
        }

        // Odd number of runs means the batters change ends.
        int runs = delivery.getRunsOffBat();

        if (runs % 2 != 0) {

                Player striker = state.getStriker();

                state.setStriker(
                        state.getNonStriker());

                state.setNonStriker(striker);
        }

        // Only legal deliveries count toward the six-ball over.
        if (delivery.getLegalDelivery()) {

                int legalBalls =
                        state.getLegalBallsInOver() + 1;

                if (legalBalls == 6) {

                state.setLegalBallsInOver(0);

                state.setCurrentOver(
                        state.getCurrentOver() + 1);

                // At the end of the over,
                // the batters change ends.
                Player striker =
                        state.getStriker();

                state.setStriker(
                        state.getNonStriker());

                state.setNonStriker(striker);

                } else {

                state.setLegalBallsInOver(
                        legalBalls);
                }
        }

        inningsStateRepository.save(state);
    }

    private void createFieldingEvent(Delivery delivery) {

                if (!Boolean.TRUE.equals(delivery.getWicket())) {
                        return;
                }

                WicketType wicketType = delivery.getWicketType();

                if (wicketType != WicketType.CAUGHT
                        && wicketType != WicketType.STUMPED
                        && wicketType != WicketType.RUN_OUT) {
                        return;
                }

                if (delivery.getFielder() == null
                        || delivery.getDismissedPlayer() == null) {
                        return;
                }

                FieldingEvent fieldingEvent = new FieldingEvent();

                fieldingEvent.setInnings(delivery.getInnings());
                fieldingEvent.setDelivery(delivery);
                fieldingEvent.setFielder(delivery.getFielder());
                fieldingEvent.setDismissedPlayer(
                        delivery.getDismissedPlayer());
                fieldingEvent.setWicketType(wicketType);

                fieldingEventRepository.save(fieldingEvent);
   }

    private void updatePartnership(
                Innings innings,
                Delivery delivery) {

                Partnership partnership =
                        partnershipService.getActivePartnership(innings);

                if (partnership == null) {
                        return;
                }

                partnership.setRuns(
                        partnership.getRuns()
                                + delivery.getTotalRuns());

                if (delivery.getLegalDelivery()) {
                        partnership.setBalls(
                                partnership.getBalls() + 1);
                }

                if (Boolean.TRUE.equals(delivery.getWicket())) {
                        partnership.setActive(false);
                }

                partnershipService.save(partnership);
    }

    private void initializePartnershipIfNeeded(
                Innings innings) {

        Partnership activePartnership =
                partnershipService.getActivePartnership(innings);

        if (activePartnership != null) {
                return;
        }

        InningsState state =
                inningsStateRepository
                        .findByInnings(innings)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Innings state not found"));

        int partnershipNumber =
                partnershipService
                        .getNextPartnershipNumber(innings);

        partnershipService.createPartnership(
                innings,
                state.getStriker(),
                state.getNonStriker(),
                partnershipNumber);
    }

    private void createFallOfWicket(
                Innings innings,
                Delivery delivery) {

                if (!Boolean.TRUE.equals(delivery.getWicket())) {
                        return;
                }

                if (delivery.getDismissedPlayer() == null) {
                        return;
                }

                Integer wicketNumber = innings.getWickets();

                fallOfWicketService.createFallOfWicket(
                        innings,
                        delivery,
                        delivery.getDismissedPlayer(),
                        wicketNumber);
    }

}

