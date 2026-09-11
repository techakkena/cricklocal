import { useEffect, useState } from "react";
import {
  getMatchScorecard,
  getMatches,
} from "../../api/matchesApi";
import type {
  MatchResponse,
  ScorecardResponse,
} from "../../api/types";

type RecentResult = {
  match: MatchResponse;
  scorecard: ScorecardResponse;
};

function formatScorecard(scorecard: ScorecardResponse) {
  if (!scorecard.innings || scorecard.innings.length === 0) {
    return "Scorecard unavailable";
  }

  return scorecard.innings
    .slice()
    .sort((a, b) => a.inningsNumber - b.inningsNumber)
    .map(
      (innings) =>
        `${innings.battingTeamName} ${innings.totalRuns}/${innings.wickets}`,
    )
    .join(" • ");
}

function getTeams(match: MatchResponse) {
  const teams = match.teams ?? [];

  if (teams.length >= 2) {
    return `${teams[0].teamName} vs ${teams[1].teamName}`;
  }

  if (teams.length === 1) {
    return `${teams[0].teamName} vs TBD`;
  }

  return "Teams to be announced";
}

export default function RecentResults() {
  const [results, setResults] = useState<RecentResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadRecentResults() {
      try {
        setLoading(true);
        setError("");

        const allMatches = await getMatches();

        const completedMatches = allMatches
          .filter((match) => match.status === "COMPLETED")
          .sort((a, b) => {
            const aTime = a.scheduledAt
              ? new Date(a.scheduledAt).getTime()
              : new Date(a.createdAt).getTime();

            const bTime = b.scheduledAt
              ? new Date(b.scheduledAt).getTime()
              : new Date(b.createdAt).getTime();

            return bTime - aTime;
          })
          .slice(0, 5);

        const recentResults = await Promise.all(
          completedMatches.map(async (match) => ({
            match,
            scorecard: await getMatchScorecard(match.id),
          })),
        );

        if (!cancelled) {
          setResults(recentResults);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Unable to load recent results.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadRecentResults();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <h3 className="font-semibold text-gray-800 dark:text-white/90">
            Recent Results
          </h3>
          <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
            Latest completed matches
          </p>
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
          Loading recent results...
        </div>
      )}

      {!loading && error && (
        <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
          {error}
        </div>
      )}

      {!loading && !error && results.length === 0 && (
        <div className="rounded-xl border border-gray-100 bg-gray-50 p-6 text-center dark:border-gray-800 dark:bg-white/[0.02]">
          <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
            No completed matches yet.
          </p>

          <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
            Completed match results will appear here.
          </p>
        </div>
      )}

      {!loading && !error && results.length > 0 && (
        <div className="space-y-3">
          {results.map(({ match, scorecard }) => (
            <div
              key={match.id}
              className="rounded-xl border border-gray-100 p-4 dark:border-gray-800"
            >
              <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                <div className="min-w-0">
                  <h4 className="font-medium text-gray-800 dark:text-white/90">
                    {getTeams(match)}
                  </h4>

                  <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                    {formatScorecard(scorecard)}
                  </p>
                </div>

                <span className="w-fit rounded-full bg-brand-50 px-3 py-1 text-xs font-medium text-brand-600 dark:bg-brand-500/10 dark:text-brand-400">
                  Completed
                </span>
              </div>

              {scorecard.result?.resultText && (
                <p className="mt-3 text-sm font-medium text-brand-600 dark:text-brand-400">
                  {scorecard.result.resultText}
                </p>
              )}

              <p className="mt-1 text-xs text-gray-400 dark:text-gray-500">
                {match.name}
              </p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}