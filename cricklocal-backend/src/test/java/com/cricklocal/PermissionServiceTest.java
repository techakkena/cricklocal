package com.cricklocal;

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
import com.cricklocal.service.PermissionService;
import com.cricklocal.service.SuperAdminAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    @Mock
    private PermissionModuleRepository moduleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private UserPermissionOverrideRepository overrideRepository;

    @Mock
    private SuperAdminAuthorizationService superAdminAuthorizationService;

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService(
                moduleRepository,
                rolePermissionRepository,
                overrideRepository,
                superAdminAuthorizationService
        );
    }

    @Test
    void roleDefaultsAreUsedWhenNoUserOverrideExists() {
        User user = activeUser(UserRole.PLAYER);
        PermissionModule module = module("TEAMS", "Teams", 1L);

        RolePermission defaults = rolePermission(
                user.getRole(), module,
                true, false, false, false, false
        );

        when(superAdminAuthorizationService.isSuperAdmin(user)).thenReturn(false);
        when(moduleRepository.findAll()).thenReturn(List.of(module));
        when(rolePermissionRepository.findAllByRole(UserRole.PLAYER))
                .thenReturn(List.of(defaults));
        when(overrideRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of());

        assertTrue(permissionService.hasPermission(user, "TEAMS", PermissionAction.VIEW));
        assertFalse(permissionService.hasPermission(user, "TEAMS", PermissionAction.CREATE));
    }

    @Test
    void explicitUserOverrideWinsOverRoleDefault() {
        User user = activeUser(UserRole.PLAYER);
        PermissionModule module = module("TEAMS", "Teams", 1L);

        RolePermission defaults = rolePermission(
                user.getRole(), module,
                true, false, false, false, false
        );

        UserPermissionOverride override = new UserPermissionOverride();
        override.setUser(user);
        override.setModule(module);
        override.setCanView(false);
        override.setCanCreate(true);

        when(superAdminAuthorizationService.isSuperAdmin(user)).thenReturn(false);
        when(moduleRepository.findAll()).thenReturn(List.of(module));
        when(rolePermissionRepository.findAllByRole(UserRole.PLAYER))
                .thenReturn(List.of(defaults));
        when(overrideRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of(override));

        assertFalse(permissionService.hasPermission(user, "TEAMS", PermissionAction.VIEW));
        assertTrue(permissionService.hasPermission(user, "TEAMS", PermissionAction.CREATE));
    }

    @Test
    void inactiveUserCannotUsePermissions() {
        User user = activeUser(UserRole.ADMIN);
        user.setActive(false);

        when(superAdminAuthorizationService.isSuperAdmin(user)).thenReturn(false);

        assertFalse(permissionService.hasPermission(
                user, "TEAMS", PermissionAction.VIEW
        ));
    }

    @Test
    void superAdminGetsAllActions() {
        User user = activeUser(UserRole.SUPER_ADMIN);

        when(superAdminAuthorizationService.isSuperAdmin(user)).thenReturn(true);

        for (PermissionAction action : PermissionAction.values()) {
            assertTrue(permissionService.hasPermission(
                    user, "ANY_MODULE", action
            ));
        }
    }

    @Test
    void unknownModuleIsDenied() {
        User user = activeUser(UserRole.PLAYER);

        when(superAdminAuthorizationService.isSuperAdmin(user)).thenReturn(false);
        when(moduleRepository.findAll()).thenReturn(List.of());
        when(rolePermissionRepository.findAllByRole(UserRole.PLAYER))
                .thenReturn(List.of());
        when(overrideRepository.findAllByUserId(user.getId()))
                .thenReturn(List.of());

        assertFalse(permissionService.hasPermission(
                user, "UNKNOWN", PermissionAction.VIEW
        ));
    }

    private User activeUser(UserRole role) {
        User user = new User();
        user.setRole(role);
        user.setActive(true);
        user.setAccountStatus(UserAccountStatus.ACTIVE);

        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, 100L);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to prepare test user", exception);
        }

        return user;
    }

    private PermissionModule module(String key, String name, Long id) {
        PermissionModule module = new PermissionModule();
        module.setModuleKey(key);
        module.setModuleName(name);

        try {
            var field = PermissionModule.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(module, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to prepare test module", exception);
        }

        return module;
    }

    private RolePermission rolePermission(
            UserRole role,
            PermissionModule module,
            boolean view,
            boolean create,
            boolean edit,
            boolean delete,
            boolean approve) {
        RolePermission permission = new RolePermission();
        permission.setRole(role);
        permission.setModule(module);
        permission.setCanView(view);
        permission.setCanCreate(create);
        permission.setCanEdit(edit);
        permission.setCanDelete(delete);
        permission.setCanApprove(approve);
        return permission;
    }
}
