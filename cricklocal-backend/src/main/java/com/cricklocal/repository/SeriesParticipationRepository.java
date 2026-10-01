package com.cricklocal.repository;

import com.cricklocal.entity.Player;
import com.cricklocal.entity.Series;
import com.cricklocal.entity.SeriesParticipation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeriesParticipationRepository
        extends JpaRepository<SeriesParticipation, Long> {

    List<SeriesParticipation> findBySeriesAndActiveTrue(Series series);

    List<SeriesParticipation> findByPlayerAndActiveTrue(Player player);

    Optional<SeriesParticipation> findBySeriesAndPlayer(
            Series series,
            Player player
    );
}