package com.cricklocal.dto;

import java.time.Instant;

public record PlayerRegistrationManagementResponse(
        Long playerId,
        String displayName,
        String firstName,
        String lastName,
        String registrationStatus,
        String invitationStatus,
        Instant invitationCreatedAt,
        Instant invitationExpiresAt
) {
}