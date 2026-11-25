package com.cricklocal.service;

import com.cricklocal.entity.PermissionAction;
import com.cricklocal.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class PermissionAuthorizationService {
    private final AuthenticationService authenticationService;
    private final PermissionService permissionService;

    public PermissionAuthorizationService(
            AuthenticationService authenticationService,
            PermissionService permissionService) {
        this.authenticationService = authenticationService;
        this.permissionService = permissionService;
    }

    public boolean isAllowed(
            Authentication authentication,
            String moduleKey,
            PermissionAction action) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            return false;
        }

        User user = authenticationService.getUserForGoogleLogin(oauth2User);

        if (user == null
                || user.getAccountStatus() != com.cricklocal.entity.UserAccountStatus.ACTIVE
                || !Boolean.TRUE.equals(user.getActive())) {
            return false;
        }

        if (moduleKey == null || moduleKey.isBlank() || action == null) {
            return false;
        }

        return permissionService.hasPermission(user, moduleKey, action);
    }
}
