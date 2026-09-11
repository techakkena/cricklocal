import { useEffect, useState } from "react";
import { apiGet } from "../../api/apiClient";
import type { MatchResponse } from "../../api/types";

interface PlayerResponse {
  id: number;
  displayName: string;
  active: boolean;
  createdAt: string;
}

interface SeriesResponse {
  id: number;
  name: string;
  createdAt: string;
  status: string;
}

type Activity = {
  id: string;
  title: string;
  description: string;
  createdAt: string;
};

function formatRelativeTime(createdAt: string) {
  const timestamp = new Date(createdAt).getTime();

  if (Number.isNaN(timestamp)) {
    return "Recently";
  }

  const diffMs = Date.now() - timestamp;
  const diffMinutes = Math.max(0, Math.floor(diffMs / 60000));

  if (diffMinutes < 1) {
    return "Just now";
  }

  if (diffMinutes < 60) {
    return `${diffMinutes} min ago`;
  }

  const diffHours = Math.floor(diffMinutes / 60);

  if (diffHours < 24) {
    return `${diffHours} hr ago`;
  }

  const diffDays = Math.floor(diffHours / 24);

  if (diffDays < 7) {
    return `${diffDays} day${diffDays === 1 ? "" : "s"} ago`;
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(new Date(createdAt));
}

function buildMatchActivity(match: MatchResponse): Activity {
  if (match.status === "COMPLETED") {
    return {
      id: `match-completed-${match.id}`,
      title: "Match completed",
      description: match.name,
      createdAt: match.createdAt,
    };
  }

  if (match.status === "LIVE") {
    return {
      id: `match-live-${match.id}`,
      title: "Match started",
      description: match.name,
      createdAt: match.createdAt,
    };
  }

  if (match.status === "SCHEDULED") {
    return {
      id: `match-scheduled-${match.id}`,
      title: "Match scheduled",
      description: match.name,
      createdAt: match.createdAt,
    };
  }

  return {
    id: `match-${match.id}`,
    title: "Match updated",
    description: match.name,
    createdAt: match.createdAt,
  };
}

export default function RecentActivity() {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadRecentActivity() {
      try {
        setLoading(true);
        setError("");

        const [matches, players, series] = await Promise.all([
          apiGet<MatchResponse[]>("/api/matches"),
          apiGet<PlayerResponse[]>("/api/players"),
          apiGet<SeriesResponse[]>("/api/series"),
        ]);

        const matchActivities = matches.map(buildMatchActivity);

        const playerActivities: Activity[] = players.map((player) => ({
          id: `player-${player.id}`,
          title: "New player added",
          description: player.displayName,
          createdAt: player.createdAt,
        }));

        const seriesActivities: Activity[] = series.map((item) => ({
          id: `series-${item.id}`,
          title: "Series created",
          description: item.name,
          createdAt: item.createdAt,
        }));

        const recentActivities = [
          ...matchActivities,
          ...playerActivities,
          ...seriesActivities,
        ]
          .sort(
            (a, b) =>
              new Date(b.createdAt).getTime() -
              new Date(a.createdAt).getTime(),
          )
          .slice(0, 5);

        if (!cancelled) {
          setActivities(recentActivities);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Unable to load recent activity.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadRecentActivity();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
      <div className="mb-5">
        <h3 className="font-semibold text-gray-800 dark:text-white/90">
          Recent Activity
        </h3>

        <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
          Latest CricketLocal activity
        </p>
      </div>

      {loading && (
        <div className="rounded-xl border border-gray-100 bg-gray-50 p-6 text-center text-sm text-gray-500 dark:border-gray-800 dark:bg-white/[0.02] dark:text-gray-400">
          Loading recent activity...
        </div>
      )}

      {!loading && error && (
        <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
          {error}
        </div>
      )}

      {!loading && !error && activities.length === 0 && (
        <div className="rounded-xl border border-gray-100 bg-gray-50 p-6 text-center dark:border-gray-800 dark:bg-white/[0.02]">
          <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
            No recent activity.
          </p>

          <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
            New CricketLocal activity will appear here.
          </p>
        </div>
      )}

      {!loading && !error && activities.length > 0 && (
        <div className="space-y-5">
          {activities.map((activity) => (
            <div key={activity.id} className="flex gap-4">
              <span className="mt-1 h-2.5 w-2.5 shrink-0 rounded-full bg-brand-500" />

              <div className="min-w-0 flex-1">
                <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                  <h4 className="text-sm font-medium text-gray-800 dark:text-white/90">
                    {activity.title}
                  </h4>

                  <span className="text-xs text-gray-400 dark:text-gray-500">
                    {formatRelativeTime(activity.createdAt)}
                  </span>
                </div>

                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  {activity.description}
                </p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}