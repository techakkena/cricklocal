import { useEffect, useState } from "react";
import { BoltIcon, CalenderIcon, FileIcon, GroupIcon } from "../../icons";
import { getMatches } from "../../api/matchesApi";
import { apiGet } from "../../api/apiClient";
import type { MatchResponse } from "../../api/types";

type Metric = {
  label: string;
  value: number | null;
  icon: typeof BoltIcon;
};

export default function DashboardMetrics() {
  const [metrics, setMetrics] = useState<Metric[]>([
    {
      label: "Live Matches",
      value: null,
      icon: BoltIcon,
    },
    {
      label: "Upcoming Matches",
      value: null,
      icon: CalenderIcon,
    },
    {
      label: "Completed Matches",
      value: null,
      icon: FileIcon,
    },
    {
      label: "Teams",
      value: null,
      icon: GroupIcon,
    },
  ]);

  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadMetrics() {
      try {
        setError("");

        const [matches, teams] = await Promise.all([
          getMatches(),
          apiGet<unknown[]>("/api/teams"),
        ]);

        if (cancelled) {
          return;
        }

        const liveCount = matches.filter(
          (match: MatchResponse) => match.status === "LIVE",
        ).length;

        const upcomingCount = matches.filter(
          (match: MatchResponse) => match.status === "SCHEDULED",
        ).length;

        const completedCount = matches.filter(
          (match: MatchResponse) => match.status === "COMPLETED",
        ).length;

        setMetrics([
          {
            label: "Live Matches",
            value: liveCount,
            icon: BoltIcon,
          },
          {
            label: "Upcoming Matches",
            value: upcomingCount,
            icon: CalenderIcon,
          },
          {
            label: "Completed Matches",
            value: completedCount,
            icon: FileIcon,
          },
          {
            label: "Teams",
            value: teams.length,
            icon: GroupIcon,
          },
        ]);
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Unable to load dashboard metrics.",
          );
        }
      }
    }

    loadMetrics();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div className="grid grid-cols-2 gap-4 xl:grid-cols-4">
      {metrics.map((metric) => {
        const Icon = metric.icon;

        return (
          <div
            key={metric.label}
            className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]"
          >
            <div className="mb-4 flex h-11 w-11 items-center justify-center rounded-xl bg-brand-50 text-brand-500 dark:bg-brand-500/15 dark:text-brand-400">
              <Icon className="size-5" />
            </div>

            <p className="text-sm text-gray-500 dark:text-gray-400">
              {metric.label}
            </p>

            <h3 className="mt-2 text-2xl font-bold text-gray-800 dark:text-white/90">
              {metric.value ?? "—"}
            </h3>

            {error && (
              <p className="mt-1 text-xs text-error-500">
                Unable to load
              </p>
            )}
          </div>
        );
      })}
    </div>
  );
}