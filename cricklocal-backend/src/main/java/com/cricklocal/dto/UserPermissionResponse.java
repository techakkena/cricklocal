package com.cricklocal.dto;

import com.cricklocal.entity.UserRole;

import java.util.List;

public record UserPermissionResponse(
        Long userId,
        String email,
        String displayName,
        UserRole role,
        List<PermissionItemResponse> permissions
) {
}