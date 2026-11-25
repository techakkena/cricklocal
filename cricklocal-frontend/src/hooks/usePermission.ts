import { useAuth } from "../context/AuthContext";

export type PermissionAction =
  | "view"
  | "create"
  | "edit"
  | "delete"
  | "approve";

export function usePermission() {
  const { user } = useAuth();

  const hasPermission = (
    moduleKey: string,
    action: PermissionAction,
  ): boolean => {
    if (!user) return false;
    if (user.adminAccess.superAdmin) return true;

    const permission = user.permissions.find(
      (item) => item.moduleKey === moduleKey,
    );

    return permission ? Boolean(permission[action]) : false;
  };

  return { hasPermission };
}
