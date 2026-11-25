package com.cricklocal.service;

import com.cricklocal.dto.EffectivePermissionResponse;
import com.cricklocal.entity.*;
import com.cricklocal.repository.PermissionModuleRepository;
import com.cricklocal.repository.RolePermissionRepository;
import com.cricklocal.repository.UserPermissionOverrideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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
        if (user == null
                || user.getAccountStatus() != UserAccountStatus.ACTIVE
                || !Boolean.TRUE.equals(user.getActive())) {
            return List.of();
        }

        List<PermissionModule> modules = moduleRepository.findAll().stream()
                .filter(module -> Boolean.TRUE.equals(module.getActive()))
                .toList();

        if (modules.isEmpty()) {
            return List.of();
        }

        Map<Long, RolePermission> rolePermissions =
                rolePermissionRepository.findAllByRole(user.getRole()).stream()
                        .collect(java.util.stream.Collectors.toMap(
                                permission -> permission.getModule().getId(),
                                permission -> permission,
                                (first, second) -> first));

        Map<Long, UserPermissionOverride> overrides =
                overrideRepository.findAllByUserId(user.getId()).stream()
                        .collect(java.util.stream.Collectors.toMap(
                                override -> override.getModule().getId(),
                                override -> override,
                                (first, second) -> first));

        return modules.stream()
                .map(module -> effectivePermission(
                        user,
                        module,
                        rolePermissions.get(module.getId()),
                        overrides.get(module.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean hasPermission(User user, String moduleKey, PermissionAction action) {
        if (user == null
                || user.getAccountStatus() != UserAccountStatus.ACTIVE
                || !Boolean.TRUE.equals(user.getActive())
                || moduleKey == null
                || moduleKey.isBlank()
                || action == null) {
            return false;
        }

        if (superAdminAuthorizationService.isSuperAdmin(user)) {
            return true;
        }

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

    private EffectivePermissionResponse effectivePermission(
            User user,
            PermissionModule module,
            RolePermission defaults,
            UserPermissionOverride override) {
        if (superAdminAuthorizationService.isSuperAdmin(user)) {
            return new EffectivePermissionResponse(
                    module.getModuleKey(), module.getModuleName(), true, true, true, true, true);
        }

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
