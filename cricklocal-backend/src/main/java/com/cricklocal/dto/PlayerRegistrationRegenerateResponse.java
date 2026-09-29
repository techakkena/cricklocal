package com.cricklocal.dto;

import java.time.Instant;

public record PlayerRegistrationRegenerateResponse(
        Long playerId,
        String displayName,
        String registrationStatus,
        String invitationStatus,
        Instant invitationCreatedAt,
        Instant invitationExpiresAt,
        String registrationLink
) {
}