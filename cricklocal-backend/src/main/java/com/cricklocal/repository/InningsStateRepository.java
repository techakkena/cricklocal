package com.cricklocal.repository;

import com.cricklocal.entity.Innings;
import com.cricklocal.entity.InningsState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InningsStateRepository
        extends JpaRepository<InningsState, Long> {

    Optional<InningsState> findByInnings(Innings innings);

    boolean existsByInnings(Innings innings);
}