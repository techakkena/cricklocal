package com.cricklocal.service;

import com.cricklocal.entity.User;
import com.cricklocal.entity.UserAccountStatus;
import com.cricklocal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SuperAdminAuthorizationService {

    private final UserRepository userRepository;
    private final String superAdminEmail;

    public SuperAdminAuthorizationService(
            UserRepository userRepository,
            @Value("${app.security.super-admin-email:}") String superAdminEmail
    ) {
        this.userRepository = userRepository;
        this.superAdminEmail = superAdminEmail;
    }

    @Transactional(readOnly = true)
    public boolean isSuperAdmin(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            return false;
        }

        String email = oauth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedSuperAdminEmail =
                superAdminEmail == null
                        ? ""
                        : superAdminEmail.trim().toLowerCase();

        if (normalizedSuperAdminEmail.isBlank()
                || !normalizedEmail.equals(normalizedSuperAdminEmail)) {
            return false;
        }

        User user = userRepository.findByEmail(normalizedEmail).orElse(null);

        return user != null
                && user.getAccountStatus() == UserAccountStatus.ACTIVE
                && Boolean.TRUE.equals(user.getActive());
    }

    @Transactional(readOnly = true)
    public boolean isSuperAdmin(User user) {

        if (user == null
                || user.getAccountStatus() != UserAccountStatus.ACTIVE
                || !Boolean.TRUE.equals(user.getActive())) {
            return false;
        }

        String normalizedEmail =
                user.getEmail() == null
                        ? ""
                        : user.getEmail().trim().toLowerCase();

        String normalizedSuperAdminEmail =
                superAdminEmail == null
                        ? ""
                        : superAdminEmail.trim().toLowerCase();

        return !normalizedSuperAdminEmail.isBlank()
                && normalizedEmail.equals(normalizedSuperAdminEmail);
    }
}