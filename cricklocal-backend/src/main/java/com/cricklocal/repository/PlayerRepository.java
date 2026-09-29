package com.cricklocal.repository;

import com.cricklocal.entity.Player;
import com.cricklocal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    boolean existsByDisplayName(String displayName);

    Optional<Player> findByUser(User user);
}