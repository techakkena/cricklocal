package com.cricklocal.dto;

import java.time.Instant;

public record ValidateScoreOperatorAccessResponse(
        Long matchId,
        String sessionToken,
        Instant expiresAt
) {}