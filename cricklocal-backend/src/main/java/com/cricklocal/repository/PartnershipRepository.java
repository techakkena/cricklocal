package com.cricklocal.repository;

import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Partnership;
import org.springframework.data.jpa.repository.JpaRepository;

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
}