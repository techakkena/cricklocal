package com.cricklocal.repository;

import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.Team;
import com.cricklocal.enums.InningsStatus;
import org.springframework.data.jpa.repository.JpaRepository;

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