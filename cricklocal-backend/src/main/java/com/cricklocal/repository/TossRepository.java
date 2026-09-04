package com.cricklocal.repository;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.Toss;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TossRepository extends JpaRepository<Toss, Long> {

    Optional<Toss> findByMatch(Match match);

    boolean existsByMatch(Match match);
}