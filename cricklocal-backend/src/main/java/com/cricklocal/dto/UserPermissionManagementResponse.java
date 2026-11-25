package com.cricklocal.dto;

import java.util.List;

public record UserPermissionManagementResponse(
        SuperAdminUserPermissionResponse user,
        List<EffectivePermissionResponse> effectivePermissions
) {}
