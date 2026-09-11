import { useEffect, useState } from "react";
import { apiGet } from "../../api/apiClient";

interface LeaderboardEntry {
  rank: number;
  playerId: number;
  playerName: string;
  primaryValue: number;
  secondaryValue: number;
}

interface LeaderboardResponse {
  category: string;
  entries: LeaderboardEntry[];
}

type Performer = {
  name: string;
  value: string;
  rank: number;
};

function PerformerList({
  title,
  items,
  emptyMessage,
}: {
  title: string;
  items: Performer[];
  emptyMessage: string;
}) {
  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="mb-5">
        <h3 className="font-semibold text-gray-800 dark:text-white/90">
          {title}
        </h3>

        <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
          Current tournament leaders
        </p>
      </div>

      {items.length === 0 ? (
        <div className="rounded-xl border border-gray-100 bg-gray-50 p-6 text-center text-sm text-gray-500 dark:border-gray-800 dark:bg-white/[0.02] dark:text-gray-400">
          {emptyMessage}
        </div>
      ) : (
        <div className="space-y-3">
          {items.map((item) => (
            <div
              key={`${item.rank}-${item.name}`}
              className="flex items-center justify-between rounded-xl border border-gray-100 p-3 dark:border-gray-800"
            >
              <div className="flex min-w-0 items-center gap-3">
                <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-brand-50 text-xs font-semibold text-brand-600 dark:bg-brand-500/10 dark:text-brand-400">
                  {item.rank}
                </span>

                <span className="truncate text-sm font-medium text-gray-800 dark:text-white/90">
                  {item.name}
                </span>
              </div>

              <span className="ml-3 shrink-0 text-sm font-semibold text-brand-600 dark:text-brand-400">
                {item.value}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default function TopPerformers() {
  const [topBatters, setTopBatters] = useState<Performer[]>([]);
  const [topBowlers, setTopBowlers] = useState<Performer[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadTopPerformers() {
      try {
        setLoading(true);
        setError("");

        const [runs, wickets] = await Promise.all([
          apiGet<LeaderboardResponse>("/api/leaderboards/top-runs"),
          apiGet<LeaderboardResponse>("/api/leaderboards/top-wickets"),
        ]);

        if (cancelled) {
          return;
        }

        setTopBatters(
          runs.entries.map((entry) => ({
            name: entry.playerName,
            value: `${entry.primaryValue} runs`,
            rank: entry.rank,
          })),
        );

        setTopBowlers(
          wickets.entries.map((entry) => ({
            name: entry.playerName,
            value: `${entry.primaryValue} wickets`,
            rank: entry.rank,
          })),
        );
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Unable to load top performers.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadTopPerformers();

    return () => {
      cancelled = true;
    };
  }, []);

  if (loading) {
    return (
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
        <div className="rounded-2xl border border-gray-200 bg-white p-5 text-center text-sm text-gray-500 dark:border-gray-800 dark:bg-white/[0.03] dark:text-gray-400">
          Loading top performers...
        </div>

        <div className="rounded-2xl border border-gray-200 bg-white p-5 text-center text-sm text-gray-500 dark:border-gray-800 dark:bg-white/[0.03] dark:text-gray-400">
          Loading top performers...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-2xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
        {error}
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
      <PerformerList
        title="Top Batters"
        items={topBatters}
        emptyMessage="No batting statistics available yet."
      />

      <PerformerList
        title="Top Bowlers"
        items={topBowlers}
        emptyMessage="No bowling statistics available yet."
      />
    </div>
  );
}