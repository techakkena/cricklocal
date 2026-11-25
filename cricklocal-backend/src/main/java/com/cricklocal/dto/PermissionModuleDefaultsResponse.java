package com.cricklocal.dto;

public record PermissionModuleDefaultsResponse(
        String moduleKey,
        String moduleName,
        PermissionSet player,
        PermissionSet admin
) {
    public record PermissionSet(
            boolean view,
            boolean create,
            boolean edit,
            boolean delete,
            boolean approve
    ) {}
}
