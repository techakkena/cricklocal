package com.cricklocal.dto;

public record PlayerRegistrationInvitationResponse(
        Long playerId,
        String displayName,
        String firstName,
        String lastName,
        String battingStyle,
        String bowlingStyle,
        String role,
        String registrationStatus
) {
}