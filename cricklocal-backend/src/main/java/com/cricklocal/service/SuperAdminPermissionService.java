package com.cricklocal.service;

import com.cricklocal.dto.EffectivePermissionResponse;
import com.cricklocal.dto.PermissionOverrideRequest;
import com.cricklocal.dto.SuperAdminUserPermissionResponse;
import com.cricklocal.dto.UserPermissionManagementResponse;
import com.cricklocal.entity.PermissionModule;
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

    public SuperAdminPermissionService(
            UserRepository userRepository,
            PermissionModuleRepository moduleRepository,
            UserPermissionOverrideRepository overrideRepository,
            PermissionService permissionService) {
        this.userRepository = userRepository;
        this.moduleRepository = moduleRepository;
        this.overrideRepository = overrideRepository;
        this.permissionService = permissionService;
    }

    @Transactional(readOnly = true)
    public List<EffectivePermissionResponse> getAllModules() {
        return moduleRepository.findAll().stream()
                .filter(module -> Boolean.TRUE.equals(module.getActive()))
                .map(module -> new EffectivePermissionResponse(
                        module.getModuleKey(),
                        module.getModuleName(),
                        false, false, false, false, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SuperAdminUserPermissionResponse> getUsers() {
        return userRepository.findAll().stream()
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

        overrideRepository.deleteAll(
                overrideRepository.findAll().stream()
                        .filter(o -> o.getUser().getId().equals(userId))
                        .toList()
        );

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
        return user.getRole().name().equals("SUPER_ADMIN")
                || permissionService.hasPermission(user, "SETTINGS",
                com.cricklocal.entity.PermissionAction.VIEW)
                && false;
    }
}
