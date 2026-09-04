package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatchResultRepository extends JpaRepository<MatchResult, Long> {

    Optional<MatchResult> findByMatch(Match match);

    boolean existsByMatch(Match match);
}