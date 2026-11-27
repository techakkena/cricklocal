package com.cricklocal.service;

import com.cricklocal.entity.User;
import com.cricklocal.entity.UserRole;
import com.cricklocal.entity.UserAuthentication;
import com.cricklocal.entity.UserAccountStatus;
import com.cricklocal.enums.AuthenticationProvider;
import com.cricklocal.repository.UserAuthenticationRepository;
import com.cricklocal.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.time.Instant;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;
    private final String superAdminEmail;

    public AuthenticationService(
            UserRepository userRepository,
            UserAuthenticationRepository userAuthenticationRepository,
            @Value("${app.security.super-admin-email:}") String superAdminEmail
    ) {
        this.userRepository = userRepository;
        this.userAuthenticationRepository = userAuthenticationRepository;
        this.superAdminEmail = superAdminEmail;
    }

    @Transactional
    public User processGoogleLogin(OAuth2User oauth2User) {

        String providerUserId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String displayName = oauth2User.getAttribute("name");

        Boolean emailVerified =
                oauth2User.getAttribute("email_verified");

        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalStateException("Google user ID is missing");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalStateException("Google email is missing");
        }

        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new IllegalStateException(
                    "Google email address is not verified"
            );
        }

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedSuperAdminEmail =
                superAdminEmail == null
                        ? ""
                        : superAdminEmail.trim().toLowerCase();

        UserAuthentication authentication =
                userAuthenticationRepository
                        .findByProviderAndProviderUserId(
                                AuthenticationProvider.GOOGLE,
                                providerUserId
                        )
                        .orElse(null);

        if (authentication != null) {

            User user = authentication.getUser();
            Hibernate.initialize(user);

            /*
             * Only the explicitly configured Super Admin email
             * can be promoted to SUPER_ADMIN.
             *
             * Normal users keep their existing role.
             */
            if (!normalizedSuperAdminEmail.isBlank()
                        && normalizedEmail.equals(normalizedSuperAdminEmail)
                        && user.getRole() != UserRole.SUPER_ADMIN
                        && user.getAccountStatus() != UserAccountStatus.BLOCKED) {

                user.setRole(UserRole.SUPER_ADMIN);
                user = userRepository.save(user);                                                                                                                                                                              
        }

            authentication.setLastLoginAt(Instant.now());
            userAuthenticationRepository.save(authentication);

            return user;
        }

        User user = userRepository
                .findByEmail(normalizedEmail)
                .orElse(null);

        if (user == null) {

            user = new User();

            user.setEmail(normalizedEmail);

            user.setDisplayName(
                    displayName != null && !displayName.isBlank()
                            ? displayName
                            : normalizedEmail
            );

            /*
             * Every normal new Google account starts as PLAYER.
             */
            user.setRole(UserRole.PLAYER);

            user.setAccountStatus(UserAccountStatus.ACTIVE);
            user.setActive(true);

            /*
             * The configured verified Google email is the
             * only exception to the normal PLAYER assignment.
             */
            if (!normalizedSuperAdminEmail.isBlank()
                    && normalizedEmail.equals(normalizedSuperAdminEmail)) {

                user.setRole(UserRole.SUPER_ADMIN);
            }

            user = userRepository.save(user);

        } else {

            /*
             * Existing account found by email.
             *
             * Never downgrade an existing role.
             * Only promote the explicitly configured Super Admin.
             */
            if (!normalizedSuperAdminEmail.isBlank()
                        && normalizedEmail.equals(normalizedSuperAdminEmail)
                        && user.getRole() != UserRole.SUPER_ADMIN
                        && user.getAccountStatus() != UserAccountStatus.BLOCKED) {

                user.setRole(UserRole.SUPER_ADMIN);
                user = userRepository.save(user);
            }
        }

        UserAuthentication newAuthentication =
                new UserAuthentication();

        newAuthentication.setUser(user);
        newAuthentication.setProvider(
                AuthenticationProvider.GOOGLE
        );
        newAuthentication.setProviderUserId(providerUserId);
        newAuthentication.setLastLoginAt(Instant.now());

        userAuthenticationRepository.save(newAuthentication);

        return user;
    }

    @Transactional(readOnly = true)
    public User getUserForGoogleLogin(OAuth2User oauth2User) {

        String providerUserId =
                oauth2User.getAttribute("sub");

        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalStateException(
                    "Google user ID is missing"
            );
        }

        UserAuthentication authentication =
                userAuthenticationRepository
                        .findByProviderAndProviderUserId(
                                AuthenticationProvider.GOOGLE,
                                providerUserId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "CricketLocal Google identity not found"
                                )
                        );

        User user = authentication.getUser();
        Hibernate.initialize(user);

        return user;
    }
}