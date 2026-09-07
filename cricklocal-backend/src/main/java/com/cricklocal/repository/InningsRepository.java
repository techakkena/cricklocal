package com.cricklocal.repository;

import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.Team;
import com.cricklocal.enums.InningsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InningsRepository
        extends JpaRepository<Innings, Long> {

    Optional<Innings> findByMatchAndInningsNumber(
            Match match,
            Integer inningsNumber
    );

    boolean existsByMatchAndInningsNumber(
            Match match,
            Integer inningsNumber
    );

    List<Innings> findByMatchOrderByInningsNumberAsc(
            Match match
    );

        @Query("""
            select i
            from Innings i
            join fetch i.battingTeam
            join fetch i.bowlingTeam
            where i.match = :match
            order by i.inningsNumber asc
            """)
    List<Innings> findScorecardInningsByMatch(
            Match match
    );

    List<Innings> findByMatchAndStatus(
            Match match,
            InningsStatus status
    );

    boolean existsByMatchAndStatus(
            Match match,
            InningsStatus status
    );

    boolean existsByMatchAndBattingTeam(
            Match match,
            Team battingTeam
    );
}