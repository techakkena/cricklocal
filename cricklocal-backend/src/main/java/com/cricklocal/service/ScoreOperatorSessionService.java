package com.cricklocal.service;

import com.cricklocal.entity.Innings;
import com.cricklocal.entity.Match;
import com.cricklocal.entity.ScoreOperatorSession;
import com.cricklocal.exception.ForbiddenException;
import com.cricklocal.exception.ResourceNotFoundException;
import com.cricklocal.repository.InningsRepository;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.ScoreOperatorSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class ScoreOperatorSessionService {

    private static final int SESSION_TOKEN_BYTES = 32;
    private static final long SESSION_DURATION_HOURS = 2;

    private final ScoreOperatorSessionRepository sessionRepository;
    private final MatchRepository matchRepository;
    private final InningsRepository inningsRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public ScoreOperatorSessionService(
            ScoreOperatorSessionRepository sessionRepository,
            MatchRepository matchRepository,
            InningsRepository inningsRepository) {

        this.sessionRepository = sessionRepository;
        this.matchRepository = matchRepository;
        this.inningsRepository = inningsRepository;
    }

    @Transactional
    public CreatedOperatorSession createSession(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Match not found"));

        revokeExistingSessions(matchId);

        String rawToken = generateToken();

        ScoreOperatorSession session =
                new ScoreOperatorSession();

        session.setMatch(match);
        session.setSessionTokenHash(
                hashToken(rawToken));
        session.setActive(true);
        session.setExpiresAt(
                Instant.now()
                        .plus(SESSION_DURATION_HOURS, ChronoUnit.HOURS));

        ScoreOperatorSession saved =
                sessionRepository.save(session);

        return new CreatedOperatorSession(
                rawToken,
                saved.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public ScoreOperatorSession validateSession(
            String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new ForbiddenException(
                    "Score operator session is required");
        }

        String tokenHash = hashToken(rawToken);

        ScoreOperatorSession session =
                sessionRepository
                        .findBySessionTokenHashAndActiveTrue(
                                tokenHash)
                        .orElseThrow(() ->
                                new ForbiddenException(
                                        "Invalid score operator session"));

        if (session.getExpiresAt() == null
                || !session.getExpiresAt()
                        .isAfter(Instant.now())) {

            throw new ForbiddenException(
                    "Score operator session has expired");
        }

        return session;
    }

    @Transactional(readOnly = true)
    public ScoreOperatorSession validateSessionForInnings(
            String rawToken,
            Long inningsId) {

        ScoreOperatorSession session =
                validateSession(rawToken);

        Innings innings =
                inningsRepository.findById(inningsId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Innings not found"));

        if (!session.getMatch().getId()
                .equals(innings.getMatch().getId())) {

            throw new ForbiddenException(
                    "Score operator session does not belong to this match");
        }

        return session;
    }

    @Transactional
    public void revokeSession(String rawToken) {

        ScoreOperatorSession session =
                validateSession(rawToken);

        session.setActive(false);
        session.setRevokedAt(Instant.now());

        sessionRepository.save(session);
    }

    @Transactional
    public void revokeExistingSessions(Long matchId) {

        sessionRepository.deleteByMatchId(matchId);
    }

    private String generateToken() {

        byte[] bytes =
                new byte[SESSION_TOKEN_BYTES];

        secureRandom.nextBytes(bytes);

        StringBuilder token =
                new StringBuilder(bytes.length * 2);

        for (byte value : bytes) {
            token.append(
                    String.format("%02x", value));
        }

        return token.toString();
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8));

            StringBuilder hex =
                    new StringBuilder(hash.length * 2);

            for (byte value : hash) {
                hex.append(
                        String.format("%02x", value));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    e);
        }
    }

    public record CreatedOperatorSession(
            String sessionToken,
            Instant expiresAt
    ) {}
}