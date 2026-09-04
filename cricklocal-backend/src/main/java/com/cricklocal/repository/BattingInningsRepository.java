package com.cricklocal.repository;

import com.cricklocal.entity.BattingInnings;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BattingInningsRepository
        extends JpaRepository<BattingInnings, Long> {

    Optional<BattingInnings> findByInningsAndPlayer(
            Innings innings,
            Player player
    );

    boolean existsByInningsAndPlayer(
            Innings innings,
            Player player
    );

    List<BattingInnings> findByInningsOrderByBattingPositionAsc(
            Innings innings
    );

    void deleteByInnings(Innings innings);
}