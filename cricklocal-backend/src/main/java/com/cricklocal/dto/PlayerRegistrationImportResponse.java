package com.cricklocal.dto;

public record PlayerRegistrationImportResponse(
        Long playerId,
        String displayName,
        String registrationStatus,
        String invitationStatus,
        String registrationLink
) {
}