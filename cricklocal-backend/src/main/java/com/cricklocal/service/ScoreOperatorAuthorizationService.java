package com.cricklocal.service;

import com.cricklocal.entity.ScoreOperatorSession;
import org.springframework.stereotype.Service;

@Service
public class ScoreOperatorAuthorizationService {

    private static final String SESSION_HEADER =
            "X-Score-Operator-Session";

    private final ScoreOperatorSessionService sessionService;

    public ScoreOperatorAuthorizationService(
            ScoreOperatorSessionService sessionService) {

        this.sessionService = sessionService;
    }

    public ScoreOperatorSession requireSessionForInnings(
            String sessionToken,
            Long inningsId) {

        return sessionService.validateSessionForInnings(
                sessionToken,
                inningsId
        );
    }

    public static String getSessionHeader() {
        return SESSION_HEADER;
    }
}