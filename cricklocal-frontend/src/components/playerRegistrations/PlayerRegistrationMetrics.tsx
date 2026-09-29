import { useEffect, useState } from "react";
import {
  FileIcon,
  GroupIcon,
  TimeIcon,
  UserIcon,
} from "../../icons";
import {
  getPlayerRegistrationSummary,
  type PlayerRegistrationSummaryResponse,
} from "../../api/playerRegistrationApi";

type Metric = {
  label: string;
  value: number | null;
  icon: typeof FileIcon;
};

export default function PlayerRegistrationMetrics() {
  const [summary, setSummary] =
    useState<PlayerRegistrationSummaryResponse | null>(null);

  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadSummary() {
      try {
        setError("");

        const data = await getPlayerRegistrationSummary();

        if (cancelled) {
          return;
        }

        setSummary(data);
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Unable to load registration summary.",
          );
        }
      }
    }

    loadSummary();

    return () => {
      cancelled = true;
    };
  }, []);

  const metrics: Metric[] = [
    {
      label: "Total Players",
      value: summary?.totalPlayers ?? null,
      icon: GroupIcon,
    },
    {
      label: "Registered",
      value: summary?.registeredPlayers ?? null,
      icon: UserIcon,
    },
    {
      label: "Pending",
      value: summary?.pendingPlayers ?? null,
      icon: TimeIcon,
    },
    {
      label: "Expired Invitations",
      value: summary?.expiredInvitations ?? null,
      icon: FileIcon,
    },
  ];

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