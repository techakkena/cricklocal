package com.cricklocal.repository;

import com.cricklocal.entity.FallOfWicket;
import com.cricklocal.entity.Innings;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cricklocal.entity.Innings;

import java.util.List;
import java.util.Optional;

public interface FallOfWicketRepository
        extends JpaRepository<FallOfWicket, Long> {

    Optional<FallOfWicket> findByInningsAndWicketNumber(
            Innings innings,
            Integer wicketNumber);

    List<FallOfWicket> findByInningsOrderByWicketNumberAsc(
            Innings innings);

    void deleteByInnings(Innings innings);

    boolean existsByInningsAndWicketNumber(
            Innings innings,
            Integer wicketNumber);
}