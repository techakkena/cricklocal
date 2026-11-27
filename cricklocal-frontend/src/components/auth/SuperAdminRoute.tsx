import { Navigate } from "react-router";
import { useAuth } from "../../context/AuthContext";

interface SuperAdminRouteProps {
  children: React.ReactNode;
}

export default function SuperAdminRoute({
  children,
}: SuperAdminRouteProps) {
  const { user, loading } = useAuth();
 
  if (loading) {
    return null;
  }

  if (!user) {
    return <Navigate to="/signin" replace />;
  }

  if (user.role !== "SUPER_ADMIN") {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}