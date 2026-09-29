import type React from "react";
import {
  createContext,
  useContext,
  useEffect,
  useState,
} from "react";
import { apiGet } from "../api/apiClient";

export interface PlayerProfile {
  exists: boolean;
  playerId: number | null;
  registrationStatus: string | null;
}

export interface AdminAccess {
  active: boolean;
}

export interface CurrentUser {
  id: number;
  email: string;
  displayName: string;
  role: string;
  active: boolean;
  playerProfile: PlayerProfile;
  adminAccess: AdminAccess;
}

interface AuthContextType {
  user: CurrentUser | null;
  loading: boolean;
  refreshUser: () => Promise<void>;
  clearUser: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);

  const loadUser = async () => {
    try {
      const currentUser =
        await apiGet<CurrentUser>("/api/auth/me");

      setUser(currentUser);
    } catch (error) {
      console.error("Failed to load CricketLocal user:", error);
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUser();
  }, []);

  const refreshUser = async () => {
    setLoading(true);
    await loadUser();
  };

  const clearUser = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        loading,
        refreshUser,
        clearUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
};