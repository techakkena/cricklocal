package com.cricklocal.repository;

import com.cricklocal.entity.ScoreOperatorSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoreOperatorSessionRepository
        extends JpaRepository<ScoreOperatorSession, Long> {

    Optional<ScoreOperatorSession>
    findBySessionTokenHashAndActiveTrue(
            String sessionTokenHash
    );

    void deleteByMatchId(Long matchId);
}