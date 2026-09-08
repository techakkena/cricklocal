package com.cricklocal.repository;

import com.cricklocal.entity.BattingInnings;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.cricklocal.enums.MatchStatus;

import java.util.List;
import java.util.Optional;

public interface BattingInningsRepository
        extends JpaRepository<BattingInnings, Long> {

    Optional<BattingInnings> findByInningsAndPlayer(
            Innings innings,
            Player player
    );

    boolean existsByInningsAndPlayer(
            Innings innings,
            Player player
    );

    List<BattingInnings> findByInningsOrderByBattingPositionAsc(
            Innings innings
    );

        @Query("""
            select bi
            from BattingInnings bi
            join fetch bi.player
            where bi.innings = :innings
            order by bi.battingPosition asc
            """)
    List<BattingInnings> findScorecardBattingByInnings(
            Innings innings
    );

    void deleteByInnings(Innings innings);

    @Query("""
                select bi
                from BattingInnings bi
                join fetch bi.innings i
                join fetch bi.player p
                join i.match m
                where p = :player
                and m.status = :status
                order by i.id asc
                """)
        List<BattingInnings> findCareerBattingByPlayer(
                Player player,
                MatchStatus status
    );

     @Query("""
                select bi.player.id,
                       bi.player.displayName,
                       sum(bi.runs),
                       sum(bi.ballsFaced),
                       count(distinct bi.innings.match.id)
                from BattingInnings bi
                where bi.innings.match.status = :status
                group by bi.player.id, bi.player.displayName
                order by sum(bi.runs) desc
                """)
    List<Object[]> findTopRunScorers(MatchStatus status);
}