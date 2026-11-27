package com.cricklocal.service;

import com.cricklocal.dto.PermissionItemResponse;
import com.cricklocal.dto.UserPermissionResponse;
import com.cricklocal.entity.PermissionAction;
import com.cricklocal.entity.PermissionModule;
import com.cricklocal.entity.RolePermission;
import com.cricklocal.entity.User;
import com.cricklocal.entity.UserAccountStatus;
import com.cricklocal.entity.UserPermissionOverride;
import com.cricklocal.entity.UserRole;
import com.cricklocal.repository.PermissionModuleRepository;
import com.cricklocal.repository.RolePermissionRepository;
import com.cricklocal.repository.UserPermissionOverrideRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionModuleRepository permissionModuleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserPermissionOverrideRepository userPermissionOverrideRepository;

    public PermissionService(
            PermissionModuleRepository permissionModuleRepository,
            RolePermissionRepository rolePermissionRepository,
            UserPermissionOverrideRepository userPermissionOverrideRepository) {

        this.permissionModuleRepository = permissionModuleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userPermissionOverrideRepository =
                userPermissionOverrideRepository;
    }

    /**
     * Check whether a user has a specific permission.
     *
     * Effective permission:
     *
     * SUPER_ADMIN
     *      -> always allowed
     *
     * ADMIN / PLAYER
     *      -> user override if present
     *      -> otherwise role default
     */
    public boolean hasPermission(
            User user,
            String moduleKey,
            PermissionAction action) {

        if (user == null) {
            return false;
        }

        if (user.getAccountStatus() != UserAccountStatus.ACTIVE) {
            return false;
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            return false;
        }

        if (moduleKey == null || moduleKey.isBlank()) {
            return false;
        }

        if (action == null) {
            return false;
        }

        /*
         * SUPER_ADMIN bypasses normal permission configuration.
         */
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return true;
        }

        PermissionModule module =
                permissionModuleRepository
                        .findByModuleKey(moduleKey)
                        .orElse(null);

        if (module == null
                || !Boolean.TRUE.equals(module.getActive())) {

            return false;
        }

        /*
         * First check user-specific override.
         */
        UserPermissionOverride override =
                userPermissionOverrideRepository
                        .findByUser_IdAndModule_ModuleKeyAndAction(
                                user.getId(),
                                moduleKey,
                                action
                        )
                        .orElse(null);

        if (override != null
                && override.getAllowed() != null) {

            return Boolean.TRUE.equals(
                    override.getAllowed()
            );
        }

        /*
         * No override -> use role default.
         */
        RolePermission rolePermission =
                rolePermissionRepository
                        .findByRoleAndModule_ModuleKeyAndAction(
                                user.getRole(),
                                moduleKey,
                                action
                        )
                        .orElse(null);

        return rolePermission != null
                && Boolean.TRUE.equals(
                        rolePermission.getAllowed()
                );
    }

    /**
     * Return all active permission modules.
     *
     * Used by Super Admin permission management APIs.
     */
    public List<PermissionModule> getActiveModules() {

        return permissionModuleRepository
                .findByActiveTrueOrderByIdAsc();
    }

    /**
     * Get the complete effective permission matrix for one user.
     *
     * For every active module and every action:
     *
     * roleDefault = permission configured for user's role
     *
     * override = user-specific override
     *
     * effective =
     *      override if explicitly configured
     *      otherwise roleDefault
     */
    public UserPermissionResponse getUserPermissions(
            User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException(
                    "Super Admin permissions cannot be modified"
            );
        }

        /*
         * Load active modules once.
         */
        List<PermissionModule> modules =
                permissionModuleRepository
                        .findByActiveTrueOrderByIdAsc();

        /*
         * Load all role permissions once.
         * This avoids querying the database for every
         * module/action combination.
         */
        List<RolePermission> rolePermissions =
                rolePermissionRepository
                        .findByRole(user.getRole());

        Map<String, Boolean> roleDefaults =
                new HashMap<>();

        for (RolePermission rolePermission
                : rolePermissions) {

            String key =
                    buildPermissionKey(
                            rolePermission.getModule()
                                    .getModuleKey(),
                            rolePermission.getAction()
                    );

            roleDefaults.put(
                    key,
                    rolePermission.getAllowed()
            );
        }

        /*
         * Load all user overrides once.
         */
        List<UserPermissionOverride> overrides =
                userPermissionOverrideRepository
                        .findByUser_Id(user.getId());

        Map<String, Boolean> userOverrides =
                new HashMap<>();

        for (UserPermissionOverride override
                : overrides) {

            /*
             * A null override means role default.
             * Normally reset deletes the override row,
             * but we still safely ignore null values.
             */
            if (override.getAllowed() == null) {
                continue;
            }

            String key =
                    buildPermissionKey(
                            override.getModule()
                                    .getModuleKey(),
                            override.getAction()
                    );

            userOverrides.put(
                    key,
                    override.getAllowed()
            );
        }

        /*
         * Build complete permission matrix.
         */
        List<PermissionItemResponse> permissions =
                modules.stream()
                        .flatMap(module ->
                                java.util.Arrays
                                        .stream(
                                                PermissionAction
                                                        .values()
                                        )
                                        .map(action -> {

                                            String key =
                                                    buildPermissionKey(
                                                            module.getModuleKey(),
                                                            action
                                                    );

                                            Boolean roleDefault =
                                                    roleDefaults.get(key);

                                            Boolean override =
                                                    userOverrides.get(key);

                                            Boolean effective;

                                            if (override != null) {
                                                effective =
                                                        override;
                                            } else {
                                                effective =
                                                        Boolean.TRUE
                                                                .equals(
                                                                        roleDefault
                                                                );
                                            }

                                            return new PermissionItemResponse(
                                                    module.getModuleKey(),
                                                    module.getModuleName(),
                                                    action,
                                                    roleDefault,
                                                    override,
                                                    effective
                                            );
                                        })
                        )
                        .toList();

        return new UserPermissionResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                permissions
        );
    }

    /**
     * Create, update, or reset one user permission.
     *
     * allowed = true
     *      -> explicitly allow
     *
     * allowed = false
     *      -> explicitly deny
     *
     * allowed = null
     *      -> reset to role default
     */
    @Transactional
    public void updateUserPermission(
            User user,
            String moduleKey,
            PermissionAction action,
            Boolean allowed) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException(
                    "Super Admin permissions cannot be modified"
            );
        }

        if (moduleKey == null || moduleKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Module key is required"
            );
        }

        if (action == null) {
            throw new IllegalArgumentException(
                    "Permission action is required"
            );
        }

        PermissionModule module =
                permissionModuleRepository
                        .findByModuleKey(moduleKey)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Permission module not found: "
                                                + moduleKey
                                )
                        );

        if (!Boolean.TRUE.equals(module.getActive())) {
            throw new IllegalArgumentException(
                    "Permission module is inactive: "
                            + moduleKey
            );
        }

        UserPermissionOverride override =
                userPermissionOverrideRepository
                        .findByUser_IdAndModule_ModuleKeyAndAction(
                                user.getId(),
                                moduleKey,
                                action
                        )
                        .orElse(null);

        /*
         * null means reset this permission
         * back to role default.
         */
        if (allowed == null) {

            if (override != null) {
                userPermissionOverrideRepository.delete(
                        override
                );
            }

            return;
        }

        /*
         * Create a new override if one doesn't exist.
         */
        if (override == null) {

            override =
                    new UserPermissionOverride();

            override.setUser(user);
            override.setModule(module);
            override.setAction(action);
        }

        /*
         * Explicitly allow or deny.
         */
        override.setAllowed(allowed);

        userPermissionOverrideRepository.save(
                override
        );
    }

    /**
     * Reset ALL permission overrides for a user.
     *
     * After reset, the user will receive only the
     * default permissions defined for their role.
     */
    @Transactional
    public void resetUserPermissions(
            User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "User cannot be null"
            );
        }

        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new IllegalArgumentException(
                    "Super Admin permissions cannot be reset"
            );
        }

        userPermissionOverrideRepository
                .deleteByUser_Id(user.getId());
    }

    /**
     * Internal permission key.
     *
     * Example:
     * TEAMS:VIEW
     * TEAMS:CREATE
     * MATCHES:EDIT
     */
    private String buildPermissionKey(
            String moduleKey,
            PermissionAction action) {

        return moduleKey + ":" + action.name();
    }
}