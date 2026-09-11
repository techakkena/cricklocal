const topBatters = [
  { name: "Arjun Kumar", value: "245 runs" },
  { name: "Rahul Dev", value: "218 runs" },
  { name: "Vikram Reddy", value: "196 runs" },
];

const topBowlers = [
  { name: "Suresh Kumar", value: "12 wickets" },
  { name: "Kiran Raj", value: "9 wickets" },
  { name: "Rohit Sai", value: "8 wickets" },
];

function PerformerList({
  title,
  items,
}: {
  title: string;
  items: { name: string; value: string }[];
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

      <div className="space-y-3">
        {items.map((item, index) => (
          <div
            key={item.name}
            className="flex items-center justify-between rounded-xl border border-gray-100 p-3 dark:border-gray-800"
          >
            <div className="flex min-w-0 items-center gap-3">
              <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-brand-50 text-xs font-semibold text-brand-600 dark:bg-brand-500/10 dark:text-brand-400">
                {index + 1}
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
    </div>
  );
}

export default function TopPerformers() {
  return (
    <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
      <PerformerList title="Top Batters" items={topBatters} />
      <PerformerList title="Top Bowlers" items={topBowlers} />
    </div>
  );
}