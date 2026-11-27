import {
  PermissionModuleResponse,
  PermissionOverrideRequest,
  UserPermissionResponse,
} from "../api/types";

import {
  apiGet,
  apiPut,
  apiDelete,
} from "./apiClient";

export async function getPermissionModules(): Promise<
  PermissionModuleResponse[]
> {
  return apiGet<PermissionModuleResponse[]>(
    "/api/admin/permissions/modules"
  );
}

export async function getPermissionUsers(): Promise<
  UserPermissionResponse[]
> {
  return apiGet<UserPermissionResponse[]>(
    "/api/admin/permissions/users"
  );
}

export async function getUserPermissions(
  userId: number
): Promise<UserPermissionResponse> {
  return apiGet<UserPermissionResponse>(
    `/api/admin/permissions/users/${userId}`
  );
}

export async function updateUserPermission(
  userId: number,
  request: PermissionOverrideRequest
): Promise<UserPermissionResponse> {
  return apiPut<UserPermissionResponse>(
    `/api/admin/permissions/users/${userId}`,
    request
  );
}

export async function resetUserPermissions(
  userId: number
): Promise<void> {
  await apiDelete(
    `/api/admin/permissions/users/${userId}/reset`
  );
}