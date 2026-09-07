package com.cricklocal.service;

import com.cricklocal.entity.BowlingInnings;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import com.cricklocal.repository.BowlingInningsRepository;
import org.springframework.stereotype.Service;
import com.cricklocal.dto.BowlingInningsResponse;

import java.util.List;

@Service
public class BowlingInningsService {

    private final BowlingInningsRepository bowlingInningsRepository;

    public BowlingInningsService(
            BowlingInningsRepository bowlingInningsRepository) {
        this.bowlingInningsRepository = bowlingInningsRepository;
    }

    public BowlingInnings getOrCreate(
            Innings innings,
            Player player) {

        return bowlingInningsRepository
                .findByInningsAndPlayer(innings, player)
                .orElseGet(() -> {

                    BowlingInnings bowlingInnings =
                            new BowlingInnings();

                    bowlingInnings.setInnings(innings);
                    bowlingInnings.setPlayer(player);

                    return bowlingInningsRepository.save(
                            bowlingInnings);
                });
    }

    public List<BowlingInnings> getByInnings(Innings innings) {

        return bowlingInningsRepository
                .findByInningsOrderByIdAsc(innings);
    }

    public List<BowlingInnings> getScorecardByInnings(
                Innings innings) {

        return bowlingInningsRepository
                .findScorecardBowlingByInnings(innings);
    }

    public List<BowlingInningsResponse> getResponsesByInnings(Innings innings) {

        return getScorecardByInnings(innings)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BowlingInnings save(
            BowlingInnings bowlingInnings) {

        return bowlingInningsRepository.save(
                bowlingInnings);
    }

    public BowlingInningsResponse toResponse(
        BowlingInnings bowlingInnings) {

    BowlingInningsResponse response =
                new BowlingInningsResponse();

                response.setId(bowlingInnings.getId());

                response.setInningsId(
                        bowlingInnings.getInnings().getId());

                Player player = bowlingInnings.getPlayer();

                response.setPlayerId(player.getId());
                response.setPlayerName(player.getDisplayName());

                int balls = bowlingInnings.getBallsBowled() == null
                        ? 0
                        : bowlingInnings.getBallsBowled();

                int completedOvers = balls / 6;
                int remainingBalls = balls % 6;

                response.setOvers(completedOvers + "." + remainingBalls);

                response.setBallsBowled(
                        bowlingInnings.getBallsBowled());
                response.setRunsConceded(
                        bowlingInnings.getRunsConceded());
                response.setWickets(
                        bowlingInnings.getWickets());
                response.setMaidens(
                        bowlingInnings.getMaidens());
                response.setFoursConceded(
                        bowlingInnings.getFoursConceded());
                response.setSixesConceded(
                        bowlingInnings.getSixesConceded());
                response.setWides(
                        bowlingInnings.getWides());
                response.setNoBalls(
                        bowlingInnings.getNoBalls());

        return response;
    }
}