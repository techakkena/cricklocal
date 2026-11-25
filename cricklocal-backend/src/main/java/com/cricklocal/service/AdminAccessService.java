package com.cricklocal.service;

import com.cricklocal.entity.AdminEntitlement;
import com.cricklocal.enums.AdminSubscriptionStatus;
import com.cricklocal.entity.User;
import com.cricklocal.repository.AdminEntitlementRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAccessService {

    private final AdminEntitlementRepository adminEntitlementRepository;
    private final String superAdminEmail;

    public AdminAccessService(
            AdminEntitlementRepository adminEntitlementRepository,
            @Value("${app.security.super-admin-email:}") String superAdminEmail
    ) {
        this.adminEntitlementRepository = adminEntitlementRepository;
        this.superAdminEmail = superAdminEmail;
    }

    @Transactional(readOnly = true)
    public boolean hasActiveAdminAccess(User user) {

        if (user == null
                || user.getAccountStatus() != com.cricklocal.entity.UserAccountStatus.ACTIVE
                || !Boolean.TRUE.equals(user.getActive())) {
            return false;
        }

        if (isConfiguredSuperAdmin(user)) {
            return true;
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

    private boolean isConfiguredSuperAdmin(User user) {

        String normalizedUserEmail =
                user.getEmail() == null
                        ? ""
                        : user.getEmail().trim().toLowerCase();

        String normalizedSuperAdminEmail =
                superAdminEmail == null
                        ? ""
                        : superAdminEmail.trim().toLowerCase();

        return !normalizedSuperAdminEmail.isBlank()
                && normalizedUserEmail.equals(normalizedSuperAdminEmail);
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
                    entitlement.setStatus(AdminSubscriptionStatus.CANCELLED);
                    adminEntitlementRepository.save(entitlement);
                });
    }
}
