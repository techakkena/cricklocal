import { BoltIcon, CalenderIcon, FileIcon, GroupIcon } from "../../icons";

const metrics = [
  {
    label: "Live Matches",
    value: "2",
    icon: BoltIcon,
  },
  {
    label: "Upcoming Matches",
    value: "4",
    icon: CalenderIcon,
  },
  {
    label: "Completed Matches",
    value: "28",
    icon: FileIcon,
  },
  {
    label: "Teams",
    value: "12",
    icon: GroupIcon,
  },
];

export default function DashboardMetrics() {
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
              {metric.value}
            </h3>
          </div>
        );
      })}
    </div>
  );
}