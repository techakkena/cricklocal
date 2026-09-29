package com.cricklocal.dto;

public record PlayerRegistrationSummaryResponse(
        long totalPlayers,
        long registeredPlayers,
        long pendingPlayers,
        long expiredInvitations
) {
}