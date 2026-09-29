package com.cricklocal.repository;

import com.cricklocal.entity.Player;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.enums.PlayerRegistrationInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRegistrationInvitationRepository
        extends JpaRepository<PlayerRegistrationInvitation, Long> {

    Optional<PlayerRegistrationInvitation> findByToken(String token);

    List<PlayerRegistrationInvitation> findByPlayer(Player player);

    Optional<PlayerRegistrationInvitation> findByPlayerAndStatus(
            Player player,
            PlayerRegistrationInvitationStatus status
    );

    boolean existsByToken(String token);

    List<PlayerRegistrationInvitation> findByPlayerIn(List<Player> players);
}