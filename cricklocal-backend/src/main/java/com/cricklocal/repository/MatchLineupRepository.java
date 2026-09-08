package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchLineup;
import com.cricklocal.entity.Player;
import com.cricklocal.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    @Query("""
        select ml
        from MatchLineup ml
        join fetch ml.match m
        join fetch ml.player p
        join fetch ml.team t
        where p = :player
          and ml.playing = true
        order by m.scheduledAt desc, ml.id desc
        """)
    List<MatchLineup> findPlayerMatchHistory(Player player);
}