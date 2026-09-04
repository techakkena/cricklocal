package com.cricklocal.service;

import com.cricklocal.dto.FallOfWicketResponse;
import com.cricklocal.entity.Delivery;
import com.cricklocal.entity.FallOfWicket;
import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Player;
import com.cricklocal.enums.WicketType;
import com.cricklocal.repository.FallOfWicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FallOfWicketService {

    private final FallOfWicketRepository fallOfWicketRepository;

    public FallOfWicketService(
            FallOfWicketRepository fallOfWicketRepository) {

        this.fallOfWicketRepository = fallOfWicketRepository;
    }

    public FallOfWicket createFallOfWicket(
            Innings innings,
            Delivery delivery,
            Player dismissedPlayer,
            Integer wicketNumber) {

        FallOfWicket fallOfWicket =
                new FallOfWicket();

        fallOfWicket.setInnings(innings);
        fallOfWicket.setWicketNumber(wicketNumber);
        fallOfWicket.setDismissedPlayer(dismissedPlayer);

        fallOfWicket.setScore(
                innings.getTotalRuns());

        fallOfWicket.setOverNumber(
                delivery.getOverNumber());

        fallOfWicket.setBallInOver(
                delivery.getBallInOver());

        fallOfWicket.setWicketType(
                delivery.getWicketType());

        fallOfWicket.setDelivery(delivery);

        return fallOfWicketRepository.save(fallOfWicket);
    }

    public List<FallOfWicket> getByInnings(
            Innings innings) {

        return fallOfWicketRepository
                .findByInningsOrderByWicketNumberAsc(
                        innings);
    }

    public FallOfWicketResponse toResponse(
            FallOfWicket fallOfWicket) {

        FallOfWicketResponse response =
                new FallOfWicketResponse();

        response.setId(fallOfWicket.getId());

        response.setInningsId(
                fallOfWicket.getInnings().getId());

        response.setWicketNumber(
                fallOfWicket.getWicketNumber());

        Player dismissedPlayer =
                fallOfWicket.getDismissedPlayer();

        response.setDismissedPlayerId(
                dismissedPlayer.getId());

        response.setDismissedPlayerName(
                dismissedPlayer.getDisplayName());

        response.setScore(
                fallOfWicket.getScore());

        response.setOverNumber(
                fallOfWicket.getOverNumber());

        response.setBallInOver(
                fallOfWicket.getBallInOver());

        response.setWicketType(
                fallOfWicket.getWicketType());

        if (fallOfWicket.getDelivery() != null) {
            response.setDeliveryId(
                    fallOfWicket.getDelivery().getId());
        }

        return response;
    }

    public List<FallOfWicketResponse> getResponsesByInnings(
            Innings innings) {

        return getByInnings(innings)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}