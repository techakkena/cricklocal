package com.cricklocal.repository;

import com.cricklocal.entity.Delivery;
import com.cricklocal.entity.FieldingEvent;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import com.cricklocal.enums.WicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import com.cricklocal.enums.MatchStatus;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FieldingEventRepository
        extends JpaRepository<FieldingEvent, Long> {

    List<FieldingEvent> findByInningsOrderByIdAsc(
            Innings innings);

    List<FieldingEvent> findByFielderOrderByIdAsc(
            Player fielder);

    void deleteByDelivery(Delivery delivery);

    @Query("""
                select fe
                from FieldingEvent fe
                join fetch fe.innings i
                join fetch fe.fielder f
                join i.match m
                where f = :player
                and m.status = :status
                order by fe.id asc
                """)
        List<FieldingEvent> findCareerFieldingByPlayer(
                Player player,
                MatchStatus status
    );

    long countByFielderAndWicketType(
            Player fielder,
            WicketType wicketType);

    @Query("""
                select fe.fielder.id,
                       fe.fielder.displayName,
                       count(fe),
                       sum(case when fe.wicketType = com.cricklocal.enums.WicketType.CAUGHT then 1 else 0 end),
                       sum(case when fe.wicketType = com.cricklocal.enums.WicketType.RUN_OUT then 1 else 0 end),
                       sum(case when fe.wicketType = com.cricklocal.enums.WicketType.STUMPED then 1 else 0 end)
                from FieldingEvent fe
                where fe.innings.match.status = :status
                group by fe.fielder.id, fe.fielder.displayName
                order by count(fe) desc
                """)
    List<Object[]> findTopFieldingPlayers(MatchStatus status);
}