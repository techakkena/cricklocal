package com.cricklocal.service;

import com.cricklocal.entity.Player;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.entity.User;
import com.cricklocal.enums.PlayerRegistrationInvitationStatus;
import com.cricklocal.repository.PlayerRegistrationInvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class PlayerRegistrationInvitationService {

    private static final Duration DEFAULT_INVITATION_LIFETIME =
            Duration.ofDays(7);

    private static final int TOKEN_BYTES = 32;

    private final PlayerRegistrationInvitationRepository invitationRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public PlayerRegistrationInvitationService(
            PlayerRegistrationInvitationRepository invitationRepository
    ) {
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public PlayerRegistrationInvitation createInvitation(
            Player player,
            User invitedBy
    ) {
        return createInvitation(
                player,
                invitedBy,
                DEFAULT_INVITATION_LIFETIME
        );
    }

    @Transactional
    public PlayerRegistrationInvitation createInvitation(
            Player player,
            User invitedBy,
            Duration lifetime
    ) {
        if (player == null) {
            throw new IllegalArgumentException("Player is required");
        }

        if (lifetime == null || lifetime.isZero() || lifetime.isNegative()) {
            throw new IllegalArgumentException(
                    "Invitation lifetime must be positive"
            );
        }

        String token = generateUniqueToken();

        PlayerRegistrationInvitation invitation =
                new PlayerRegistrationInvitation();

        invitation.setPlayer(player);
        invitation.setToken(token);
        invitation.setStatus(
                PlayerRegistrationInvitationStatus.PENDING
        );
        invitation.setExpiresAt(
                Instant.now().plus(lifetime)
        );
        invitation.setInvitedBy(invitedBy);

        return invitationRepository.save(invitation);
    }

    @Transactional(readOnly = true)
    public PlayerRegistrationInvitation validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "Invitation token is required"
            );
        }

        PlayerRegistrationInvitation invitation =
                invitationRepository.findByToken(token)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid invitation token"
                                )
                        );

        if (invitation.getStatus()
                != PlayerRegistrationInvitationStatus.PENDING) {
            throw new IllegalStateException(
                    "Invitation is no longer valid"
            );
        }

        if (invitation.getExpiresAt() == null
                || !invitation.getExpiresAt().isAfter(Instant.now())) {
            throw new IllegalStateException(
                    "Invitation has expired"
            );
        }

        return invitation;
    }

    @Transactional
    public void cancelInvitation(
            PlayerRegistrationInvitation invitation
    ) {
        if (invitation == null) {
            throw new IllegalArgumentException(
                    "Invitation is required"
            );
        }

        if (invitation.getStatus()
                != PlayerRegistrationInvitationStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending invitations can be cancelled"
            );
        }

        invitation.setStatus(
                PlayerRegistrationInvitationStatus.CANCELLED
        );

        invitationRepository.save(invitation);
    }

    @Transactional
    public PlayerRegistrationInvitation regenerateInvitation(
            PlayerRegistrationInvitation oldInvitation,
            User invitedBy
    ) {
        if (oldInvitation == null) {
            throw new IllegalArgumentException(
                    "Existing invitation is required"
            );
        }

        Player player = oldInvitation.getPlayer();

        if (oldInvitation.getStatus()
                == PlayerRegistrationInvitationStatus.PENDING) {

            oldInvitation.setStatus(
                    PlayerRegistrationInvitationStatus.CANCELLED
            );

            invitationRepository.save(oldInvitation);
        }

        return createInvitation(player, invitedBy);
    }

    private String generateUniqueToken() {
        String token;

        do {
            byte[] bytes = new byte[TOKEN_BYTES];
            secureRandom.nextBytes(bytes);

            token = Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(bytes);

        } while (invitationRepository.existsByToken(token));

        return token;
    }
}