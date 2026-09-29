package com.cricklocal.dto;

import java.time.Instant;

public record PlayerRegistrationDetailResponse(
        Long playerId,
        String displayName,
        String firstName,
        String lastName,
        String phone,
        String battingStyle,
        String bowlingStyle,
        String role,
        String registrationStatus,
        Long userId,
        String userEmail,
        String userDisplayName,
        String invitationStatus,
        Instant invitationCreatedAt,
        Instant invitationExpiresAt,
        Instant invitationCompletedAt
) {
}