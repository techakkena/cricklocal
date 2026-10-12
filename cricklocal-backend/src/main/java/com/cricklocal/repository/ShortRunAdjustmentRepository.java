
package com.cricklocal.repository;

import com.cricklocal.entity.Delivery;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.ShortRunAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShortRunAdjustmentRepository
        extends JpaRepository<ShortRunAdjustment, Long> {

    List<ShortRunAdjustment> findByInningsOrderByCreatedAtAscIdAsc(
            Innings innings);

    List<ShortRunAdjustment> findByDeliveryAndReversedFalse(
            Delivery delivery);

    @Query("""
        select coalesce(sum(a.runsDeducted), 0)
        from ShortRunAdjustment a
        where a.innings = :innings
          and a.reversed = false
        """)
    Integer sumRunsDeductedByInnings(
            @Param("innings") Innings innings);
}
