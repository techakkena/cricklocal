package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.PlayingXI;
import com.cricklocal.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayingXIRepository extends JpaRepository<PlayingXI, Long> {

    Optional<PlayingXI> findByMatchAndTeam(
            Match match,
            Team team
    );

    boolean existsByMatchAndTeam(
            Match match,
            Team team
    );
}