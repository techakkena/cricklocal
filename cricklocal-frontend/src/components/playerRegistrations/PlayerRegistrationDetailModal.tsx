import { useEffect, useState } from "react";
import type {
  PlayerRegistrationDetailResponse,
  PlayerRegistrationLinkResponse,
} from "../../api/playerRegistrationApi";
import { getPlayerRegistrationLink } from "../../api/playerRegistrationApi";
import { Modal } from "../ui/modal";

interface PlayerRegistrationDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  registration: PlayerRegistrationDetailResponse | null;
  loading: boolean;
  error: string | null;
}

function DetailRow({
  label,
  value,
}: {
  label: string;
  value: string | number | null;
}) {
  return (
    <div className="border-b border-gray-100 py-3 last:border-b-0 dark:border-gray-800">
      <div className="text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-gray-400">
        {label}
      </div>

      <div className="mt-1 text-sm font-medium text-gray-800 dark:text-white/90">
        {value === null || value === "" ? "—" : value}
      </div>
    </div>
  );
}

export default function PlayerRegistrationDetailModal({
  isOpen,
  onClose,
  registration,
  loading,
  error,
}: PlayerRegistrationDetailModalProps) {
  const [linkData, setLinkData] =
    useState<PlayerRegistrationLinkResponse | null>(null);

  const [linkLoading, setLinkLoading] = useState(false);
  const [linkError, setLinkError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (!isOpen || !registration) {
      setLinkData(null);
      setLinkError(null);
      setCopied(false);
      return;
    }

    if (registration.registrationStatus !== "PENDING") {
      setLinkData(null);
      setLinkError(null);
      setCopied(false);
      return;
    }

    const playerId = registration.playerId;

    let cancelled = false;

    async function loadRegistrationLink() {
      setLinkLoading(true);
      setLinkError(null);
      setCopied(false);

      try {
        const response = await getPlayerRegistrationLink(playerId);

        if (!cancelled) {
          setLinkData(response);
        }
      } catch (err) {
        if (!cancelled) {
          setLinkError(
            err instanceof Error
              ? err.message
              : "Failed to load registration link.",
          );
        }
      } finally {
        if (!cancelled) {
          setLinkLoading(false);
        }
      }
    }

    void loadRegistrationLink();

    return () => {
      cancelled = true;
    };
  }, [isOpen, registration]);

  async function handleCopyLink() {
    if (!linkData?.registrationLink) {
      return;
    }

    try {
      await navigator.clipboard.writeText(
        linkData.registrationLink,
      );

      setCopied(true);

      window.setTimeout(() => {
        setCopied(false);
      }, 2000);
    } catch {
      setLinkError("Failed to copy registration link.");
    }
  }

  function handleOpenLink() {
    if (!linkData?.registrationLink) {
      return;
    }

    window.open(
      linkData.registrationLink,
      "_blank",
      "noopener,noreferrer",
    );
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      className="max-w-[700px] m-4"
    >
      <div className="p-6 sm:p-8">
        <div className="mb-6">
          <h2 className="text-xl font-semibold text-gray-800 dark:text-white/90">
            Player Registration Details
          </h2>

          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            View the player's registration and invitation information.
          </p>
        </div>

        {loading && (
          <div className="py-10 text-center text-sm text-gray-500 dark:text-gray-400">
            Loading player details...
          </div>
        )}

        {!loading && error && (
          <div className="rounded-xl border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
            {error}
          </div>
        )}

        {!loading && !error && registration && (
          <>
            <div className="grid grid-cols-1 gap-x-8 sm:grid-cols-2">
              <DetailRow
                label="Player ID"
                value={registration.playerId}
              />

              <DetailRow
                label="Registration Status"
                value={registration.registrationStatus}
              />

              <DetailRow
                label="Display Name"
                value={registration.displayName}
              />

              <DetailRow
                label="First Name"
                value={registration.firstName}
              />

              <DetailRow
                label="Last Name"
                value={registration.lastName}
              />

              <DetailRow
                label="Phone"
                value={registration.phone}
              />

              <DetailRow
                label="Batting Style"
                value={registration.battingStyle}
              />

              <DetailRow
                label="Bowling Style"
                value={registration.bowlingStyle}
              />

              <DetailRow
                label="Role"
                value={registration.role}
              />

              <DetailRow
                label="User ID"
                value={registration.userId}
              />

              <DetailRow
                label="User Email"
                value={registration.userEmail}
              />

              <DetailRow
                label="User Display Name"
                value={registration.userDisplayName}
              />

              <DetailRow
                label="Invitation Status"
                value={registration.invitationStatus}
              />

              <DetailRow
                label="Invitation Created"
                value={registration.invitationCreatedAt}
              />

              <DetailRow
                label="Invitation Expires"
                value={registration.invitationExpiresAt}
              />

              <DetailRow
                label="Registration Completed"
                value={registration.invitationCompletedAt}
              />
            </div>

            <div className="mt-6 rounded-xl border border-gray-200 p-4 dark:border-gray-800">
              <div className="text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-gray-400">
                Registration Link
              </div>

              {registration.registrationStatus !== "PENDING" ? (
                <div className="mt-2 text-sm text-gray-600 dark:text-gray-300">
                  Registration completed. There is no active registration
                  link.
                </div>
              ) : (
                <>
                  {linkLoading && (
                    <div className="mt-2 text-sm text-gray-500 dark:text-gray-400">
                      Loading registration link...
                    </div>
                  )}

                  {!linkLoading && linkError && (
                    <div className="mt-2 text-sm text-error-600 dark:text-error-400">
                      {linkError}
                    </div>
                  )}

                  {!linkLoading && !linkError && linkData && (
                    <>
                      <div className="mt-2 break-all rounded-lg bg-gray-50 p-3 text-sm text-gray-700 dark:bg-white/[0.03] dark:text-gray-300">
                        {linkData.registrationLink}
                      </div>

                      <div className="mt-3 flex flex-wrap gap-2">
                        <button
                          type="button"
                          onClick={handleCopyLink}
                          className="rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white transition hover:bg-brand-600"
                        >
                          {copied ? "Copied" : "Copy Link"}
                        </button>

                        <button
                          type="button"
                          onClick={handleOpenLink}
                          className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
                        >
                          Open Link
                        </button>
                      </div>

                      <div className="mt-2 text-xs text-gray-500 dark:text-gray-400">
                        Expires: {linkData.expiresAt}
                      </div>
                    </>
                  )}
                </>
              )}
            </div>
          </>
        )}

        <div className="mt-6 flex justify-end border-t border-gray-100 pt-5 dark:border-gray-800">
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
          >
            Close
          </button>
        </div>
      </div>
    </Modal>
  );
}