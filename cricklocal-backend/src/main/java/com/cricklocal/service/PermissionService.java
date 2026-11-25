package com.cricklocal.service;

import com.cricklocal.dto.EffectivePermissionResponse;
import com.cricklocal.entity.*;
import com.cricklocal.repository.PermissionModuleRepository;
import com.cricklocal.repository.RolePermissionRepository;
import com.cricklocal.repository.UserPermissionOverrideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PermissionService {
    private final PermissionModuleRepository moduleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserPermissionOverrideRepository overrideRepository;
    private final SuperAdminAuthorizationService superAdminAuthorizationService;

    public PermissionService(
            PermissionModuleRepository moduleRepository,
            RolePermissionRepository rolePermissionRepository,
            UserPermissionOverrideRepository overrideRepository,
            SuperAdminAuthorizationService superAdminAuthorizationService) {
        this.moduleRepository = moduleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.overrideRepository = overrideRepository;
        this.superAdminAuthorizationService = superAdminAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<EffectivePermissionResponse> getEffectivePermissions(User user) {
        if (user == null) return List.of();

        return moduleRepository.findAll().stream()
                .filter(module -> Boolean.TRUE.equals(module.getActive()))
                .map(module -> effectivePermission(user, module))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(User user, String moduleKey, PermissionAction action) {
        if (user == null) return false;
        if (superAdminAuthorizationService.isSuperAdmin(user)) return true;

        return getEffectivePermissions(user).stream()
                .filter(p -> p.moduleKey().equals(moduleKey))
                .findFirst()
                .map(p -> switch (action) {
                    case VIEW -> p.view();
                    case CREATE -> p.create();
                    case EDIT -> p.edit();
                    case DELETE -> p.delete();
                    case APPROVE -> p.approve();
                })
                .orElse(false);
    }

    private EffectivePermissionResponse effectivePermission(User user, PermissionModule module) {
        if (superAdminAuthorizationService.isSuperAdmin(user)) {
            return new EffectivePermissionResponse(
                    module.getModuleKey(), module.getModuleName(), true, true, true, true, true);
        }

        RolePermission defaults = rolePermissionRepository
                .findByRoleAndModuleId(user.getRole(), module.getId())
                .orElse(null);

        UserPermissionOverride override = overrideRepository
                .findByUserIdAndModuleId(user.getId(), module.getId())
                .orElse(null);

        return new EffectivePermissionResponse(
                module.getModuleKey(),
                module.getModuleName(),
                value(override == null ? null : override.getCanView(),
                        defaults == null ? null : defaults.getCanView()),
                value(override == null ? null : override.getCanCreate(),
                        defaults == null ? null : defaults.getCanCreate()),
                value(override == null ? null : override.getCanEdit(),
                        defaults == null ? null : defaults.getCanEdit()),
                value(override == null ? null : override.getCanDelete(),
                        defaults == null ? null : defaults.getCanDelete()),
                value(override == null ? null : override.getCanApprove(),
                        defaults == null ? null : defaults.getCanApprove())
        );
    }

    private boolean value(Boolean override, Boolean defaultValue) {
        return override != null ? override : Boolean.TRUE.equals(defaultValue);
    }
}
