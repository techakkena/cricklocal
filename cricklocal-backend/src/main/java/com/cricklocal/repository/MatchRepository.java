package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.Series;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    boolean existsBySeriesAndMatchNumber(
            Series series,
            Integer matchNumber
    );

    @Query("""
        select m
        from Match m
        order by m.scheduledAt desc, m.id desc
        """)
    List<Match> findMatchHistory();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Match m where m.id = :matchId")
    Optional<Match> findByIdForUpdate(Long matchId);
}