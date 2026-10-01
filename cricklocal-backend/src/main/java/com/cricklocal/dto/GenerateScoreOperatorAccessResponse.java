package com.cricklocal.dto;

import java.time.Instant;

public record GenerateScoreOperatorAccessResponse(
        Long matchId,
        String accessToken,
        String securityCode,
        Instant expiresAt
) {
}