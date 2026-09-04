package com.cricklocal.repository;

import com.cricklocal.entity.Series;
import com.cricklocal.entity.SeriesTeam;
import com.cricklocal.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesTeamRepository extends JpaRepository<SeriesTeam, Long> {

    Optional<SeriesTeam> findBySeriesAndTeam(
            Series series,
            Team team
    );

    boolean existsBySeriesAndTeam(
            Series series,
            Team team
    );

    List<SeriesTeam> findBySeries(Series series);
}