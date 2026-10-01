package com.cricklocal.service;

import com.cricklocal.entity.Match;
import com.cricklocal.entity.ScoreOperatorAccess;
import com.cricklocal.repository.MatchRepository;
import com.cricklocal.repository.ScoreOperatorAccessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class ScoreOperatorAccessService {

    private final ScoreOperatorAccessRepository accessRepository;
    private final MatchRepository matchRepository;
    private final ScoreOperatorSessionService sessionService;

    private final SecureRandom secureRandom = new SecureRandom();

    public ScoreOperatorAccessService(
            ScoreOperatorAccessRepository accessRepository,
            MatchRepository matchRepository,
            ScoreOperatorSessionService sessionService
    ) {
        this.accessRepository = accessRepository;
        this.matchRepository = matchRepository;
        this.sessionService = sessionService;
    }

    @Transactional
    public GeneratedOperatorAccess generateAccess(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Match not found")
                );

        // Close any currently active operator session.
        sessionService.revokeExistingSessions(matchId);

        // Revoke any currently active access link.
        accessRepository.findFirstByMatchAndActiveTrue(match)
                .ifPresent(existing -> {
                existing.setActive(false);
                existing.setRevokedAt(Instant.now());
                accessRepository.save(existing);
                });

        String accessToken = generateAccessToken();
        String securityCode = generateSecurityCode();

        ScoreOperatorAccess access = new ScoreOperatorAccess();
        access.setMatch(match);
        access.setAccessToken(accessToken);
        access.setSecurityCodeHash(hashSecurityCode(securityCode));
        access.setActive(true);
        access.setExpiresAt(
                Instant.now().plus(7, ChronoUnit.DAYS)
        );

        accessRepository.save(access);

        return new GeneratedOperatorAccess(
                accessToken,
                securityCode,
                access.getExpiresAt()
        );
    }

    @Transactional
    public ScoreOperatorAccess validateAccess(
            String accessToken,
            String securityCode
    ) {

        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException(
                    "Access token is required"
            );
        }

        if (securityCode == null || securityCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Security code is required"
            );
        }

        ScoreOperatorAccess access =
                accessRepository
                        .findByAccessTokenAndActiveTrue(accessToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid or inactive operator link"
                                )
                        );

        if (access.getExpiresAt() != null
                && access.getExpiresAt().isBefore(Instant.now())) {

            throw new IllegalArgumentException(
                    "Operator link has expired"
            );
        }

        String suppliedHash = hashSecurityCode(securityCode);

        if (!MessageDigest.isEqual(
                suppliedHash.getBytes(StandardCharsets.UTF_8),
                access.getSecurityCodeHash()
                        .getBytes(StandardCharsets.UTF_8)
        )) {

        throw new IllegalArgumentException(
                "Invalid security code"
        );
        }

        access.setActive(false);
        access.setRevokedAt(Instant.now());

        accessRepository.save(access);

        return access;
    }

    @Transactional
    public void revokeAccess(Long matchId) {

        Match match = matchRepository.findById(matchId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Match not found")
                );

        accessRepository.findFirstByMatchAndActiveTrue(match)
                .ifPresent(access -> {
                    access.setActive(false);
                    access.setRevokedAt(Instant.now());
                    accessRepository.save(access);
                });
    }

    private String generateAccessToken() {

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    private String generateSecurityCode() {

        int code = 100000 + secureRandom.nextInt(900000);

        return String.valueOf(code);
    }

    private String hashSecurityCode(String securityCode) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            securityCode.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    e
            );
        }
    }

    public record GeneratedOperatorAccess(
            String accessToken,
            String securityCode,
            Instant expiresAt
    ) {
    }
}