package com.cricklocal.dto;

import com.cricklocal.entity.PermissionAction;

public record PermissionItemResponse(
        String moduleKey,
        String moduleName,
        PermissionAction action,
        Boolean roleDefault,
        Boolean override,
        Boolean effective
) {
}