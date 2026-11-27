package com.cricklocal.service;

import com.cricklocal.entity.PermissionAction;
import com.cricklocal.entity.User;
import com.cricklocal.entity.UserAccountStatus;
import com.cricklocal.entity.UserRole;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service("permissionAuthorizationService")
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

        User user =
                authenticationService.getUserForGoogleLogin(oauth2User);

        if (user == null) {
            return false;
        }

        if (user.getAccountStatus() != UserAccountStatus.ACTIVE) {
            return false;
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            return false;
        }

        return permissionService.hasPermission(
                user,
                moduleKey,
                action
        );
    }
    public boolean isSuperAdmin(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {

            return false;
        }

        User user =
                authenticationService.getUserForGoogleLogin(oauth2User);

        if (user == null) {
            return false;
        }

        return user.getRole() == UserRole.SUPER_ADMIN
                && user.getAccountStatus() == UserAccountStatus.ACTIVE
                && Boolean.TRUE.equals(user.getActive());
    }

}