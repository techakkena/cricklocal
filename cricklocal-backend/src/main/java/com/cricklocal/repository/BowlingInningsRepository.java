package com.cricklocal.repository;

import com.cricklocal.entity.BowlingInnings;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BowlingInningsRepository
        extends JpaRepository<BowlingInnings, Long> {

    Optional<BowlingInnings> findByInningsAndPlayer(
            Innings innings,
            Player player
    );

    boolean existsByInningsAndPlayer(
            Innings innings,
            Player player
    );

    List<BowlingInnings> findByInningsOrderByIdAsc(
            Innings innings
    );

    void deleteByInnings(Innings innings);
}