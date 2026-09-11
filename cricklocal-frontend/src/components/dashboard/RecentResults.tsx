const results = [
  {
    teams: "CMR Royals vs Strikers",
    score: "CMR Royals 145/6 • Strikers 106/8",
    result: "CMR Royals won by 39 runs",
  },
  {
    teams: "CMR Warriors vs CMR Titans",
    score: "CMR Warriors 132/7 • CMR Titans 128/9",
    result: "CMR Warriors won by 4 runs",
  },
  {
    teams: "CMR Lions vs CMR Eagles",
    score: "CMR Lions 118/5 • CMR Eagles 119/4",
    result: "CMR Eagles won by 6 wickets",
  },
];

export default function RecentResults() {
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

      <div className="space-y-3">
        {results.map((result) => (
          <div
            key={result.teams}
            className="rounded-xl border border-gray-100 p-4 dark:border-gray-800"
          >
            <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
              <div className="min-w-0">
                <h4 className="font-medium text-gray-800 dark:text-white/90">
                  {result.teams}
                </h4>

                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  {result.score}
                </p>
              </div>

              <span className="w-fit rounded-full bg-brand-50 px-3 py-1 text-xs font-medium text-brand-600 dark:bg-brand-500/10 dark:text-brand-400">
                Completed
              </span>
            </div>

            <p className="mt-3 text-sm font-medium text-brand-600 dark:text-brand-400">
              {result.result}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
}