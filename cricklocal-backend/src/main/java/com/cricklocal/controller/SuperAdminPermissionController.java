package com.cricklocal.controller;

import com.cricklocal.dto.PermissionModuleDefaultsResponse;
import com.cricklocal.dto.PermissionOverrideRequest;
import com.cricklocal.dto.SuperAdminUserPermissionResponse;
import com.cricklocal.dto.UserPermissionManagementResponse;
import com.cricklocal.service.SuperAdminPermissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/super-admin/permissions")
@PreAuthorize("@superAdminAuthorizationService.isSuperAdmin(authentication)")
public class SuperAdminPermissionController {

    private final SuperAdminPermissionService service;

    public SuperAdminPermissionController(SuperAdminPermissionService service) {
        this.service = service;
    }

    @GetMapping("/modules")
    public List<PermissionModuleDefaultsResponse> getModules() {
        return service.getAllModules();
    }

    @GetMapping("/users")
    public List<SuperAdminUserPermissionResponse> getUsers() {
        return service.getUsers();
    }

    @GetMapping("/users/{userId}")
    public UserPermissionManagementResponse getUserPermissions(
            @PathVariable Long userId) {
        return service.getUserPermissions(userId);
    }

    @PutMapping("/users/{userId}")
    public UserPermissionManagementResponse updateUserPermissions(
            @PathVariable Long userId,
            @RequestBody List<PermissionOverrideRequest> requests) {
        return service.updateUserPermissions(userId, requests);
    }

    @PostMapping("/users/{userId}/reset")
    public ResponseEntity<UserPermissionManagementResponse> resetUserPermissions(
            @PathVariable Long userId) {
        return ResponseEntity.ok(service.resetUserPermissions(userId));
    }
}
