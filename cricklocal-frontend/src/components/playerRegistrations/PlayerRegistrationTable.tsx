import { useEffect, useState } from "react";
import {
  getPlayerRegistrationDetail,
  getPlayerRegistrationManagement,
  type PlayerRegistrationDetailResponse,
  type PlayerRegistrationManagementResponse,
} from "../../api/playerRegistrationApi";
import PlayerRegistrationDetailModal from "./PlayerRegistrationDetailModal";

function formatDate(value: string | null): string {
  if (!value) {
    return "—";
  }

  return new Date(value).toLocaleString();
}

function getStatusClasses(status: string): string {
  if (status === "REGISTERED") {
    return "bg-success-50 text-success-700 dark:bg-success-500/10 dark:text-success-400";
  }

  if (status === "PENDING") {
    return "bg-warning-50 text-warning-700 dark:bg-warning-500/10 dark:text-warning-400";
  }

  return "bg-gray-100 text-gray-700 dark:bg-white/5 dark:text-gray-300";
}

export default function PlayerRegistrationTable() {
  const [registrations, setRegistrations] = useState<
    PlayerRegistrationManagementResponse[]
  >([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedPlayer, setSelectedPlayer] =
    useState<PlayerRegistrationDetailResponse | null>(null);

  const [detailLoading, setDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState<string | null>(null);

  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);

  useEffect(() => {
    async function loadRegistrations() {
      try {
        setLoading(true);
        setError(null);

        const data = await getPlayerRegistrationManagement();

        setRegistrations(data);
      } catch (err) {
        console.error(
          "Failed to load player registrations:",
          err,
        );

        setError("Unable to load player registrations.");
      } finally {
        setLoading(false);
      }
    }

    loadRegistrations();
  }, []);

  async function handleViewPlayer(playerId: number) {
    setIsDetailModalOpen(true);
    setDetailLoading(true);
    setDetailError(null);
    setSelectedPlayer(null);

    try {
      const data = await getPlayerRegistrationDetail(playerId);

      setSelectedPlayer(data);
    } catch (err) {
      console.error(
        "Failed to load player registration details:",
        err,
      );

      setDetailError(
        "Unable to load player registration details.",
      );
    } finally {
      setDetailLoading(false);
    }
  }

  function handleCloseDetailModal() {
    setIsDetailModalOpen(false);
    setSelectedPlayer(null);
    setDetailError(null);
  }

  return (
    <>
      <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
        <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">
          <div>
            <h2 className="text-lg font-semibold text-gray-800 dark:text-white/90">
              Player Registration Status
            </h2>

            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              View registration and invitation status for imported players.
            </p>
          </div>
        </div>

        {loading && (
          <div className="px-6 py-10 text-center text-sm text-gray-500 dark:text-gray-400">
            Loading player registrations...
          </div>
        )}

        {!loading && error && (
          <div className="px-6 py-10 text-center text-sm text-error-600 dark:text-error-400">
            {error}
          </div>
        )}

        {!loading && !error && registrations.length === 0 && (
          <div className="px-6 py-10 text-center text-sm text-gray-500 dark:text-gray-400">
            No player registrations found.
          </div>
        )}

        {!loading && !error && registrations.length > 0 && (
          <div className="overflow-x-auto">
            <table className="min-w-full">
              <thead>
                <tr className="border-b border-gray-200 dark:border-gray-800">
                  <th className="px-6 py-4 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                    Player
                  </th>

                  <th className="px-6 py-4 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                    Registration
                  </th>

                  <th className="px-6 py-4 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                    Invitation
                  </th>

                  <th className="px-6 py-4 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                    Created
                  </th>

                  <th className="px-6 py-4 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                    Expires
                  </th>

                  <th className="px-6 py-4 text-right text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                    Actions
                  </th>
                </tr>
              </thead>

              <tbody className="divide-y divide-gray-200 dark:divide-gray-800">
                {registrations.map((registration) => (
                  <tr
                    key={registration.playerId}
                    className="hover:bg-gray-50 dark:hover:bg-white/[0.02]"
                  >
                    <td className="whitespace-nowrap px-6 py-4">
                      <div>
                        <p className="font-medium text-gray-800 dark:text-white/90">
                          {registration.displayName}
                        </p>

                        <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                          Player ID: {registration.playerId}
                        </p>
                      </div>
                    </td>

                    <td className="whitespace-nowrap px-6 py-4">
                      <span
                        className={`inline-flex rounded-full px-2.5 py-1 text-xs font-medium ${getStatusClasses(
                          registration.registrationStatus,
                        )}`}
                      >
                        {registration.registrationStatus}
                      </span>
                    </td>

                    <td className="whitespace-nowrap px-6 py-4">
                      <span className="text-sm text-gray-700 dark:text-gray-300">
                        {registration.invitationStatus}
                      </span>
                    </td>

                    <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-600 dark:text-gray-400">
                      {formatDate(registration.invitationCreatedAt)}
                    </td>

                    <td className="whitespace-nowrap px-6 py-4 text-sm text-gray-600 dark:text-gray-400">
                      {formatDate(registration.invitationExpiresAt)}
                    </td>

                    <td className="whitespace-nowrap px-6 py-4 text-right">
                      <button
                        type="button"
                        onClick={() =>
                          handleViewPlayer(registration.playerId)
                        }
                        className="text-sm font-medium text-brand-500 hover:text-brand-600"
                      >
                        View
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <PlayerRegistrationDetailModal
        isOpen={isDetailModalOpen}
        onClose={handleCloseDetailModal}
        registration={selectedPlayer}
        loading={detailLoading}
        error={detailError}
      />
    </>
  );
}