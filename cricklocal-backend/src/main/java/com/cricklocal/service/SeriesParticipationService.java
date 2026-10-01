package com.cricklocal.service;

import com.cricklocal.entity.Player;
import com.cricklocal.entity.Series;
import com.cricklocal.entity.SeriesParticipation;
import com.cricklocal.entity.SeriesParticipation;
import com.cricklocal.dto.SeriesParticipationResponse;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.PlayerRepository;
import com.cricklocal.repository.SeriesParticipationRepository;
import com.cricklocal.repository.SeriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SeriesParticipationService {

    private final SeriesParticipationRepository seriesParticipationRepository;
    private final SeriesRepository seriesRepository;
    private final PlayerRepository playerRepository;

    public SeriesParticipationService(
            SeriesParticipationRepository seriesParticipationRepository,
            SeriesRepository seriesRepository,
            PlayerRepository playerRepository) {

        this.seriesParticipationRepository = seriesParticipationRepository;
        this.seriesRepository = seriesRepository;
        this.playerRepository = playerRepository;
    }

    @Transactional
    public void addPlayerToSeries(Long seriesId, Long playerId) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found"));

        Optional<SeriesParticipation> existingParticipation =
                seriesParticipationRepository.findBySeriesAndPlayer(series, player);

        if (existingParticipation.isPresent()) {
                SeriesParticipation participation = existingParticipation.get();

                if (Boolean.TRUE.equals(participation.getActive())) {
                throw new IllegalArgumentException(
                        "Player is already part of this series"
                );
                }

                participation.setActive(true);
                seriesParticipationRepository.save(participation);
                return;
        }

        SeriesParticipation participation = new SeriesParticipation();
        participation.setSeries(series);
        participation.setPlayer(player);
        participation.setActive(true);

        seriesParticipationRepository.save(participation);
    }

    @Transactional(readOnly = true)
    public List<SeriesParticipationResponse> getSeriesParticipants(
            Long seriesId) {

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Series not found"));

        return seriesParticipationRepository
                .findBySeriesAndActiveTrue(series)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private SeriesParticipationResponse toResponse(
            SeriesParticipation participation) {

        SeriesParticipationResponse response =
                new SeriesParticipationResponse();

        response.setId(participation.getId());
        response.setSeriesId(participation.getSeries().getId());
        response.setPlayerId(participation.getPlayer().getId());
        response.setPlayerName(participation.getPlayer().getDisplayName());
        response.setActive(participation.getActive());
        response.setCreatedAt(participation.getCreatedAt());

        return response;
    }

    @Transactional
    public void removePlayerFromSeries(
            Long seriesId,
            Long playerId) {

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Series not found"));

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Player not found"));

        SeriesParticipation participation =
                seriesParticipationRepository
                        .findBySeriesAndPlayer(series, player)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Player is not part of this series"));

        participation.setActive(false);
        seriesParticipationRepository.save(participation);
    }
}