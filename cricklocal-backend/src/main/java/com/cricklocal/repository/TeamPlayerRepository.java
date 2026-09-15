package com.cricklocal.repository;

import com.cricklocal.entity.Player;
import com.cricklocal.entity.Team;
import com.cricklocal.entity.TeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, Long> {

    Optional<TeamPlayer> findByTeamAndPlayer(
            Team team,
            Player player
    );

    boolean existsByTeamAndJerseyNumberAndActiveTrue(
            Team team,
            Integer jerseyNumber
    );

    List<TeamPlayer> findByTeamAndActiveTrue(Team team);

    List<TeamPlayer> findByPlayerAndActiveTrue(Player player);

    @Query("""
        select count(tp) > 0
        from TeamPlayer tp
        where tp.player = :player
          and tp.active = true
          and tp.team <> :team
          and exists (
              select stExisting.id
              from SeriesTeam stExisting, SeriesTeam stTarget
              where stExisting.team = tp.team
                and stTarget.team = :team
                and stExisting.series = stTarget.series
          )
        """)
    boolean existsActiveMembershipInAnotherTeamOfSameSeries(
            Player player,
            Team team
    );
}