const activities = [
  {
    title: "Match completed",
    description: "CMR Royals defeated Strikers by 39 runs",
    time: "15 min ago",
  },
  {
    title: "Match started",
    description: "CMR Warriors vs CMR Titans is now live",
    time: "1 hr ago",
  },
  {
    title: "New player added",
    description: "Vikram Reddy was added to CMR Royals",
    time: "3 hrs ago",
  },
  {
    title: "Series created",
    description: "CMR Local Premier League 2026",
    time: "Yesterday",
  },
];

export default function RecentActivity() {
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

      <div className="space-y-5">
        {activities.map((activity) => (
          <div key={activity.title + activity.time} className="flex gap-4">
            <span className="mt-1 h-2.5 w-2.5 shrink-0 rounded-full bg-brand-500" />

            <div className="min-w-0 flex-1">
              <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                <h4 className="text-sm font-medium text-gray-800 dark:text-white/90">
                  {activity.title}
                </h4>

                <span className="text-xs text-gray-400 dark:text-gray-500">
                  {activity.time}
                </span>
              </div>

              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                {activity.description}
              </p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}