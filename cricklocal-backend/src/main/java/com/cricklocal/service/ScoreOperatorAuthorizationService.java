package com.cricklocal.service;

import com.cricklocal.entity.ScoreOperatorSession;
import org.springframework.stereotype.Service;
import com.cricklocal.exception.ForbiddenException;

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

    public ScoreOperatorSession requireSessionForMatch(
            String sessionToken,
            Long matchId) {

        ScoreOperatorSession session =
                sessionService.validateSession(sessionToken);

        if (!session.getMatch().getId().equals(matchId)) {
            throw new ForbiddenException(
                    "Score operator session does not belong to this match");
        }

        return session;
    }
}