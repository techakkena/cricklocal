package com.cricklocal.dto;

public record PermissionOverrideRequest(
        String moduleKey,
        Boolean view,
        Boolean create,
        Boolean edit,
        Boolean delete,
        Boolean approve
) {}
