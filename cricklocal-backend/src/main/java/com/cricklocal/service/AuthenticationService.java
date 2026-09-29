package com.cricklocal.service;

import com.cricklocal.entity.User;
import com.cricklocal.entity.UserAuthentication;
import com.cricklocal.enums.AuthenticationProvider;
import com.cricklocal.enums.UserRole;
import com.cricklocal.repository.UserAuthenticationRepository;
import com.cricklocal.repository.UserRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.time.Instant;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserAuthenticationRepository userAuthenticationRepository;

    public AuthenticationService(
            UserRepository userRepository,
            UserAuthenticationRepository userAuthenticationRepository
    ) {
        this.userRepository = userRepository;
        this.userAuthenticationRepository = userAuthenticationRepository;
    }

    @Transactional
    public User processGoogleLogin(OAuth2User oauth2User) {
        String providerUserId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String displayName = oauth2User.getAttribute("name");

        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalStateException("Google user ID is missing");
        }

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
                authentication.setLastLoginAt(Instant.now());
                userAuthenticationRepository.save(authentication);
                return user;
        }

        User user = userRepository
                .findByEmail(email)
                .orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setDisplayName(
                    displayName != null && !displayName.isBlank()
                            ? displayName
                            : email
            );
            user.setRole(UserRole.PLAYER);
            user.setActive(true);

            user = userRepository.save(user);
        }

        UserAuthentication newAuthentication = new UserAuthentication();
        newAuthentication.setUser(user);
        newAuthentication.setProvider(AuthenticationProvider.GOOGLE);
        newAuthentication.setProviderUserId(providerUserId);
        newAuthentication.setLastLoginAt(Instant.now());

        userAuthenticationRepository.save(newAuthentication);

        return user;
    }

    @Transactional(readOnly = true)
    public User getUserForGoogleLogin(OAuth2User oauth2User) {

        String providerUserId = oauth2User.getAttribute("sub");

        if (providerUserId == null || providerUserId.isBlank()) {
            throw new IllegalStateException("Google user ID is missing");
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