import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import PageMeta from "../../components/common/PageMeta";
import AuthLayout from "./AuthPageLayout";
import {
  confirmPlayerRegistration,
  getPlayerRegistrationInvitation,
  type PlayerRegistrationInvitationResponse,
  type PlayerRegistrationUpdateRequest,
} from "../../api/playerRegistrationApi";
import { useAuth } from "../../context/AuthContext";

const REGISTRATION_TOKEN_STORAGE_KEY =
  "cricketlocal_player_registration_token";

const BATTING_STYLES = [
  "RIGHT_HAND",
  "LEFT_HAND",
];

const BOWLING_STYLES = [
  "RIGHT_ARM_FAST",
  "RIGHT_ARM_MEDIUM",
  "RIGHT_ARM_OFF_BREAK",
  "RIGHT_ARM_LEG_SPIN",
  "LEFT_ARM_FAST",
  "LEFT_ARM_MEDIUM",
  "LEFT_ARM_ORTHODOX",
  "LEFT_ARM_CHINAMAN",
];

const PLAYER_ROLES = [
  "BATSMAN",
  "BOWLER",
  "ALL_ROUNDER",
  "WICKET_KEEPER",
];

export default function PlayerRegistrationConfirm() {
  const { token } = useParams<{ token: string }>();
  const navigate = useNavigate();
  const { refreshUser } = useAuth();

  const [invitation, setInvitation] =
    useState<PlayerRegistrationInvitationResponse | null>(null);

  const [form, setForm] =
    useState<PlayerRegistrationUpdateRequest | null>(null);

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
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

        setForm({
          firstName: response.firstName || "",
          lastName: response.lastName || "",
          displayName: response.displayName || "",
          phone: "",
          battingStyle: response.battingStyle || "",
          bowlingStyle: response.bowlingStyle || "",
          role: response.role || "",
        });
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

  const updateField = (
    field: keyof PlayerRegistrationUpdateRequest,
    value: string,
  ) => {
    setForm((current) =>
      current
        ? {
            ...current,
            [field]: value,
          }
        : current,
    );
  };

  const confirmRegistration = async () => {
    if (!token) {
      setError("Registration invitation link is invalid.");
      return;
    }

    if (!form) {
      setError("Registration details are not available.");
      return;
    }

    setSaving(true);
    setError(null);

    try {
      await confirmPlayerRegistration(token, form);

      sessionStorage.removeItem(
        REGISTRATION_TOKEN_STORAGE_KEY,
      );

      await refreshUser();

      navigate("/", { replace: true });
    } catch (requestError: unknown) {
      console.error(
        "Failed to confirm player registration:",
        requestError,
      );

      setError(
        requestError instanceof Error
          ? requestError.message
          : "Unable to complete player registration.",
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <PageMeta
        title="Confirm Player Registration | CrickLocal"
        description="Confirm your CrickLocal player profile"
      />

      <AuthLayout>
        <div className="flex flex-col flex-1">
          <div className="flex flex-col justify-center flex-1 w-full max-w-2xl mx-auto px-6 py-8">
            <div>
              <div className="mb-6">
                <h1 className="mb-2 font-semibold text-gray-800 text-title-sm dark:text-white/90 sm:text-title-md">
                  Confirm Player Registration
                </h1>

                <p className="text-sm text-gray-500 dark:text-gray-400">
                  Review your player details and make any necessary
                  changes before completing registration.
                </p>
              </div>

              {loading && (
                <div className="p-4 text-sm text-gray-500 border border-gray-200 rounded-lg dark:border-gray-800 dark:text-gray-400">
                  Loading player details...
                </div>
              )}

              {!loading && error && !form && (
                <div className="p-4 text-sm text-error-600 border border-error-200 rounded-lg bg-error-50 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-400">
                  {error}
                </div>
              )}

              {!loading && invitation && form && (
                <div className="space-y-6">
                  <div className="p-5 border border-gray-200 rounded-xl dark:border-gray-800">
                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                      <div>
                        <p className="text-xs font-medium tracking-wide text-gray-500 uppercase dark:text-gray-400">
                          Player ID
                        </p>

                        <p className="mt-1 text-lg font-semibold text-gray-800 dark:text-white/90">
                          {invitation.playerId}
                        </p>
                      </div>

                      <div>
                        <p className="text-xs font-medium tracking-wide text-gray-500 uppercase dark:text-gray-400">
                          Registration Status
                        </p>

                        <p className="mt-1 text-sm font-semibold text-gray-800 dark:text-white/90">
                          {invitation.registrationStatus}
                        </p>
                      </div>
                    </div>
                  </div>

                  {error && (
                    <div className="p-4 text-sm text-error-600 border border-error-200 rounded-lg bg-error-50 dark:border-error-500/30 dark:bg-error-500/10 dark:text-error-400">
                      {error}
                    </div>
                  )}

                  <div className="p-5 border border-gray-200 rounded-xl dark:border-gray-800">
                    <h2 className="mb-5 text-base font-semibold text-gray-800 dark:text-white/90">
                      Player Details
                    </h2>

                    <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
                      <div>
                        <label className="block mb-1.5 text-sm font-medium text-gray-700 dark:text-gray-300">
                          First Name
                        </label>

                        <input
                          type="text"
                          value={form.firstName}
                          onChange={(event) =>
                            updateField(
                              "firstName",
                              event.target.value,
                            )
                          }
                          disabled={saving}
                          className="w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 outline-none transition focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                        />
                      </div>

                      <div>
                        <label className="block mb-1.5 text-sm font-medium text-gray-700 dark:text-gray-300">
                          Last Name
                        </label>

                        <input
                          type="text"
                          value={form.lastName}
                          onChange={(event) =>
                            updateField(
                              "lastName",
                              event.target.value,
                            )
                          }
                          disabled={saving}
                          className="w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 outline-none transition focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                        />
                      </div>

                      <div className="sm:col-span-2">
                        <label className="block mb-1.5 text-sm font-medium text-gray-700 dark:text-gray-300">
                          Display Name
                        </label>

                        <input
                          type="text"
                          value={form.displayName}
                          onChange={(event) =>
                            updateField(
                              "displayName",
                              event.target.value,
                            )
                          }
                          disabled={saving}
                          className="w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 outline-none transition focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                        />
                      </div>

                      <div>
                        <label className="block mb-1.5 text-sm font-medium text-gray-700 dark:text-gray-300">
                          Phone
                        </label>

                        <input
                          type="text"
                          value={form.phone}
                          onChange={(event) =>
                            updateField(
                              "phone",
                              event.target.value,
                            )
                          }
                          disabled={saving}
                          placeholder="Enter phone number"
                          className="w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 outline-none transition focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                        />
                      </div>

                      <div>
                        <label className="block mb-1.5 text-sm font-medium text-gray-700 dark:text-gray-300">
                          Batting Style
                        </label>

                        <select
                          value={form.battingStyle}
                          onChange={(event) =>
                            updateField(
                              "battingStyle",
                              event.target.value,
                            )
                          }
                          disabled={saving}
                          className="w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 outline-none transition focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                        >
                          <option value="">
                            Select batting style
                          </option>

                          {BATTING_STYLES.map((style) => (
                            <option key={style} value={style}>
                              {style}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div>
                        <label className="block mb-1.5 text-sm font-medium text-gray-700 dark:text-gray-300">
                          Bowling Style
                        </label>

                        <select
                          value={form.bowlingStyle}
                          onChange={(event) =>
                            updateField(
                              "bowlingStyle",
                              event.target.value,
                            )
                          }
                          disabled={saving}
                          className="w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 outline-none transition focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                        >
                          <option value="">
                            Select bowling style
                          </option>

                          {BOWLING_STYLES.map((style) => (
                            <option key={style} value={style}>
                              {style}
                            </option>
                          ))}
                        </select>
                      </div>

                      <div>
                        <label className="block mb-1.5 text-sm font-medium text-gray-700 dark:text-gray-300">
                          Player Role
                        </label>

                        <select
                          value={form.role}
                          onChange={(event) =>
                            updateField(
                              "role",
                              event.target.value,
                            )
                          }
                          disabled={saving}
                          className="w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 outline-none transition focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                        >
                          <option value="">
                            Select player role
                          </option>

                          {PLAYER_ROLES.map((role) => (
                            <option key={role} value={role}>
                              {role}
                            </option>
                          ))}
                        </select>
                      </div>
                    </div>
                  </div>

                  <div className="p-4 text-sm text-gray-600 border border-brand-200 rounded-lg bg-brand-50 dark:border-brand-500/20 dark:bg-brand-500/10 dark:text-gray-300">
                    Your Player ID is permanent. You can update your
                    profile details above, but your Player ID and
                    registration identity remain unchanged.
                  </div>

                  <button
                    type="button"
                    onClick={confirmRegistration}
                    disabled={saving}
                    className="inline-flex items-center justify-center w-full gap-3 px-5 py-3 text-sm font-medium text-white transition rounded-lg bg-brand-500 hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
                  >
                    {saving
                      ? "Completing Registration..."
                      : "Confirm & Complete Registration"}
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