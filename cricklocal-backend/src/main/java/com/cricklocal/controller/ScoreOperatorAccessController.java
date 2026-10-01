package com.cricklocal.controller;

import com.cricklocal.dto.GenerateScoreOperatorAccessResponse;
import com.cricklocal.dto.ValidateScoreOperatorAccessRequest;
import com.cricklocal.dto.ValidateScoreOperatorAccessResponse;
import com.cricklocal.entity.ScoreOperatorAccess;
import com.cricklocal.entity.User;
import com.cricklocal.exception.ForbiddenException;
import com.cricklocal.service.AdminAccessService;
import com.cricklocal.service.AuthenticationService;
import com.cricklocal.service.ScoreOperatorAccessService;
import com.cricklocal.service.ScoreOperatorSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class ScoreOperatorAccessController {

    private final ScoreOperatorAccessService accessService;
    private final ScoreOperatorSessionService sessionService;
    private final AuthenticationService authenticationService;
    private final AdminAccessService adminAccessService;

    public ScoreOperatorAccessController(
            ScoreOperatorAccessService accessService,
            ScoreOperatorSessionService sessionService,
            AuthenticationService authenticationService,
            AdminAccessService adminAccessService
    ) {
        this.accessService = accessService;
        this.sessionService = sessionService;
        this.authenticationService = authenticationService;
        this.adminAccessService = adminAccessService;
    }

    @PostMapping("/{matchId}/score-operator/access")
    @ResponseStatus(HttpStatus.CREATED)
    public GenerateScoreOperatorAccessResponse generateAccess(
            @PathVariable Long matchId,
            Authentication authentication
    ) {

        requireAdmin(authentication);

        ScoreOperatorAccessService.GeneratedOperatorAccess generated =
                accessService.generateAccess(matchId);

        return new GenerateScoreOperatorAccessResponse(
                matchId,
                generated.accessToken(),
                generated.securityCode(),
                generated.expiresAt()
        );
    }

    @PostMapping("/score-operator/access/validate")
    public ValidateScoreOperatorAccessResponse validateAccess(
            @Valid @RequestBody ValidateScoreOperatorAccessRequest request
    ) {

        ScoreOperatorAccess access =
                accessService.validateAccess(
                        request.accessToken(),
                        request.securityCode()
                );

        ScoreOperatorSessionService.CreatedOperatorSession session =
                sessionService.createSession(
                        access.getMatch().getId()
                );

        return new ValidateScoreOperatorAccessResponse(
                access.getMatch().getId(),
                session.sessionToken(),
                session.expiresAt()
        );
    }

    @DeleteMapping("/{matchId}/score-operator/access")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeAccess(
            @PathVariable Long matchId,
            Authentication authentication
    ) {

        requireAdmin(authentication);

        accessService.revokeAccess(matchId);
    }

    private User requireAdmin(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        if (!(authentication.getPrincipal()
                instanceof OAuth2User oauth2User)) {

            throw new IllegalStateException(
                    "Unsupported authentication provider"
            );
        }

        User user =
                authenticationService
                        .getUserForGoogleLogin(oauth2User);

        if (user == null) {
            throw new IllegalStateException(
                    "Authenticated user not found"
            );
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalStateException(
                    "User is inactive"
            );
        }

        if (!adminAccessService.hasActiveAdminAccess(user)) {
            throw new ForbiddenException(
                    "Active admin access is required"
            );
        }

        return user;
    }
}