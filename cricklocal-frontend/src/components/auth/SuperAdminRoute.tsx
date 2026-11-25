import type React from "react";
import { Navigate } from "react-router";
import { useAuth } from "../../context/AuthContext";

export default function SuperAdminRoute({
  children,
}: {
  children: React.ReactNode;
}) {
  const { user, loading } = useAuth();

  if (loading) return null;
  if (!user) return <Navigate to="/signin" replace />;
  if (!user.adminAccess.superAdmin) return <Navigate to="/" replace />;

  return <>{children}</>;
}
