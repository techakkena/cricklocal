import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import PageMeta from "../../components/common/PageMeta";
import AuthLayout from "./AuthPageLayout";
import { apiGet } from "../../api/apiClient";
import { useAuth } from "../../context/AuthContext";

interface AuthUser {
  id: number;
  email: string;
  displayName: string;
  role: string;
  active: boolean;
}

const REGISTRATION_TOKEN_STORAGE_KEY =
  "cricketlocal_player_registration_token";

export default function LoginSuccess() {
  const navigate = useNavigate();
  const { refreshUser } = useAuth();

  const [message, setMessage] = useState(
    "Completing Google sign-in...",
  );

  useEffect(() => {
    async function loadUser() {
      try {
        const user = await apiGet<AuthUser>("/api/auth/me");

        console.log(
          "CricketLocal authenticated user:",
          user,
        );

        if (!user.active) {
          setMessage(
            "Your CricketLocal account is inactive.",
          );
          return;
        }

        const registrationToken = sessionStorage.getItem(
          REGISTRATION_TOKEN_STORAGE_KEY,
        );

        if (registrationToken) {
          setMessage(
            "Google sign-in successful. Opening player confirmation...",
          );

          await refreshUser();

          navigate(
            `/player-registration/confirm/${encodeURIComponent(
              registrationToken,
            )}`,
            { replace: true },
          );

          return;
        }

        navigate("/", { replace: true });
      } catch (error) {
        console.error(
          "Failed to load authenticated user:",
          error,
        );

        setMessage(
          "Google sign-in completed, but CricketLocal could not load your account.",
        );
      }
    }

    loadUser();
  }, [navigate, refreshUser]);

  return (
    <>
      <PageMeta
        title="Sign-in Complete | CrickLocal"
        description="Completing your CrickLocal sign-in"
      />

      <AuthLayout>
        <div className="flex flex-col flex-1">
          <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto px-6">
            <div className="p-6 text-center border border-gray-200 rounded-xl dark:border-gray-800">
              <h1 className="mb-3 text-xl font-semibold text-gray-800 dark:text-white/90">
                CricketLocal
              </h1>

              <p className="text-sm text-gray-500 dark:text-gray-400">
                {message}
              </p>
            </div>
          </div>
        </div>
      </AuthLayout>
    </>
  );
}