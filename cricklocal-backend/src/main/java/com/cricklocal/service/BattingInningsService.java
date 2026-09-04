package com.cricklocal.service;

import com.cricklocal.dto.BattingInningsResponse;
import com.cricklocal.entity.BattingInnings;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import com.cricklocal.repository.BattingInningsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BattingInningsService {

    private final BattingInningsRepository battingInningsRepository;

    public BattingInningsService(
            BattingInningsRepository battingInningsRepository) {
        this.battingInningsRepository = battingInningsRepository;
    }

    public BattingInnings getOrCreate(
            Innings innings,
            Player player) {

        return battingInningsRepository
                .findByInningsAndPlayer(innings, player)
                .orElseGet(() -> {

                    BattingInnings battingInnings =
                            new BattingInnings();

                    battingInnings.setInnings(innings);
                    battingInnings.setPlayer(player);
                    battingInnings.setBattingPosition(
                            getNextBattingPosition(innings));

                    return battingInningsRepository.save(
                            battingInnings);
                });
    }

    public List<BattingInnings> getByInnings(Innings innings) {

        return battingInningsRepository
                .findByInningsOrderByBattingPositionAsc(innings);
    }

    public BattingInningsResponse toResponse(
            BattingInnings battingInnings) {

        BattingInningsResponse response =
                new BattingInningsResponse();

        response.setId(battingInnings.getId());
        response.setInningsId(
                battingInnings.getInnings().getId());

        Player player = battingInnings.getPlayer();

        response.setPlayerId(player.getId());
        response.setPlayerName(player.getDisplayName());

        response.setBattingPosition(
                battingInnings.getBattingPosition());
        response.setRuns(battingInnings.getRuns());
        response.setBallsFaced(
                battingInnings.getBallsFaced());
        response.setFours(battingInnings.getFours());
        response.setSixes(battingInnings.getSixes());
        response.setDots(battingInnings.getDots());
        response.setExtrasFaced(
                battingInnings.getExtrasFaced());

        response.setDismissed(
                battingInnings.getDismissed());
        response.setDismissalType(
                battingInnings.getDismissalType());

        if (battingInnings.getDismissedByPlayer() != null) {
            Player dismissedBy =
                    battingInnings.getDismissedByPlayer();

            response.setDismissedByPlayerId(
                    dismissedBy.getId());

            response.setDismissedByPlayerName(
                    dismissedBy.getDisplayName());
        }

        return response;
    }

    public List<BattingInningsResponse> getResponsesByInnings(
            Innings innings) {

        return getByInnings(innings)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public BattingInnings save(BattingInnings battingInnings) {
        return battingInningsRepository.save(battingInnings);
    }

    private int getNextBattingPosition(Innings innings) {

    List<BattingInnings> existing =
            battingInningsRepository
                    .findByInningsOrderByBattingPositionAsc(
                            innings);

        return existing.size() + 1;
    }
}