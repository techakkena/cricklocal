package com.cricklocal.service;

import com.cricklocal.dto.LeaderboardEntryResponse;
import com.cricklocal.dto.LeaderboardResponse;
import com.cricklocal.enums.MatchStatus;
import com.cricklocal.repository.BattingInningsRepository;
import com.cricklocal.repository.BowlingInningsRepository;
import com.cricklocal.repository.FieldingEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    private final BattingInningsRepository battingInningsRepository;
    private final BowlingInningsRepository bowlingInningsRepository;
    private final FieldingEventRepository fieldingEventRepository;

    public LeaderboardService(
            BattingInningsRepository battingInningsRepository,
            BowlingInningsRepository bowlingInningsRepository,
            FieldingEventRepository fieldingEventRepository) {

        this.battingInningsRepository = battingInningsRepository;
        this.bowlingInningsRepository = bowlingInningsRepository;
        this.fieldingEventRepository = fieldingEventRepository;
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse getTopRunScorers() {

        List<Object[]> rows =
                battingInningsRepository.findTopRunScorers(
                        MatchStatus.COMPLETED);

        List<LeaderboardEntryResponse> entries =
                new ArrayList<>();

        long rank = 1;

        for (Object[] row : rows) {

            LeaderboardEntryResponse entry =
                    new LeaderboardEntryResponse();

            entry.setRank(rank++);
            entry.setPlayerId(((Number) row[0]).longValue());
            entry.setPlayerName((String) row[1]);
            entry.setPrimaryValue(((Number) row[2]).longValue());
            entry.setSecondaryValue(((Number) row[4]).longValue());

            entries.add(entry);
        }

        LeaderboardResponse response =
                new LeaderboardResponse();

        response.setCategory("TOP_RUN_SCORERS");
        response.setEntries(entries);

        return response;
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse getTopWicketTakers() {

        List<Object[]> rows =
                bowlingInningsRepository.findTopWicketTakers(
                        MatchStatus.COMPLETED);

        List<LeaderboardEntryResponse> entries =
                new ArrayList<>();

        long rank = 1;

        for (Object[] row : rows) {

            LeaderboardEntryResponse entry =
                    new LeaderboardEntryResponse();

            entry.setRank(rank++);
            entry.setPlayerId(((Number) row[0]).longValue());
            entry.setPlayerName((String) row[1]);
            entry.setPrimaryValue(((Number) row[2]).longValue());
            entry.setSecondaryValue(((Number) row[5]).longValue());

            entries.add(entry);
        }

        LeaderboardResponse response =
                new LeaderboardResponse();

        response.setCategory("TOP_WICKET_TAKERS");
        response.setEntries(entries);

        return response;
    }

    @Transactional(readOnly = true)
    public LeaderboardResponse getTopFieldingPlayers() {

        List<Object[]> rows =
                fieldingEventRepository.findTopFieldingPlayers(
                        MatchStatus.COMPLETED);

        List<LeaderboardEntryResponse> entries =
                new ArrayList<>();

        long rank = 1;

        for (Object[] row : rows) {

            LeaderboardEntryResponse entry =
                    new LeaderboardEntryResponse();

            entry.setRank(rank++);
            entry.setPlayerId(((Number) row[0]).longValue());
            entry.setPlayerName((String) row[1]);
            entry.setPrimaryValue(((Number) row[2]).longValue());
            entry.setSecondaryValue(((Number) row[3]).longValue());

            entries.add(entry);
        }

        LeaderboardResponse response =
                new LeaderboardResponse();

        response.setCategory("TOP_FIELDING_PLAYERS");
        response.setEntries(entries);

        return response;
    }
}