package com.cricklocal.repository;

import com.cricklocal.entity.UserPermissionOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserPermissionOverrideRepository extends JpaRepository<UserPermissionOverride, Long> {
    Optional<UserPermissionOverride> findByUserIdAndModuleId(Long userId, Long moduleId);

    void deleteByUserId(Long userId);
}
