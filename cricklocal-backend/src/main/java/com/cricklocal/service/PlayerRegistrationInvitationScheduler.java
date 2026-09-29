package com.cricklocal.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PlayerRegistrationInvitationScheduler {

    private final PlayerRegistrationInvitationMaintenanceService
            maintenanceService;

    public PlayerRegistrationInvitationScheduler(
            PlayerRegistrationInvitationMaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void expireInvitations() {
        maintenanceService.markExpiredInvitations();
    }
}