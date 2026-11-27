package com.cricklocal.dto;

import com.cricklocal.entity.PermissionAction;

import java.util.List;

public record PermissionModuleResponse(
        Long id,
        String moduleKey,
        String moduleName,
        List<PermissionAction> actions
) {
}