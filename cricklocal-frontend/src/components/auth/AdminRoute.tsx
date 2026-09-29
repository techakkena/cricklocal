import type React from "react";
import { Navigate } from "react-router";
import { useAuth } from "../../context/AuthContext";

interface AdminRouteProps {
  children: React.ReactNode;
}

export default function AdminRoute({
  children,
}: AdminRouteProps) {
  const { user, loading } = useAuth();

  if (loading) {
    return null;
  }

  if (!user) {
    return <Navigate to="/signin" replace />;
  }

  if (!user.adminAccess.active) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}