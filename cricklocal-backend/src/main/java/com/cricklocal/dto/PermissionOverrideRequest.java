package com.cricklocal.dto;

import com.cricklocal.entity.PermissionAction;

public record PermissionOverrideRequest(
        String moduleKey,
        PermissionAction action,
        Boolean allowed
) {
}