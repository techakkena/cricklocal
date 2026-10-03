package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.ScoreDisplayAccess;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ScoreDisplayAccessRepository
        extends JpaRepository<ScoreDisplayAccess, Long> {

    Optional<ScoreDisplayAccess>
    findByDisplayTokenAndActiveTrue(String displayToken);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ScoreDisplayAccess>
    findFirstByMatchAndActiveTrue(Match match);
}
