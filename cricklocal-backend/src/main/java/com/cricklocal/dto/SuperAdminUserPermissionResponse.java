package com.cricklocal.dto;

import com.cricklocal.entity.UserRole;

public record SuperAdminUserPermissionResponse(
        Long userId,
        String email,
        String displayName,
        UserRole role,
        boolean active
) {}
