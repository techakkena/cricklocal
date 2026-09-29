package com.cricklocal.repository;

import com.cricklocal.entity.UserAuthentication;
import com.cricklocal.enums.AuthenticationProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAuthenticationRepository
        extends JpaRepository<UserAuthentication, Long> {

    Optional<UserAuthentication> findByProviderAndProviderUserId(
            AuthenticationProvider provider,
            String providerUserId
    );

    boolean existsByProviderAndProviderUserId(
            AuthenticationProvider provider,
            String providerUserId
    );
}