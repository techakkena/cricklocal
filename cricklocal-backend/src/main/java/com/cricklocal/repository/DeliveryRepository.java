package com.cricklocal.repository;

import com.cricklocal.entity.Delivery;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository
        extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByInningsAndDeliveryNumber(
            Innings innings,
            Integer deliveryNumber
    );

    List<Delivery> findByInningsOrderByDeliveryNumberAsc(
            Innings innings
    );

    long countByInnings(Innings innings);

    Optional<Delivery> findTopByInningsOrderByDeliveryNumberDesc(
            Innings innings
    );

    List<Delivery> findByInningsAndBatter(
            Innings innings,
            Player batter
    );

    List<Delivery> findByInningsAndBowler(
            Innings innings,
            Player bowler
    );

    boolean existsByInningsAndDeliveryNumber(
            Innings innings,
            Integer deliveryNumber
    );
}