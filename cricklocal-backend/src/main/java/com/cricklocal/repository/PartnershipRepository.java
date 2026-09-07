package com.cricklocal.repository;

import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Partnership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PartnershipRepository
        extends JpaRepository<Partnership, Long> {

    Optional<Partnership> findByInningsAndPartnershipNumber(
            Innings innings,
            Integer partnershipNumber);

    List<Partnership> findByInningsOrderByPartnershipNumberAsc(
            Innings innings);

    Optional<Partnership> findByInningsAndActiveTrue(
            Innings innings);

    @Query("""
            select p
            from Partnership p
            join fetch p.batterOne
            join fetch p.batterTwo
            where p.innings = :innings
            order by p.partnershipNumber asc
            """)
    List<Partnership> findScorecardPartnershipsByInnings(Innings innings);
}
