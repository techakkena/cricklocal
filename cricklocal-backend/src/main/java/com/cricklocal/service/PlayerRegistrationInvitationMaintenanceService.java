package com.cricklocal.service;

import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.enums.PlayerRegistrationInvitationStatus;
import com.cricklocal.repository.PlayerRegistrationInvitationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class PlayerRegistrationInvitationMaintenanceService {

    private final PlayerRegistrationInvitationRepository invitationRepository;

    public PlayerRegistrationInvitationMaintenanceService(
            PlayerRegistrationInvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Transactional
    public int markExpiredInvitations() {

        Instant now = Instant.now();

        List<PlayerRegistrationInvitation> invitations =
                invitationRepository.findAll();

        int expiredCount = 0;

        for (PlayerRegistrationInvitation invitation : invitations) {

            if (invitation.getStatus()
                    != PlayerRegistrationInvitationStatus.PENDING) {
                continue;
            }

            if (invitation.getExpiresAt() == null) {
                continue;
            }

            if (!invitation.getExpiresAt().isAfter(now)) {

                invitation.setStatus(
                        PlayerRegistrationInvitationStatus.EXPIRED
                );

                expiredCount++;
            }
        }

        if (expiredCount > 0) {
            invitationRepository.saveAll(invitations);
        }

        return expiredCount;
    }
}