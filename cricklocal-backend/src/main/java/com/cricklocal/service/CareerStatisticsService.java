package com.cricklocal.service;

import com.cricklocal.dto.CareerBattingStatsResponse;
import com.cricklocal.entity.BattingInnings;
import com.cricklocal.entity.Player;
import com.cricklocal.enums.MatchStatus;
import com.cricklocal.repository.BattingInningsRepository;
import com.cricklocal.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cricklocal.dto.CareerBowlingStatsResponse;
import com.cricklocal.entity.BowlingInnings;
import com.cricklocal.repository.BowlingInningsRepository;
import com.cricklocal.dto.CareerFieldingStatsResponse;
import com.cricklocal.entity.FieldingEvent;
import com.cricklocal.enums.WicketType;
import com.cricklocal.repository.FieldingEventRepository;
import com.cricklocal.dto.CareerStatsResponse;
import com.cricklocal.exception.ResourceNotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CareerStatisticsService {

    private final PlayerRepository playerRepository;
    private final BattingInningsRepository battingInningsRepository;
    private final BowlingInningsRepository bowlingInningsRepository;
    private final FieldingEventRepository fieldingEventRepository;

    public CareerStatisticsService(
            PlayerRepository playerRepository,
            BattingInningsRepository battingInningsRepository,
            BowlingInningsRepository bowlingInningsRepository,
            FieldingEventRepository fieldingEventRepository) {

        this.playerRepository = playerRepository;
        this.battingInningsRepository = battingInningsRepository;
        this.bowlingInningsRepository = bowlingInningsRepository;
        this.fieldingEventRepository = fieldingEventRepository;
    }

    @Transactional(readOnly = true)
    public CareerBattingStatsResponse getCareerBattingStats(Long playerId) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Player not found: " + playerId));

        List<BattingInnings> innings =
                battingInningsRepository.findCareerBattingByPlayer(
                        player,
                        MatchStatus.COMPLETED);

        CareerBattingStatsResponse response =
                new CareerBattingStatsResponse();

        response.setPlayerId(player.getId());
        response.setPlayerName(player.getDisplayName());

        long matches = innings.stream()
                .map(battingInnings ->
                        battingInnings.getInnings().getMatch().getId())
                .distinct()
                .count();

        long totalInnings = innings.size();

        long runs = innings.stream()
                .mapToLong(battingInnings ->
                        battingInnings.getRuns())
                .sum();

        long ballsFaced = innings.stream()
                .mapToLong(battingInnings ->
                        battingInnings.getBallsFaced())
                .sum();

        long fours = innings.stream()
                .mapToLong(battingInnings ->
                        battingInnings.getFours())
                .sum();

        long sixes = innings.stream()
                .mapToLong(battingInnings ->
                        battingInnings.getSixes())
                .sum();

        long dots = innings.stream()
                .mapToLong(battingInnings ->
                        battingInnings.getDots())
                .sum();

        long extrasFaced = innings.stream()
                .mapToLong(battingInnings ->
                        battingInnings.getExtrasFaced())
                .sum();

        long dismissals = innings.stream()
                .filter(battingInnings ->
                        Boolean.TRUE.equals(
                                battingInnings.getDismissed()))
                .count();

        long highestScore = innings.stream()
                .mapToLong(battingInnings ->
                        battingInnings.getRuns())
                .max()
                .orElse(0);

        BigDecimal average = dismissals == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(runs)
                        .divide(
                                BigDecimal.valueOf(dismissals),
                                2,
                                RoundingMode.HALF_UP);

        BigDecimal strikeRate = ballsFaced == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(runs)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(
                                BigDecimal.valueOf(ballsFaced),
                                2,
                                RoundingMode.HALF_UP);

        response.setMatches(matches);
        response.setInnings(totalInnings);
        response.setRuns(runs);
        response.setBallsFaced(ballsFaced);
        response.setFours(fours);
        response.setSixes(sixes);
        response.setDots(dots);
        response.setExtrasFaced(extrasFaced);
        response.setDismissals(dismissals);
        response.setHighestScore(highestScore);
        response.setAverage(average);
        response.setStrikeRate(strikeRate);

        return response;
    }

    @Transactional(readOnly = true)
    public CareerBowlingStatsResponse getCareerBowlingStats(Long playerId) {

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Player not found: " + playerId));

            List<BowlingInnings> innings =
                        bowlingInningsRepository.findCareerBowlingByPlayer(
                                player,
                                MatchStatus.COMPLETED);

            CareerBowlingStatsResponse response =
                    new CareerBowlingStatsResponse();

            response.setPlayerId(player.getId());
            response.setPlayerName(player.getDisplayName());

            long matches = innings.stream()
                    .map(bowlingInnings ->
                            bowlingInnings.getInnings().getMatch().getId())
                    .distinct()
                    .count();

            long totalInnings = innings.size();

            long ballsBowled = innings.stream()
                    .mapToLong(bowlingInnings ->
                            bowlingInnings.getBallsBowled())
                    .sum();

            long runsConceded = innings.stream()
                    .mapToLong(bowlingInnings ->
                            bowlingInnings.getRunsConceded())
                    .sum();

            long wickets = innings.stream()
                    .mapToLong(bowlingInnings ->
                            bowlingInnings.getWickets())
                    .sum();

            long maidens = innings.stream()
                    .mapToLong(bowlingInnings ->
                            bowlingInnings.getMaidens())
                    .sum();

            long foursConceded = innings.stream()
                    .mapToLong(bowlingInnings ->
                            bowlingInnings.getFoursConceded())
                    .sum();

            long sixesConceded = innings.stream()
                    .mapToLong(bowlingInnings ->
                            bowlingInnings.getSixesConceded())
                    .sum();

            long wides = innings.stream()
                    .mapToLong(bowlingInnings ->
                            bowlingInnings.getWides())
                    .sum();

            long noBalls = innings.stream()
                    .mapToLong(bowlingInnings ->
                            bowlingInnings.getNoBalls())
                    .sum();

            BigDecimal economy = ballsBowled == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(runsConceded)
                            .multiply(BigDecimal.valueOf(6))
                            .divide(
                                    BigDecimal.valueOf(ballsBowled),
                                    2,
                                    RoundingMode.HALF_UP);

            BigDecimal average = wickets == 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(runsConceded)
                            .divide(
                                    BigDecimal.valueOf(wickets),
                                    2,
                                    RoundingMode.HALF_UP);

            response.setMatches(matches);
            response.setInnings(totalInnings);
            response.setBallsBowled(ballsBowled);
            response.setRunsConceded(runsConceded);
            response.setWickets(wickets);
            response.setMaidens(maidens);
            response.setFoursConceded(foursConceded);
            response.setSixesConceded(sixesConceded);
            response.setWides(wides);
            response.setNoBalls(noBalls);
            response.setEconomy(economy);
            response.setAverage(average);

            return response;
    }

    @Transactional(readOnly = true)
    public CareerFieldingStatsResponse getCareerFieldingStats(Long playerId) {

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Player not found: " + playerId));

        List<FieldingEvent> events =
                    fieldingEventRepository.findCareerFieldingByPlayer(
                            player,
                            MatchStatus.COMPLETED);

            CareerFieldingStatsResponse response =
                    new CareerFieldingStatsResponse();

            response.setPlayerId(player.getId());
            response.setPlayerName(player.getDisplayName());

            long matches = events.stream()
                    .map(event ->
                            event.getInnings().getMatch().getId())
                    .distinct()
                    .count();

            long fieldingDismissals = events.size();

            long catches = events.stream()
                    .filter(event ->
                            event.getWicketType() == WicketType.CAUGHT)
                    .count();

            long runOuts = events.stream()
                    .filter(event ->
                            event.getWicketType() == WicketType.RUN_OUT)
                    .count();

            long stumpings = events.stream()
                    .filter(event ->
                            event.getWicketType() == WicketType.STUMPED)
                    .count();

            response.setMatches(matches);
            response.setFieldingDismissals(fieldingDismissals);
            response.setCatches(catches);
            response.setRunOuts(runOuts);
            response.setStumpings(stumpings);

            return response;
    }

    @Transactional(readOnly = true)
    public CareerStatsResponse getCareerStats(Long playerId) {

            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Player not found: " + playerId));

            CareerStatsResponse response = new CareerStatsResponse();

            response.setPlayerId(player.getId());
            response.setPlayerName(player.getDisplayName());

            response.setBatting(
                    getCareerBattingStats(playerId));

            response.setBowling(
                    getCareerBowlingStats(playerId));

            response.setFielding(
                    getCareerFieldingStats(playerId));

            return response;
    }

}
