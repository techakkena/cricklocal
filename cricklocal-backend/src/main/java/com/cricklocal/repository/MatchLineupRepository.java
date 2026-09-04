package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchLineup;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchLineupRepository
        extends JpaRepository<MatchLineup, Long> {

    Optional<MatchLineup> findByMatchAndPlayer(
            Match match,
            Player player
    );

    boolean existsByMatchAndPlayer(
            Match match,
            Player player
    );

    boolean existsByMatchAndTeamAndJerseyNumber(
            Match match,
            Team team,
            Integer jerseyNumber
    );

    boolean existsByMatchAndTeamAndCaptainTrue(
            Match match,
            Team team
    );

    boolean existsByMatchAndTeamAndWicketKeeperTrue(
            Match match,
            Team team
    );

    List<MatchLineup> findByMatch(Match match);

    List<MatchLineup> findByMatchAndTeam(
            Match match,
            Team team
    );
}