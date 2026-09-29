import { useState } from "react";
import PageMeta from "../components/common/PageMeta";
import PlayerRegistrationMetrics from "../components/playerRegistrations/PlayerRegistrationMetrics";
import PlayerRegistrationTable from "../components/playerRegistrations/PlayerRegistrationTable";
import { backfillPlayerRegistrationInvitations } from "../api/playerRegistrationApi";

export default function PlayerRegistrations() {
  const [backfilling, setBackfilling] = useState(false);
  const [backfillMessage, setBackfillMessage] = useState<string | null>(null);

  async function handleBackfillInvitations() {
    try {
      setBackfilling(true);
      setBackfillMessage(null);

      const result = await backfillPlayerRegistrationInvitations();

      setBackfillMessage(
        `Backfill complete: ${result.invitationsCreated} invitations created, ${result.playersSkipped} players skipped.`,
      );
    } catch (error) {
      console.error("Failed to backfill invitations:", error);

      setBackfillMessage(
        error instanceof Error
          ? error.message
          : "Failed to backfill invitations.",
      );
    } finally {
      setBackfilling(false);
    }
  }

  return (
    <>
      <PageMeta
        title="Player Registrations | CricketLocal"
        description="Manage CricketLocal player registrations"
      />

      <div className="space-y-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-semibold text-gray-800 dark:text-white/90">
              Player Registrations
            </h1>

            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Manage player registration invitations and registration status.
            </p>
          </div>

          <button
            type="button"
            onClick={handleBackfillInvitations}
            disabled={backfilling}
            className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-3 text-sm font-medium text-white transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {backfilling
              ? "Creating Invitations..."
              : "Backfill Invitations"}
          </button>
        </div>

        {backfillMessage && (
          <div className="rounded-lg border border-gray-200 bg-white px-4 py-3 text-sm text-gray-700 dark:border-gray-800 dark:bg-white/[0.03] dark:text-gray-300">
            {backfillMessage}
          </div>
        )}

        <PlayerRegistrationMetrics />

        <PlayerRegistrationTable />
      </div>
    </>
  );
}