package com.cricklocal.service;

import com.cricklocal.entity.BattingInnings;
import com.cricklocal.entity.BowlingInnings;
import com.cricklocal.entity.Delivery;
import com.cricklocal.entity.Innings;
import com.cricklocal.enums.ExtraType;
import com.cricklocal.enums.WicketType;
import com.cricklocal.repository.BattingInningsRepository;
import com.cricklocal.repository.BowlingInningsRepository;
import com.cricklocal.repository.DeliveryRepository;
import org.springframework.stereotype.Service;
import com.cricklocal.entity.FallOfWicket;
import com.cricklocal.repository.FallOfWicketRepository;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatisticsRebuildService {

    private final DeliveryRepository deliveryRepository;
    private final BattingInningsRepository battingInningsRepository;
    private final BowlingInningsRepository bowlingInningsRepository;
    private final FallOfWicketRepository fallOfWicketRepository;

    public StatisticsRebuildService(
            DeliveryRepository deliveryRepository,
            BattingInningsRepository battingInningsRepository,
            BowlingInningsRepository bowlingInningsRepository,
            FallOfWicketRepository fallOfWicketRepository) {

        this.deliveryRepository = deliveryRepository;
        this.battingInningsRepository = battingInningsRepository;
        this.bowlingInningsRepository = bowlingInningsRepository;
        this.fallOfWicketRepository = fallOfWicketRepository;
    }

    @Transactional
    public void rebuild(Innings innings) {

            battingInningsRepository.deleteByInnings(innings);
            bowlingInningsRepository.deleteByInnings(innings);
            fallOfWicketRepository.deleteByInnings(innings);

            int runningScore = 0;
            int runningWickets = 0;

            for (Delivery delivery :
                    deliveryRepository.findByInningsOrderByDeliveryNumberAsc(innings)) {

                runningScore += delivery.getTotalRuns();

                rebuildBatting(innings, delivery);
                rebuildBowling(innings, delivery);

                if (Boolean.TRUE.equals(delivery.getWicket())
                        && delivery.getDismissedPlayer() != null) {

                    runningWickets++;

                    FallOfWicket fallOfWicket =
                            new FallOfWicket();

                    fallOfWicket.setInnings(innings);
                    fallOfWicket.setWicketNumber(runningWickets);
                    fallOfWicket.setDismissedPlayer(
                            delivery.getDismissedPlayer());
                    fallOfWicket.setScore(runningScore);
                    fallOfWicket.setOverNumber(
                            delivery.getOverNumber());
                    fallOfWicket.setBallInOver(
                            delivery.getBallInOver());
                    fallOfWicket.setWicketType(
                            delivery.getWicketType());
                    fallOfWicket.setDelivery(delivery);

                    fallOfWicketRepository.save(fallOfWicket);
                }
            }
    }

    private void rebuildBatting(
                Innings innings,
                Delivery delivery) {

            BattingInnings battingInnings =
                    battingInningsRepository
                            .findByInningsAndPlayer(
                                    innings,
                                    delivery.getBatter())
                            .orElseGet(() -> {

                                BattingInnings newBatting =
                                        new BattingInnings();

                                newBatting.setInnings(innings);
                                newBatting.setPlayer(
                                        delivery.getBatter());

                                newBatting.setBattingPosition(
                                        battingInningsRepository
                                                .findByInningsOrderByBattingPositionAsc(
                                                        innings)
                                                .size() + 1);

                                return newBatting;
                            });

            int runs = delivery.getRunsOffBat();

            battingInnings.setRuns(
                    battingInnings.getRuns() + runs);

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

            battingInningsRepository.save(battingInnings);
    }

    private void rebuildBowling(
                Innings innings,
                Delivery delivery) {

            BowlingInnings bowlingInnings =
                    bowlingInningsRepository
                            .findByInningsAndPlayer(
                                    innings,
                                    delivery.getBowler())
                            .orElseGet(() -> {

                                BowlingInnings newBowling =
                                        new BowlingInnings();

                                newBowling.setInnings(innings);
                                newBowling.setPlayer(
                                        delivery.getBowler());

                                return newBowling;
                            });

            int runsConceded = delivery.getRunsOffBat();

            // Byes and leg-byes are not charged to the bowler.
            if (delivery.getExtraType() != ExtraType.BYE
                    && delivery.getExtraType() != ExtraType.LEG_BYE) {

                runsConceded += delivery.getExtraRuns();
            }

            bowlingInnings.setRunsConceded(
                    bowlingInnings.getRunsConceded()
                            + runsConceded);

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
                        bowlingInnings.getWides()
                                + delivery.getExtraRuns());
            }

            if (delivery.getExtraType() == ExtraType.NO_BALL) {
                bowlingInnings.setNoBalls(
                        bowlingInnings.getNoBalls() + 1);
            }

            if (Boolean.TRUE.equals(delivery.getWicket())) {

                WicketType wicketType =
                        delivery.getWicketType();

                if (wicketType == WicketType.BOWLED
                        || wicketType == WicketType.CAUGHT
                        || wicketType == WicketType.LBW
                        || wicketType == WicketType.STUMPED
                        || wicketType == WicketType.HIT_WICKET) {

                    bowlingInnings.setWickets(
                            bowlingInnings.getWickets() + 1);
                }
            }

            bowlingInningsRepository.save(bowlingInnings);
    }


}