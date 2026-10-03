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

@Service
public class ScoreDisplayAccessService {

    private static final int TOKEN_BYTES = 32;

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

        /*
         * PUBLIC SCORE DISPLAY LINK RULE:
         *
         * One match gets one public display link.
         *
         * If an active link already exists, return it.
         * Do NOT revoke it.
         * Do NOT generate a new token.
         */
        var existing = accessRepository
                .findFirstByMatchAndActiveTrue(match)
                .orElse(null);

        if (existing != null) {
            return new GeneratedDisplayAccess(
                    existing.getDisplayToken(),
                    existing.getExpiresAt()
            );
        }

        /*
         * No active public link exists.
         *
         * Create the public link for this match.
         */
        String displayToken = generateToken();

        ScoreDisplayAccess access = new ScoreDisplayAccess();
        access.setMatch(match);
        access.setDisplayToken(displayToken);
        access.setActive(true);

        /*
         * Public score display link does not expire automatically.
         *
         * It remains valid until the administrator explicitly
         * revokes the public display link.
         */
        access.setExpiresAt(null);

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

        /*
         * expiresAt is currently null for public score display links.
         *
         * Keep this check for future compatibility if an expiry
         * policy is introduced later.
         */
        if (access.getExpiresAt() != null
                && !access.getExpiresAt().isAfter(Instant.now())) {

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