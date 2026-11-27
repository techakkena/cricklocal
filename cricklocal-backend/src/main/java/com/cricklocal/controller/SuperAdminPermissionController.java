package com.cricklocal.controller;

import com.cricklocal.dto.PermissionModuleResponse;
import com.cricklocal.dto.PermissionOverrideRequest;
import com.cricklocal.dto.UserPermissionResponse;
import com.cricklocal.entity.PermissionAction;
import com.cricklocal.entity.PermissionModule;
import com.cricklocal.entity.User;
import com.cricklocal.entity.UserRole;
import com.cricklocal.repository.UserRepository;
import com.cricklocal.service.PermissionAuthorizationService;
import com.cricklocal.service.PermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/admin/permissions")
public class SuperAdminPermissionController {

    private final PermissionService permissionService;
    private final PermissionAuthorizationService
            permissionAuthorizationService;
    private final UserRepository userRepository;

    public SuperAdminPermissionController(
            PermissionService permissionService,
            PermissionAuthorizationService
                    permissionAuthorizationService,
            UserRepository userRepository) {

        this.permissionService = permissionService;
        this.permissionAuthorizationService =
                permissionAuthorizationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/modules")
    @PreAuthorize(
            "@permissionAuthorizationService.isSuperAdmin(authentication)"
    )
    public List<PermissionModuleResponse> getModules() {

        return permissionService
                .getActiveModules()
                .stream()
                .map(module ->
                        new PermissionModuleResponse(
                                module.getId(),
                                module.getModuleKey(),
                                module.getModuleName(),
                                Arrays.asList(
                                        PermissionAction.values()
                                )
                        )
                )
                .toList();
    }

    @GetMapping("/users")
    @PreAuthorize(
            "@permissionAuthorizationService.isSuperAdmin(authentication)"
    )
    public List<UserPermissionResponse> getUsers() {

        return userRepository
                .findAll()
                .stream()
                .filter(user ->
                        user.getRole() == UserRole.ADMIN
                                || user.getRole() == UserRole.PLAYER
                )
                .map(permissionService::getUserPermissions)
                .toList();
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize(
            "@permissionAuthorizationService.isSuperAdmin(authentication)"
    )
    public UserPermissionResponse getUserPermissions(
            @PathVariable Long userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        return permissionService.getUserPermissions(user);
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize(
            "@permissionAuthorizationService.isSuperAdmin(authentication)"
    )
    public UserPermissionResponse updateUserPermission(
            @PathVariable Long userId,
            @RequestBody PermissionOverrideRequest request) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        permissionService.updateUserPermission(
                user,
                request.moduleKey(),
                request.action(),
                request.allowed()
        );

        return permissionService.getUserPermissions(user);
    }

    @DeleteMapping("/users/{userId}/reset")
    @PreAuthorize(
            "@permissionAuthorizationService.isSuperAdmin(authentication)"
    )
    public ResponseEntity<Void> resetUserPermissions(
            @PathVariable Long userId) {

        User user = userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "User not found: " + userId
                        )
                );

        permissionService.resetUserPermissions(user);

        return ResponseEntity.noContent().build();
    }
}