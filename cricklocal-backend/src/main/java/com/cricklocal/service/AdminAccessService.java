package com.cricklocal.service;

import com.cricklocal.entity.AdminEntitlement;
import com.cricklocal.entity.User;
import com.cricklocal.enums.AdminSubscriptionStatus;
import com.cricklocal.repository.AdminEntitlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAccessService {

    private final AdminEntitlementRepository adminEntitlementRepository;

    public AdminAccessService(
            AdminEntitlementRepository adminEntitlementRepository
    ) {
        this.adminEntitlementRepository = adminEntitlementRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasActiveAdminAccess(User user) {

        if (user == null
                || user.getAccountStatus() != com.cricklocal.entity.UserAccountStatus.ACTIVE
                || !Boolean.TRUE.equals(user.getActive())) {
            return false;
        }

        AdminEntitlement entitlement =
                adminEntitlementRepository
                        .findByUser(user)
                        .orElse(null);

        if (entitlement == null) {
            return false;
        }

        if (entitlement.getStatus() != AdminSubscriptionStatus.ACTIVE) {
            return false;
        }

        java.time.Instant now = java.time.Instant.now();

        if (entitlement.getStartsAt() != null
                && entitlement.getStartsAt().isAfter(now)) {
            return false;
        }

        if (entitlement.getExpiresAt() != null
                && entitlement.getExpiresAt().isBefore(now)) {
            return false;
        }

        return true;
    }
    @Transactional
    public AdminEntitlement activateAdminAccess(
            User user,
            java.time.Instant startsAt,
            java.time.Instant expiresAt
    ) {
        AdminEntitlement entitlement =
                adminEntitlementRepository
                        .findByUser(user)
                        .orElseGet(() -> {
                            AdminEntitlement newEntitlement =
                                    new AdminEntitlement();
                            newEntitlement.setUser(user);
                            return newEntitlement;
                        });

        entitlement.setStatus(AdminSubscriptionStatus.ACTIVE);
        entitlement.setStartsAt(startsAt);
        entitlement.setExpiresAt(expiresAt);

        return adminEntitlementRepository.save(entitlement);
    }

    @Transactional
    public void deactivateAdminAccess(User user) {

        adminEntitlementRepository
                .findByUser(user)
                .ifPresent(entitlement -> {
                    entitlement.setStatus(
                            AdminSubscriptionStatus.CANCELLED
                    );
                    adminEntitlementRepository.save(entitlement);
                });
    }
}