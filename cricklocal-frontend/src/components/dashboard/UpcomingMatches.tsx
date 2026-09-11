import { CalenderIcon } from "../../icons";

const matches = [
  {
    teams: "CMR Royals vs Strikers",
    schedule: "Today • 6:00 PM",
    venue: "CMR Cricket Club",
  },
  {
    teams: "CMR Warriors vs CMR Titans",
    schedule: "Tomorrow • 4:00 PM",
    venue: "Local Ground",
  },
];

export default function UpcomingMatches() {
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

      <div className="space-y-3">
        {matches.map((match) => (
          <div
            key={match.teams}
            className="flex flex-col gap-4 rounded-xl border border-gray-100 bg-gray-50 p-4 dark:border-gray-800 dark:bg-white/[0.02] sm:flex-row sm:items-center sm:justify-between"
          >
            <div>
              <h4 className="font-medium text-gray-800 dark:text-white/90">
                {match.teams}
              </h4>

              <div className="mt-1 flex flex-col gap-1 text-xs text-gray-500 dark:text-gray-400 sm:flex-row sm:gap-3">
                <span>{match.schedule}</span>
                <span className="hidden sm:inline">•</span>
                <span>{match.venue}</span>
              </div>
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
    </div>
  );
}