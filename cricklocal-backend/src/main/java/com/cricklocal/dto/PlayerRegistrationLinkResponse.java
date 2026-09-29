package com.cricklocal.dto;

import java.time.Instant;

public record PlayerRegistrationLinkResponse(
        Long playerId,
        String displayName,
        String registrationStatus,
        String invitationStatus,
        String registrationLink,
        Instant expiresAt
) {
}