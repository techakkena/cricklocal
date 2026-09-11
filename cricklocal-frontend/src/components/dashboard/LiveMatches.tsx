import { useEffect, useState } from "react";
import { BoltIcon } from "../../icons";
import { getMatchInnings, getMatches } from "../../api/matchesApi";
import type { InningsResponse, MatchResponse } from "../../api/types";

type LiveMatch = {
  match: MatchResponse;
  innings: InningsResponse[];
};

function formatOvers(legalBalls: number) {
  const overs = Math.floor(legalBalls / 6);
  const balls = legalBalls % 6;

  return `${overs}.${balls}`;
}

export default function LiveMatches() {
  const [liveMatches, setLiveMatches] = useState<LiveMatch[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadLiveMatches() {
      try {
        setLoading(true);
        setError("");

        const matches = await getMatches();

        const live = matches.filter((match) => match.status === "LIVE");

        const matchData = await Promise.all(
          live.map(async (match) => ({
            match,
            innings: await getMatchInnings(match.id),
          })),
        );

        if (!cancelled) {
          setLiveMatches(matchData);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Unable to load live matches.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadLiveMatches();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="mb-5 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-50 text-brand-500 dark:bg-brand-500/15 dark:text-brand-400">
            <BoltIcon className="size-5" />
          </span>

          <div>
            <h3 className="font-semibold text-gray-800 dark:text-white/90">
              Live Matches
            </h3>
            <p className="text-xs text-gray-500 dark:text-gray-400">
              Matches happening now
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
          Loading live matches...
        </div>
      )}

      {!loading && error && (
        <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
          {error}
        </div>
      )}

      {!loading && !error && liveMatches.length === 0 && (
        <div className="rounded-xl border border-gray-100 bg-gray-50 p-6 text-center dark:border-gray-800 dark:bg-white/[0.02]">
          <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
            No live matches right now.
          </p>

          <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
            Live matches will appear here when a match starts.
          </p>
        </div>
      )}

      {!loading &&
        !error &&
        liveMatches.map(({ match, innings }) => {
        const sortedInnings = [...innings].sort(
            (a, b) => a.inningsNumber - b.inningsNumber,
            );

        const latestInnings =
            sortedInnings.length > 0
                ? sortedInnings[sortedInnings.length - 1]
                : undefined;

        const teams = match.teams ?? [];

          return (
            <div
              key={match.id}
              className="rounded-xl border border-gray-100 bg-gray-50 p-4 dark:border-gray-800 dark:bg-white/[0.02]"
            >
              <div className="mb-4 flex items-center gap-2">
                <span className="h-2.5 w-2.5 animate-pulse rounded-full bg-red-500" />
                <span className="text-xs font-semibold uppercase tracking-wide text-red-500">
                  Live
                </span>
              </div>

              <div className="grid grid-cols-[1fr_auto] gap-y-3 text-sm">
                {latestInnings ? (
                  <>
                    <span className="font-medium text-gray-800 dark:text-white/90">
                      {latestInnings.battingTeamName}
                    </span>

                    <span className="font-semibold text-gray-800 dark:text-white/90">
                      {latestInnings.totalRuns}/{latestInnings.wickets} (
                      {formatOvers(latestInnings.legalBalls)})
                    </span>

                    <span className="font-medium text-gray-800 dark:text-white/90">
                      {latestInnings.bowlingTeamName}
                    </span>

                    <span className="font-semibold text-gray-800 dark:text-white/90">
                      —
                    </span>
                  </>
                ) : (
                  <>
                    <span className="font-medium text-gray-800 dark:text-white/90">
                      {teams[0]?.teamName ?? "Team 1"}
                    </span>

                    <span className="font-semibold text-gray-800 dark:text-white/90">
                      —
                    </span>

                    <span className="font-medium text-gray-800 dark:text-white/90">
                      {teams[1]?.teamName ?? "Team 2"}
                    </span>

                    <span className="font-semibold text-gray-800 dark:text-white/90">
                      —
                    </span>
                  </>
                )}
              </div>

              <div className="mt-4 flex flex-col gap-3 border-t border-gray-200 pt-4 dark:border-gray-800 sm:flex-row sm:items-center sm:justify-between">
                <span className="text-xs text-gray-500 dark:text-gray-400">
                  {match.venue ?? "Venue not specified"}
                  {" • "}
                  {match.name}
                </span>

                <button
                  type="button"
                  className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600"
                >
                  Open Live Score
                </button>
              </div>
            </div>
          );
        })}
    </div>
  );
}