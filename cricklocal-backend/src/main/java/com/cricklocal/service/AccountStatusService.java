package com.cricklocal.service;

import com.cricklocal.entity.User;
import com.cricklocal.entity.UserAccountStatus;
import com.cricklocal.entity.UserAuthentication;
import com.cricklocal.enums.AuthenticationProvider;
import com.cricklocal.repository.UserAuthenticationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountStatusService {

    private final UserAuthenticationRepository userAuthenticationRepository;

    public AccountStatusService(
            UserAuthenticationRepository userAuthenticationRepository) {
        this.userAuthenticationRepository =
                userAuthenticationRepository;
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(String providerUserId) {

        UserAuthentication authentication =
                userAuthenticationRepository
                        .findByProviderAndProviderUserId(
                                AuthenticationProvider.GOOGLE,
                                providerUserId)
                        .orElse(null);

        if (authentication == null) {
            return false;
        }

        User user = authentication.getUser();

        return user.getAccountStatus() == UserAccountStatus.BLOCKED
                || !Boolean.TRUE.equals(user.getActive());
    }
}