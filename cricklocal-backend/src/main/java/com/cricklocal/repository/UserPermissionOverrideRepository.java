package com.cricklocal.repository;

import com.cricklocal.entity.PermissionAction;
import com.cricklocal.entity.UserPermissionOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPermissionOverrideRepository
        extends JpaRepository<UserPermissionOverride, Long> {

    Optional<UserPermissionOverride>
    findByUser_IdAndModule_ModuleKeyAndAction(
            Long userId,
            String moduleKey,
            PermissionAction action
    );

    List<UserPermissionOverride> findByUser_Id(Long userId);

    void deleteByUser_Id(Long userId);
}