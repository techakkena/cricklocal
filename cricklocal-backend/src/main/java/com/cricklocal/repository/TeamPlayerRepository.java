package com.cricklocal.repository;

import com.cricklocal.entity.Team;
import com.cricklocal.entity.TeamPlayer;
import com.cricklocal.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, Long> {

    Optional<TeamPlayer> findByTeamAndPlayer(Team team, Player player);

    boolean existsByTeamAndJerseyNumberAndActiveTrue(
            Team team,
            Integer jerseyNumber
    );

    List<TeamPlayer> findByTeamAndActiveTrue(Team team);

    List<TeamPlayer> findByPlayerAndActiveTrue(Player player);
}