import { useEffect, useState } from "react";
import { useParams } from "react-router";
import PageMeta from "../../components/common/PageMeta";
import AuthLayout from "./AuthPageLayout";
import {
  getPlayerRegistrationInvitation,
  type PlayerRegistrationInvitationResponse,
} from "../../api/playerRegistrationApi";

const REGISTRATION_TOKEN_STORAGE_KEY =
  "cricketlocal_player_registration_token";

export default function PlayerRegistration() {
  const { token } = useParams<{ token: string }>();

  const [invitation, setInvitation] =
    useState<PlayerRegistrationInvitationResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!token) {
      setError("Registration invitation link is invalid.");
      setLoading(false);
      return;
    }

    sessionStorage.setItem(
      REGISTRATION_TOKEN_STORAGE_KEY,
      token,
    );

    getPlayerRegistrationInvitation(token)
      .then((response) => {
        setInvitation(response);
      })
      .catch((requestError: unknown) => {
        setError(
          requestError instanceof Error
            ? requestError.message
            : "Unable to load the registration invitation.",
        );
      })
      .finally(() => {
        setLoading(false);
      });
  }, [token]);

  const continueWithGoogle = () => {
    if (!token) {
      setError("Registration invitation link is invalid.");
      return;
    }

    sessionStorage.setItem(
      REGISTRATION_TOKEN_STORAGE_KEY,
      token,
    );

    window.location.href =
      `${import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080"}/oauth2/authorization/google`
  };

  return (
    <>
      <PageMeta
        title="Player Registration | CrickLocal"
        description="Complete your CrickLocal player registration"
      />

      <AuthLayout>
        <div className="flex flex-col flex-1">
          <div className="flex flex-col justify-center flex-1 w-full max-w-md mx-auto px-6">
            <div>
              <div className="mb-6">
                <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
                  Player Registration
                </h1>

                <p className="text-sm text-gray-500 dark:text-gray-400">
                  Complete your CrickLocal player registration.
                </p>
              </div>

              {loading && (
                <div className="p-4 text-sm text-gray-500 border border-gray-200 rounded-lg dark:border-gray-800 dark:text-gray-400">
                  Loading registration invitation...
                </div>
              )}

              {!loading && error && (
                <div className="p-4 text-sm text-error-600 border border-error-200 rounded-lg bg-error-50 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-400">
                  {error}
                </div>
              )}

              {!loading && !error && invitation && (
                <div className="space-y-5">
                  <div className="p-5 border border-gray-200 rounded-xl dark:border-gray-800">
                    <p className="mb-1 text-xs font-medium tracking-wide text-gray-500 uppercase dark:text-gray-400">
                      Player
                    </p>

                    <h2 className="text-xl font-semibold text-gray-800 dark:text-white/90">
                      {invitation.displayName}
                    </h2>
                  </div>

                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400">
                        First Name
                      </p>
                      <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                        {invitation.firstName}
                      </p>
                    </div>

                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400">
                        Last Name
                      </p>
                      <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                        {invitation.lastName || "—"}
                      </p>
                    </div>

                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400">
                        Batting Style
                      </p>
                      <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                        {invitation.battingStyle}
                      </p>
                    </div>

                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400">
                        Bowling Style
                      </p>
                      <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                        {invitation.bowlingStyle}
                      </p>
                    </div>

                    <div>
                      <p className="text-xs text-gray-500 dark:text-gray-400">
                        Role
                      </p>
                      <p className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
                        {invitation.role}
                      </p>
                    </div>
                  </div>

                  <div className="p-4 text-sm text-gray-600 border border-brand-200 rounded-lg bg-brand-50 dark:border-brand-500/20 dark:bg-brand-500/10 dark:text-gray-300">
                    This player profile was created by a CricketLocal
                    administrator. Continue with Google to connect this
                    profile to your CricketLocal account.
                  </div>

                  <button
                    type="button"
                    onClick={continueWithGoogle}
                    className="inline-flex items-center justify-center w-full gap-3 px-5 py-3 text-sm font-medium text-white transition rounded-lg bg-brand-500 hover:bg-brand-600"
                  >
                    Continue with Google
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </AuthLayout>
    </>
  );
}