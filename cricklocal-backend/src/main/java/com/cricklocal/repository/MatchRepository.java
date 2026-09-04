package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.Series;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match, Long> {

    boolean existsBySeriesAndMatchNumber(
            Series series,
            Integer matchNumber
    );
}