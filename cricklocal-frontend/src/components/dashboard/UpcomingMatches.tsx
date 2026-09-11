import { useEffect, useState } from "react";
import { CalenderIcon } from "../../icons";
import { getMatches } from "../../api/matchesApi";
import type { MatchResponse } from "../../api/types";

function formatSchedule(scheduledAt: string | null) {
  if (!scheduledAt) {
    return "Schedule not specified";
  }

  const date = new Date(scheduledAt);

  if (Number.isNaN(date.getTime())) {
    return "Schedule not specified";
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
  }).format(date);
}

function getTeamNames(match: MatchResponse) {
  const teams = match.teams ?? [];

  if (teams.length >= 2) {
    return `${teams[0].teamName} vs ${teams[1].teamName}`;
  }

  if (teams.length === 1) {
    return `${teams[0].teamName} vs TBD`;
  }

  return "Teams to be announced";
}

export default function UpcomingMatches() {
  const [matches, setMatches] = useState<MatchResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadUpcomingMatches() {
      try {
        setLoading(true);
        setError("");

        const allMatches = await getMatches();

        const upcomingMatches = allMatches
          .filter((match) => match.status === "SCHEDULED")
          .sort((a, b) => {
            const aTime = a.scheduledAt
              ? new Date(a.scheduledAt).getTime()
              : Number.MAX_SAFE_INTEGER;

            const bTime = b.scheduledAt
              ? new Date(b.scheduledAt).getTime()
              : Number.MAX_SAFE_INTEGER;

            return aTime - bTime;
          })
          .slice(0, 5);

        if (!cancelled) {
          setMatches(upcomingMatches);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Unable to load upcoming matches.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadUpcomingMatches();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="mb-5 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-50 text-brand-500 dark:bg-brand-500/15 dark:text-brand-400">
            <CalenderIcon className="size-5" />
          </span>

          <div>
            <h3 className="font-semibold text-gray-800 dark:text-white/90">
              Upcoming Matches
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Scheduled matches
            </p>
          </div>
        </div>

        <button
          type="button"
          className="text-sm font-medium text-brand-500 hover:text-brand-600 dark:text-brand-400"
        >
          View All
        </button>
      </div>

      {loading && (
        <div className="rounded-xl border border-gray-100 bg-gray-50 p-6 text-center text-sm text-gray-500 dark:border-gray-800 dark:bg-white/[0.02] dark:text-gray-400">
          Loading upcoming matches...
        </div>
      )}

      {!loading && error && (
        <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
          {error}
        </div>
      )}

      {!loading && !error && matches.length === 0 && (
        <div className="rounded-xl border border-gray-100 bg-gray-50 p-6 text-center dark:border-gray-800 dark:bg-white/[0.02]">
          <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
            No upcoming matches.
          </p>

          <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
            Scheduled matches will appear here.
          </p>
        </div>
      )}

      {!loading && !error && matches.length > 0 && (
        <div className="space-y-3">
          {matches.map((match) => (
            <div
              key={match.id}
              className="flex flex-col gap-4 rounded-xl border border-gray-100 bg-gray-50 p-4 dark:border-gray-800 dark:bg-white/[0.02] sm:flex-row sm:items-center sm:justify-between"
            >
              <div>
                <h4 className="font-medium text-gray-800 dark:text-white/90">
                  {getTeamNames(match)}
                </h4>

                <div className="mt-1 flex flex-col gap-1 text-xs text-gray-500 dark:text-gray-400 sm:flex-row sm:gap-3">
                  <span>{formatSchedule(match.scheduledAt)}</span>

                  <span className="hidden sm:inline">•</span>

                  <span>
                    {match.venue ?? "Venue not specified"}
                  </span>
                </div>

                <p className="mt-1 text-xs text-gray-400 dark:text-gray-500">
                  {match.name}
                </p>
              </div>

              <button
                type="button"
                className="w-full rounded-lg border border-brand-500 px-4 py-2 text-sm font-medium text-brand-500 hover:bg-brand-50 dark:text-brand-400 dark:hover:bg-brand-500/10 sm:w-auto"
              >
                View Match
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}