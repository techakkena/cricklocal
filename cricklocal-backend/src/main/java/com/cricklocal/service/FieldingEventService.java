package com.cricklocal.service;

import com.cricklocal.dto.FieldingEventResponse;
import com.cricklocal.dto.FieldingStatsResponse;
import com.cricklocal.entity.FieldingEvent;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import com.cricklocal.enums.WicketType;
import com.cricklocal.repository.FieldingEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FieldingEventService {

    private final FieldingEventRepository fieldingEventRepository;

    public FieldingEventService(
            FieldingEventRepository fieldingEventRepository) {

        this.fieldingEventRepository = fieldingEventRepository;
    }

    public FieldingEvent save(FieldingEvent fieldingEvent) {
        return fieldingEventRepository.save(fieldingEvent);
    }

    public List<FieldingEvent> getByInnings(Innings innings) {
        return fieldingEventRepository
                .findByInningsOrderByIdAsc(innings);
    }

    public FieldingEventResponse toResponse(
            FieldingEvent fieldingEvent) {

        FieldingEventResponse response =
                new FieldingEventResponse();

        response.setId(fieldingEvent.getId());

        response.setInningsId(
                fieldingEvent.getInnings().getId());

        if (fieldingEvent.getDelivery() != null) {
            response.setDeliveryId(
                    fieldingEvent.getDelivery().getId());
        }

        response.setFielderId(
                fieldingEvent.getFielder().getId());

        response.setFielderName(
                fieldingEvent.getFielder().getDisplayName());

        response.setDismissedPlayerId(
                fieldingEvent.getDismissedPlayer().getId());

        response.setDismissedPlayerName(
                fieldingEvent.getDismissedPlayer()
                        .getDisplayName());

        response.setWicketType(
                fieldingEvent.getWicketType());

        response.setCreatedAt(
                fieldingEvent.getCreatedAt());

        return response;
    }

    public List<FieldingEventResponse> getResponsesByInnings(
            Innings innings) {

        return getByInnings(innings)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public FieldingStatsResponse getPlayerStats(Player player) {

                FieldingStatsResponse response =
                        new FieldingStatsResponse();

                response.setPlayerId(player.getId());
                response.setPlayerName(player.getDisplayName());

                response.setCatches(
                        fieldingEventRepository.countByFielderAndWicketType(
                                player,
                                WicketType.CAUGHT));

                response.setRunOuts(
                        fieldingEventRepository.countByFielderAndWicketType(
                                player,
                                WicketType.RUN_OUT));

                response.setStumpings(
                        fieldingEventRepository.countByFielderAndWicketType(
                                player,
                                WicketType.STUMPED));

        return response;
    }

}