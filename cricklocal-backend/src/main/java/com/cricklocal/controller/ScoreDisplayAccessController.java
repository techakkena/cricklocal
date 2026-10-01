package com.cricklocal.controller;

import com.cricklocal.entity.ScoreDisplayAccess;
import com.cricklocal.entity.User;
import com.cricklocal.service.AdminAccessService;
import com.cricklocal.service.AuthenticationService;
import com.cricklocal.service.ScoreDisplayAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/matches")
public class ScoreDisplayAccessController {

    private final ScoreDisplayAccessService displayAccessService;
    private final AuthenticationService authenticationService;
    private final AdminAccessService adminAccessService;

    public ScoreDisplayAccessController(
            ScoreDisplayAccessService displayAccessService,
            AuthenticationService authenticationService,
            AdminAccessService adminAccessService
    ) {
        this.displayAccessService = displayAccessService;
        this.authenticationService = authenticationService;
        this.adminAccessService = adminAccessService;
    }

    @PostMapping("/{matchId}/score-display/access")
    @ResponseStatus(HttpStatus.CREATED)
    public GenerateScoreDisplayAccessResponse generateAccess(
            @PathVariable Long matchId,
            Authentication authentication
    ) {
        requireAdmin(authentication);

        ScoreDisplayAccessService.GeneratedDisplayAccess generated =
                displayAccessService.generateAccess(matchId);

        return new GenerateScoreDisplayAccessResponse(
                matchId,
                generated.displayToken(),
                generated.expiresAt()
        );
    }

    @GetMapping("/score-display/access/{displayToken}")
    public ValidateScoreDisplayAccessResponse validateAccess(
            @PathVariable String displayToken
    ) {
        ScoreDisplayAccess access =
                displayAccessService.validateAccess(displayToken);

        return new ValidateScoreDisplayAccessResponse(
                access.getMatch().getId(),
                access.getExpiresAt()
        );
    }

    @DeleteMapping("/{matchId}/score-display/access")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeAccess(
            @PathVariable Long matchId,
            Authentication authentication
    ) {
        requireAdmin(authentication);
        displayAccessService.revokeAccess(matchId);
    }

    private void requireAdmin(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new AccessDeniedException(
                    "Admin access required"
            );
        }

        User user =
                authenticationService.getUserForGoogleLogin(oauth2User);

        if (!adminAccessService.hasActiveAdminAccess(user)) {
            throw new AccessDeniedException(
                    "Admin access required"
            );
        }
    }

    public record GenerateScoreDisplayAccessResponse(
            Long matchId,
            String displayToken,
            java.time.Instant expiresAt
    ) {
    }

    public record ValidateScoreDisplayAccessResponse(
            Long matchId,
            java.time.Instant expiresAt
    ) {
    }
}