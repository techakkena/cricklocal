package com.cricklocal.repository;

import com.cricklocal.entity.FieldingEvent;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import com.cricklocal.enums.WicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FieldingEventRepository
        extends JpaRepository<FieldingEvent, Long> {

    List<FieldingEvent> findByInningsOrderByIdAsc(
            Innings innings);

    List<FieldingEvent> findByFielderOrderByIdAsc(
            Player fielder);

    long countByFielderAndWicketType(
            Player fielder,
            WicketType wicketType);
}