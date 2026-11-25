package com.cricklocal.dto;

public record EffectivePermissionResponse(
        String moduleKey,
        String moduleName,
        boolean view,
        boolean create,
        boolean edit,
        boolean delete,
        boolean approve
) {}
