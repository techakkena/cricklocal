package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.ScoreDisplayAccess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScoreDisplayAccessRepository
        extends JpaRepository<ScoreDisplayAccess, Long> {

    Optional<ScoreDisplayAccess>
    findByDisplayTokenAndActiveTrue(String displayToken);

    Optional<ScoreDisplayAccess>
    findFirstByMatchAndActiveTrue(Match match);
}