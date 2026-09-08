package com.cricklocal.repository;

import com.cricklocal.entity.BowlingInnings;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.cricklocal.enums.MatchStatus;

import java.util.List;
import java.util.Optional;

public interface BowlingInningsRepository
        extends JpaRepository<BowlingInnings, Long> {

    Optional<BowlingInnings> findByInningsAndPlayer(
            Innings innings,
            Player player
    );

    boolean existsByInningsAndPlayer(
            Innings innings,
            Player player
    );

    List<BowlingInnings> findByInningsOrderByIdAsc(
            Innings innings
    );

        @Query("""
            select bi
            from BowlingInnings bi
            join fetch bi.player
            where bi.innings = :innings
            order by bi.id asc
            """)
    List<BowlingInnings> findScorecardBowlingByInnings(
            Innings innings
    );

    void deleteByInnings(Innings innings);

    @Query("""
                select bi
                from BowlingInnings bi
                join fetch bi.innings i
                join fetch bi.player p
                join i.match m
                where p = :player
                and m.status = :status
                order by i.id asc
                """)
        List<BowlingInnings> findCareerBowlingByPlayer(
                Player player,
                MatchStatus status
    );
}