package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchTeam;
import com.cricklocal.entity.Team;
import com.cricklocal.enums.MatchTeamSide;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {

    Optional<MatchTeam> findByMatchAndTeam(
            Match match,
            Team team
    );

    boolean existsByMatchAndSide(
            Match match,
            MatchTeamSide side
    );

    List<MatchTeam> findByMatch(Match match);
}