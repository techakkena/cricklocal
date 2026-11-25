package com.cricklocal.service;

import com.cricklocal.dto.EffectivePermissionResponse;
import com.cricklocal.dto.PermissionOverrideRequest;
import com.cricklocal.dto.PermissionModuleDefaultsResponse;
import com.cricklocal.dto.SuperAdminUserPermissionResponse;
import com.cricklocal.dto.UserPermissionManagementResponse;
import com.cricklocal.entity.PermissionModule;
import com.cricklocal.entity.RolePermission;
import com.cricklocal.entity.User;
import com.cricklocal.entity.UserPermissionOverride;
import com.cricklocal.repository.PermissionModuleRepository;
import com.cricklocal.repository.UserPermissionOverrideRepository;
import com.cricklocal.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SuperAdminPermissionService {

    private final UserRepository userRepository;
    private final PermissionModuleRepository moduleRepository;
    private final UserPermissionOverrideRepository overrideRepository;
    private final PermissionService permissionService;
    private final com.cricklocal.repository.RolePermissionRepository permissionServiceRoleRepository;
    private final SuperAdminAuthorizationService superAdminAuthorizationService;

    public SuperAdminPermissionService(
            UserRepository userRepository,
            PermissionModuleRepository moduleRepository,
            UserPermissionOverrideRepository overrideRepository,
            PermissionService permissionService,
            SuperAdminAuthorizationService superAdminAuthorizationService,
            com.cricklocal.repository.RolePermissionRepository permissionServiceRoleRepository) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.overrideRepository = overrideRepository;
        this.permissionService = permissionService;
        this.superAdminAuthorizationService = superAdminAuthorizationService;
        this.permissionServiceRoleRepository = permissionServiceRoleRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionModuleDefaultsResponse> getAllModules() {
        return moduleRepository.findAll().stream()
                .filter(module -> Boolean.TRUE.equals(module.getActive()))
                .map(this::toModuleDefaults)
                .toList();
    }

    private PermissionModuleDefaultsResponse toModuleDefaults(PermissionModule module) {
        RolePermission player = permissionServiceDefault(module, com.cricklocal.entity.UserRole.PLAYER);
        RolePermission admin = permissionServiceDefault(module, com.cricklocal.entity.UserRole.ADMIN);

        return new PermissionModuleDefaultsResponse(
                module.getModuleKey(),
                module.getModuleName(),
                toSet(player),
                toSet(admin)
        );
    }

    private RolePermission permissionServiceDefault(
            PermissionModule module,
            com.cricklocal.entity.UserRole role) {
        return permissionServiceRoleRepository.findByRoleAndModuleId(role, module.getId()).orElse(null);
    }

    private PermissionModuleDefaultsResponse.PermissionSet toSet(RolePermission permission) {
        if (permission == null) {
            return new PermissionModuleDefaultsResponse.PermissionSet(
                    false, false, false, false, false);
        }
        return new PermissionModuleDefaultsResponse.PermissionSet(
                Boolean.TRUE.equals(permission.getCanView()),
                Boolean.TRUE.equals(permission.getCanCreate()),
                Boolean.TRUE.equals(permission.getCanEdit()),
                Boolean.TRUE.equals(permission.getCanDelete()),
                Boolean.TRUE.equals(permission.getCanApprove()));
    }

    @Transactional(readOnly = true)
    public List<SuperAdminUserPermissionResponse> getUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !isSuperAdmin(user))
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserPermissionManagementResponse getUserPermissions(Long userId) {
        User user = getUser(userId);
        return new UserPermissionManagementResponse(
                toUserResponse(user),
                permissionService.getEffectivePermissions(user));
    }

    @Transactional
    public UserPermissionManagementResponse updateUserPermissions(
            Long userId,
            List<PermissionOverrideRequest> requests) {

        User user = getUser(userId);

        if (isSuperAdmin(user)) {
            throw new IllegalArgumentException(
                    "Super Admin permissions cannot be overridden");
        }

        for (PermissionOverrideRequest request : requests) {
            PermissionModule module = moduleRepository
                    .findByModuleKey(request.moduleKey())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown permission module: " + request.moduleKey()));

            UserPermissionOverride override = overrideRepository
                    .findByUserIdAndModuleId(user.getId(), module.getId())
                    .orElseGet(UserPermissionOverride::new);

            override.setUser(user);
            override.setModule(module);
            override.setCanView(request.view());
            override.setCanCreate(request.create());
            override.setCanEdit(request.edit());
            override.setCanDelete(request.delete());
            override.setCanApprove(request.approve());

            overrideRepository.save(override);
        }

        return getUserPermissions(userId);
    }

    @Transactional
    public UserPermissionManagementResponse resetUserPermissions(Long userId) {
        User user = getUser(userId);

        if (isSuperAdmin(user)) {
            throw new IllegalArgumentException(
                    "Super Admin permissions cannot be reset");
        }

        overrideRepository.deleteByUserId(userId);

        return getUserPermissions(userId);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found: " + userId));
    }

    private SuperAdminUserPermissionResponse toUserResponse(User user) {
        return new SuperAdminUserPermissionResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                Boolean.TRUE.equals(user.getActive()));
    }

    private boolean isSuperAdmin(User user) {
        return superAdminAuthorizationService.isSuperAdmin(user);
    }
}
