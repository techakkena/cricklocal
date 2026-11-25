import { useEffect, useMemo, useState } from "react";
import { usePermission } from "../hooks/usePermission";
import PageBreadcrumb from "../components/common/PageBreadCrumb";
import PageMeta from "../components/common/PageMeta";
import Badge from "../components/ui/badge/Badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../components/ui/table";
import { getGlobalPlayers } from "../api/playersApi";
import {
  getPlayerRegistrationManagement,
  invitePlayerRegistration,
} from "../api/playerRegistrationApi";
import type { PlayerResponse } from "../api/types";

function formatCreatedDate(createdAt: string) {
  const date = new Date(createdAt);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(date);
}

function formatEnumValue(value: string | null | undefined) {
  if (!value) {
    return "—";
  }

  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function getTeamNames(player: PlayerResponse) {
  if (!player.teams || player.teams.length === 0) {
    return "—";
  }

  return player.teams
    .map((team) => team.shortName || team.teamName)
    .join(", ");
}

export default function GlobalPlayers() {
  const [players, setPlayers] = useState<PlayerResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");
  const [invitingPlayerId, setInvitingPlayerId] = useState<number | null>(null);
  const { hasPermission } = usePermission();
  const canViewRegistrations = hasPermission("PLAYER_REGISTRATIONS", "view");
  const canEditRegistrations = hasPermission("PLAYER_REGISTRATIONS", "edit");
  const [registrationStatuses, setRegistrationStatuses] = useState<
    Record< number,
      {
        registrationStatus: string | null;
        invitationStatus: string | null;
      }
    > 
  >({});

  useEffect(() => {
    let cancelled = false;

    async function loadGlobalPlayers() {
      try {
        setLoading(true);
        setError("");

        const response = await getGlobalPlayers();
        const registrations = canViewRegistrations
          ? await getPlayerRegistrationManagement()
          : [];

        const statusMap = Object.fromEntries(
          registrations.map((registration) => [
            registration.playerId,
            {
              registrationStatus: registration.registrationStatus,
              invitationStatus: registration.invitationStatus,
            },
          ]),
        );

        if (!cancelled) {
          setPlayers(response);
          setRegistrationStatuses(statusMap);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Unable to load global players.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadGlobalPlayers();
    
    return () => {
      cancelled = true;
    };
  }, [canViewRegistrations]);

  const filteredPlayers = useMemo(() => {
    const query = search.trim().toLowerCase();

    if (!query) {
      return players;
    }

    return players.filter((player) => {
      const searchableText = [
        player.displayName,
        player.firstName,
        player.lastName,
        player.role,
        player.battingStyle,
        player.bowlingStyle,
      ]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();

      return searchableText.includes(query);
    });
  }, [players, search]);

    async function handleInvite(playerId: number) {
      if (!canEditRegistrations) return;
      try {
        setInvitingPlayerId(playerId);

        await invitePlayerRegistration(playerId);

        setRegistrationStatuses((current) => ({
          ...current,
          [playerId]: {
            registrationStatus: current[playerId]?.registrationStatus ?? "PENDING",
            invitationStatus: "PENDING",
          },
        }));

        window.alert("Registration invitation created successfully.");
      } catch (err) {
        window.alert(
          err instanceof Error
            ? err.message
            : "Unable to create registration invitation.",
        );
      } finally {
        setInvitingPlayerId(null);
      }
    }

  return (
    <>
      <PageMeta
        title="Global Player Directory | CricketLocal"
        description="View registered CricketLocal global players."
      />

      <PageBreadcrumb pageTitle="Global Player Directory" />

      <div className="space-y-6">
        <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-5 sm:flex-row sm:items-center sm:justify-between dark:border-gray-800">
            <div>
              <h3 className="font-semibold text-gray-800 dark:text-white/90">
                Global Player Directory
              </h3>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                View registered CricketLocal players and their team
                memberships.
              </p>
            </div>

            <div className="text-sm text-gray-500 dark:text-gray-400">
              {loading
                ? "Loading..."
                : `${filteredPlayers.length} of ${players.length} player${
                    players.length === 1 ? "" : "s"
                  }`}
            </div>
          </div>

          {!loading && !error && players.length > 0 && (
            <div className="border-b border-gray-100 px-5 py-4 dark:border-gray-800">
              <label
                htmlFor="global-player-search"
                className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                Search Players
              </label>

              <input
                id="global-player-search"
                type="search"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search by player name, role, batting or bowling style..."
                className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>
          )}

          {loading && (
            <div className="p-6 text-center text-sm text-gray-500 dark:text-gray-400">
              Loading global players...
            </div>
          )}

          {!loading && error && (
            <div className="p-6">
              <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
                {error}
              </div>
            </div>
          )}

          {!loading && !error && players.length === 0 && (
            <div className="p-8 text-center">
              <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                No global players found.
              </p>
              <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                Registered global players will appear here.
              </p>
            </div>
          )}

          {!loading &&
            !error &&
            players.length > 0 &&
            filteredPlayers.length === 0 && (
              <div className="p-8 text-center">
                <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  No players match your search.
                </p>
                <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                  Try a different player name or search term.
                </p>
              </div>
            )}

          {!loading &&
            !error &&
            filteredPlayers.length > 0 && (
              <div className="max-w-full overflow-x-auto">
                <Table>
                  <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
                    <TableRow>
                      <TableCell
                        isHeader
                        className="px-5 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                      >
                        Player
                      </TableCell>

                      <TableCell
                        isHeader
                        className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                      >
                        Role
                      </TableCell>

                      <TableCell
                        isHeader
                        className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                      >
                        Batting
                      </TableCell>

                      <TableCell
                        isHeader
                        className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                      >
                        Bowling
                      </TableCell>

                      <TableCell
                        isHeader
                        className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                      >
                        Teams
                      </TableCell>

                      <TableCell
                        isHeader
                        className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                      >
                        Status
                      </TableCell>

                      <TableCell
                        isHeader
                        className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                      >
                        Registered
                      </TableCell>
                      {canEditRegistrations && (
                        <TableCell
                          isHeader
                          className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                        >
                          Actions
                        </TableCell>
                      )}
                    </TableRow>
                  </TableHeader>

                  <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                    {filteredPlayers.map((player) => {
                    const registrationInfo = registrationStatuses[player.id];
                    const registrationStatus = registrationInfo?.registrationStatus;
                    const invitationStatus = registrationInfo?.invitationStatus;

                    return (
                      <TableRow key={player.id}>
                        <TableCell className="px-5 py-4 text-start sm:px-6">
                          <div>
                            <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                              {player.displayName}
                            </span>
                            <span className="mt-1 block text-xs text-gray-500 dark:text-gray-400">
                              Player #{player.id}
                            </span>
                          </div>
                        </TableCell>

                        <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                          {formatEnumValue(player.role)}
                        </TableCell>

                        <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                          {formatEnumValue(player.battingStyle)}
                        </TableCell>

                        <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                          {formatEnumValue(player.bowlingStyle)}
                        </TableCell>

                        <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                          {getTeamNames(player)}
                        </TableCell>

                        <TableCell className="px-4 py-3 text-start">
                          <Badge
                            size="sm"
                            color={player.active ? "success" : "error"}
                          >
                            {player.active ? "Active" : "Inactive"}
                          </Badge>
                        </TableCell>

                        <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                          {formatCreatedDate(player.createdAt)}
                        </TableCell>
                        {canEditRegistrations && <TableCell className="px-4 py-3 text-start">
                         <button
                            type="button"
                            onClick={() => handleInvite(player.id)}
                            disabled={
                              invitingPlayerId === player.id ||
                              registrationStatus === "REGISTERED" ||
                              invitationStatus === "PENDING"
                            }
                            className="rounded-lg bg-brand-500 px-3 py-2 text-xs font-medium text-white transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            {invitingPlayerId === player.id
                              ? "Inviting..."
                              : registrationStatus === "REGISTERED"
                                ? "Registered"
                                : invitationStatus === "PENDING"
                                  ? "Invitation Pending"
                                  : "Invite"}
                          </button>
                        </TableCell>}
                          </TableRow>
                          );
                        })}
                  </TableBody>
                </Table>
              </div>
            )}
        </div>
      </div>
    </>
  );
}