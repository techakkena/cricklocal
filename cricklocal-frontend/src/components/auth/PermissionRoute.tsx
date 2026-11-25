import type React from "react";
import { Navigate } from "react-router";
import { usePermission, type PermissionAction } from "../../hooks/usePermission";
import { useAuth } from "../../context/AuthContext";

export default function PermissionRoute({
  moduleKey,
  action = "view",
  children,
}: {
  moduleKey: string;
  action?: PermissionAction;
  children: React.ReactNode;
}) {
  const { user, loading } = useAuth();
  const { hasPermission } = usePermission();

  if (loading) return null;
  if (!user) return <Navigate to="/signin" replace />;
  if (!hasPermission(moduleKey, action)) return <Navigate to="/" replace />;

  return <>{children}</>;
}
