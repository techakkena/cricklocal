import { FormEvent, useState } from "react";
import { useNavigate, useParams } from "react-router";
import {
  validateScoreOperatorAccess,
} from "../api/matchesApi";

export default function ScoreOperatorAccess() {
  const { accessToken } = useParams<{ accessToken: string }>();
  const navigate = useNavigate();

  const [securityCode, setSecurityCode] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!accessToken) {
      setError("Invalid operator access link.");
      return;
    }

    if (!securityCode.trim()) {
      setError("Security code is required.");
      return;
    }

    try {
      setLoading(true);
      setError("");

      const response = await validateScoreOperatorAccess({
        accessToken,
        securityCode: securityCode.trim(),
      });

      sessionStorage.setItem(
        "cricklocal_score_operator_session",
        response.sessionToken,
      );

      sessionStorage.setItem(
        "cricklocal_score_operator_match_id",
        String(response.matchId),
      );

      sessionStorage.setItem(
        "cricklocal_score_operator_expires_at",
        response.expiresAt,
      );

      navigate(`/score/match/${response.matchId}`);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to validate operator access.",
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 px-4 py-10 dark:bg-gray-900">
      <div className="mx-auto flex min-h-[70vh] max-w-md items-center justify-center">
        <div className="w-full rounded-2xl border border-gray-200 bg-white p-6 shadow-sm dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-gray-800 dark:text-white/90">
              Score Operator Access
            </h1>

            <p className="mt-2 text-sm text-gray-500 dark:text-gray-400">
              Enter the security code provided by the match administrator.
            </p>
          </div>

          {error && (
            <div className="mb-4 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-900/40 dark:bg-error-900/10 dark:text-error-400">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <label
              htmlFor="securityCode"
              className="mb-2 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              Security Code
            </label>

            <input
              id="securityCode"
              type="text"
              inputMode="numeric"
              autoComplete="one-time-code"
              maxLength={6}
              value={securityCode}
              onChange={(event) =>
                setSecurityCode(
                  event.target.value.replace(/\D/g, "").slice(0, 6),
                )
              }
              placeholder="Enter 6-digit code"
              className="h-12 w-full rounded-lg border border-gray-300 bg-white px-4 text-center font-mono text-xl tracking-[0.3em] text-gray-800 outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white"
            />

            <button
              type="submit"
              disabled={loading || securityCode.length !== 6}
              className="mt-5 w-full rounded-lg bg-brand-500 px-5 py-3.5 text-sm font-medium text-white transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {loading ? "Verifying..." : "Enter Live Score"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}