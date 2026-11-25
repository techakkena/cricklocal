package com.cricklocal.controller;

import com.cricklocal.entity.User;
import com.cricklocal.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @PreAuthorize("@superAdminAuthorizationService.isSuperAdmin(authentication)")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(adminUserService.getUsers());
    }

    @PostMapping("/{userId}/block")
    @PreAuthorize("@superAdminAuthorizationService.isSuperAdmin(authentication)")
    public ResponseEntity<User> blockUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                adminUserService.blockUser(userId)
        );
    }

    @PostMapping("/{userId}/activate")
    @PreAuthorize("@superAdminAuthorizationService.isSuperAdmin(authentication)")
    public ResponseEntity<User> activateUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                adminUserService.activateUser(userId)
        );
    }

    @PostMapping("/{userId}/approve-admin")
    @PreAuthorize("@superAdminAuthorizationService.isSuperAdmin(authentication)")
    public ResponseEntity<User> approveAdmin(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                adminUserService.approveAdmin(userId)
        );
    }
}