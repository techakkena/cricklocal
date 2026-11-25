package com.cricklocal.repository;

import com.cricklocal.entity.RolePermission;
import com.cricklocal.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    Optional<RolePermission> findByRoleAndModuleId(UserRole role, Long moduleId);

    List<RolePermission> findAllByRole(UserRole role);
}
