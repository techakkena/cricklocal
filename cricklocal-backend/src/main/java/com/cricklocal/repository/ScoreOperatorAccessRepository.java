package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.ScoreOperatorAccess;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ScoreOperatorAccessRepository
        extends JpaRepository<ScoreOperatorAccess, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<ScoreOperatorAccess> findByAccessTokenAndActiveTrue(
            String accessToken
    );

    Optional<ScoreOperatorAccess> findFirstByMatchAndActiveTrue(
            Match match
    );
}