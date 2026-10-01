import { useEffect, useMemo, useState } from "react";
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
import { getLocalPlayers } from "../api/playersApi";
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

export default function LocalPlayers() {
  const [players, setPlayers] = useState<PlayerResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [search, setSearch] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadLocalPlayers() {
      try {
        setLoading(true);
        setError("");

        const response = await getLocalPlayers();

        if (!cancelled) {
          setPlayers(response);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : "Unable to load local players.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadLocalPlayers();

    return () => {
      cancelled = true;
    };
  }, []);

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

  return (
    <>
      <PageMeta
        title="Local Players | CricketLocal"
        description="View CricketLocal local players."
      />

      <PageBreadcrumb pageTitle="Local Players" />

      <div className="space-y-6">
        <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-5 sm:flex-row sm:items-center sm:justify-between dark:border-gray-800">
            <div>
              <h3 className="font-semibold text-gray-800 dark:text-white/90">
                Local Players
              </h3>

              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                View players registered locally in CricketLocal.
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
                htmlFor="local-player-search"
                className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                Search Players
              </label>

              <input
                id="local-player-search"
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
              Loading local players...
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
                No local players found.
              </p>

              <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                Locally registered players will appear here.
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
                    </TableRow>
                  </TableHeader>

                  <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                    {filteredPlayers.map((player) => (
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
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
        </div>
      </div>
    </>
  );
}