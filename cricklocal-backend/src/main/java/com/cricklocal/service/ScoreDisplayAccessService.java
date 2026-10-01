package com.cricklocal.service;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.ScoreDisplayAccess;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.ScoreDisplayAccessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class ScoreDisplayAccessService {

    private static final int TOKEN_BYTES = 32;
    private static final long ACCESS_DURATION_DAYS = 7;

    private final ScoreDisplayAccessRepository accessRepository;
    private final MatchRepository matchRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public ScoreDisplayAccessService(
            ScoreDisplayAccessRepository accessRepository,
            MatchRepository matchRepository
    ) {
        this.accessRepository = accessRepository;
        this.matchRepository = matchRepository;
    }

    @Transactional
    public GeneratedDisplayAccess generateAccess(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Match not found"));

        accessRepository.findFirstByMatchAndActiveTrue(match)
                .ifPresent(existing -> {
                    existing.setActive(false);
                    existing.setRevokedAt(Instant.now());
                    accessRepository.save(existing);
                });

        String displayToken = generateToken();

        ScoreDisplayAccess access = new ScoreDisplayAccess();
        access.setMatch(match);
        access.setDisplayToken(displayToken);
        access.setActive(true);
        access.setExpiresAt(
                Instant.now().plus(
                        ACCESS_DURATION_DAYS,
                        ChronoUnit.DAYS
                )
        );

        ScoreDisplayAccess saved = accessRepository.save(access);

        return new GeneratedDisplayAccess(
                saved.getDisplayToken(),
                saved.getExpiresAt()
        );
    }

    @Transactional(readOnly = true)
    public ScoreDisplayAccess validateAccess(String displayToken) {

        if (displayToken == null || displayToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Display token is required"
            );
        }

        ScoreDisplayAccess access =
                accessRepository.findByDisplayTokenAndActiveTrue(
                        displayToken
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid or inactive display link"
                        ));

        if (access.getExpiresAt() != null
                && access.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException(
                    "Display link has expired"
            );
        }

        return access;
    }

    @Transactional
    public void revokeAccess(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Match not found"));

        accessRepository.findFirstByMatchAndActiveTrue(match)
                .ifPresent(existing -> {
                    existing.setActive(false);
                    existing.setRevokedAt(Instant.now());
                    accessRepository.save(existing);
                });
    }

    private String generateToken() {

        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        StringBuilder token = new StringBuilder(
                TOKEN_BYTES * 2
        );

        for (byte value : bytes) {
            token.append(String.format("%02x", value));
        }

        return token.toString();
    }

    public record GeneratedDisplayAccess(
            String displayToken,
            Instant expiresAt
    ) {
    }
}