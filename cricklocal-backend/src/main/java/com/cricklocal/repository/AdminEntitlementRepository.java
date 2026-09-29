package com.cricklocal.repository;

import com.cricklocal.entity.AdminEntitlement;
import com.cricklocal.entity.User;
import com.cricklocal.enums.AdminSubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminEntitlementRepository
        extends JpaRepository<AdminEntitlement, Long> {

    Optional<AdminEntitlement> findByUser(User user);

    Optional<AdminEntitlement> findByUserAndStatus(
            User user,
            AdminSubscriptionStatus status
    );

    boolean existsByUserAndStatus(
            User user,
            AdminSubscriptionStatus status
    );
}