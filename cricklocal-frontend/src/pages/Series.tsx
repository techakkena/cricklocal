import { FormEvent, useEffect, useState } from "react";
import PageBreadcrumb from "../components/common/PageBreadCrumb";
import PageMeta from "../components/common/PageMeta";
import Badge from "../components/ui/badge/Badge";
import { Modal } from "../components/ui/modal";
import { getGlobalPlayers } from "../api/playersApi";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../components/ui/table";
import { getTeams } from "../api/teamsApi";
import {
  addPlayerToSeries,
  addTeamToSeries,
  createSeries,
  getSeries,
  getSeriesParticipants,
  removePlayerFromSeries,
  type SeriesParticipationResponse,
} from "../api/seriesApi";
import type {
  SeriesResponse,
  TeamResponse,
  PlayerResponse,
} from "../api/types";

function formatDate(dateValue: string | null) {
  if (!dateValue) {
    return "—";
  }

  const date = new Date(dateValue);

  if (Number.isNaN(date.getTime())) {
    return "—";
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "2-digit",
    month: "short",
    year: "numeric",
  }).format(date);
}

function getStatusBadgeColor(
  status: SeriesResponse["status"],
): "success" | "warning" | "error" | "info" {
  switch (status) {
    case "LIVE":
      return "success";
    case "COMPLETED":
      return "info";
    case "CANCELLED":
      return "error";
    default:
      return "warning";
  }
}

export default function Series() {
  const [series, setSeries] = useState<SeriesResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [participants, setParticipants] = useState<SeriesParticipationResponse[]>([]);
  const [participantsLoading, setParticipantsLoading] = useState(false);
  const [participantsError, setParticipantsError] = useState("");
  const [isManagePlayersOpen, setIsManagePlayersOpen] = useState(false);

  const [globalPlayers, setGlobalPlayers] = useState<PlayerResponse[]>([]);
  const [globalPlayersLoading, setGlobalPlayersLoading] = useState(false);
  const [globalPlayersError, setGlobalPlayersError] = useState("");

  const [selectedPlayerId, setSelectedPlayerId] = useState<number | "">("");
  const [addingPlayer, setAddingPlayer] = useState(false);
  const [addPlayerError, setAddPlayerError] = useState("");
  
  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState("");

  const [teams, setTeams] = useState<TeamResponse[]>([]);
  const [teamsLoading, setTeamsLoading] = useState(false);
  const [teamsError, setTeamsError] = useState("");
  const [isManageTeamsOpen, setIsManageTeamsOpen] = useState(false);
  const [selectedSeries, setSelectedSeries] =
    useState<SeriesResponse | null>(null);
  const [selectedTeamId, setSelectedTeamId] = useState("");
  const [addingTeam, setAddingTeam] = useState(false);
  const [addTeamError, setAddTeamError] = useState("");

  const [form, setForm] = useState({
    name: "",
    totalMatches: "",
    startDate: "",
    endDate: "",
  });

  async function loadSeries() {
    try {
      setLoading(true);
      setError("");

      const response = await getSeries();
      setSeries(response);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Unable to load series.",
      );
    } finally {
      setLoading(false);
    }
  }

  async function loadSeriesParticipants(seriesId: number) {
    try {
      setParticipantsLoading(true);
      setParticipantsError("");

      const response = await getSeriesParticipants(seriesId);
      setParticipants(response);
    } catch (err) {
      setParticipantsError(
        err instanceof Error
          ? err.message
          : "Unable to load series participants.",
      );
    } finally {
      setParticipantsLoading(false);
    }
  }

  async function loadGlobalPlayers() {
    try {
      setGlobalPlayersLoading(true);
      setGlobalPlayersError("");

      const response = await getGlobalPlayers();
      setGlobalPlayers(response);
    } catch (err) {
      setGlobalPlayersError(
        err instanceof Error
          ? err.message
          : "Unable to load global players.",
      );
    } finally {
      setGlobalPlayersLoading(false);
    }
  }

  async function loadTeams() {
    try {
      setTeamsLoading(true);
      setTeamsError("");

      const response = await getTeams();
      setTeams(response);
    } catch (err) {
      setTeamsError(
        err instanceof Error ? err.message : "Unable to load teams.",
      );
    } finally {
      setTeamsLoading(false);
    }
  }

  async function openManageTeamsModal(item: SeriesResponse) {
    setSelectedSeries(item);
    setSelectedTeamId("");
    setAddTeamError("");
    setIsManageTeamsOpen(true);

    await loadTeams();
  }

  async function openManagePlayersModal(item: SeriesResponse) {
    setSelectedSeries(item);
    setSelectedPlayerId("");
    setAddPlayerError("");
    setParticipantsError("");
    setParticipants([]);
    setIsManagePlayersOpen(true);

    await Promise.all([
      loadSeriesParticipants(item.id),
      loadGlobalPlayers(),
    ]);
  }

  async function handleAddPlayer() {
    if (!selectedSeries || !selectedPlayerId) {
      setAddPlayerError("Please select a player.");
      return;
    }

    try {
      setAddingPlayer(true);
      setAddPlayerError("");

      await addPlayerToSeries(selectedSeries.id, {
        playerId: Number(selectedPlayerId),
      });

      setSelectedPlayerId("");

      await loadSeriesParticipants(selectedSeries.id);
    } catch (err) {
      setAddPlayerError(
        err instanceof Error
          ? err.message
          : "Unable to add player to series.",
      );
    } finally {
      setAddingPlayer(false);
    }
  }

  async function handleRemovePlayer(playerId: number) {
    if (!selectedSeries) {
      return;
    }

    try {
      setParticipantsError("");

      await removePlayerFromSeries(selectedSeries.id, playerId);

      await loadSeriesParticipants(selectedSeries.id);
    } catch (err) {
      setParticipantsError(
        err instanceof Error
          ? err.message
          : "Unable to remove player from series.",
      );
    }
  }

  function closeManageTeamsModal() {
    if (addingTeam) {
      return;
    }

    setIsManageTeamsOpen(false);
    setSelectedSeries(null);
    setSelectedTeamId("");
    setAddTeamError("");
    setTeamsError("");
  }

  async function handleAddTeam() {
    if (!selectedSeries || !selectedTeamId) {
      setAddTeamError("Please select a team.");
      return;
    }

    try {
      setAddingTeam(true);
      setAddTeamError("");

      const updatedSeries = await addTeamToSeries(selectedSeries.id, {
        teamId: Number(selectedTeamId),
      });

      setSelectedSeries(updatedSeries);

      setSeries((current) =>
        current.map((item) =>
          item.id === updatedSeries.id ? updatedSeries : item,
        ),
      );

      setSelectedTeamId("");
    } catch (err) {
      setAddTeamError(
        err instanceof Error ? err.message : "Unable to add team to series.",
      );
    } finally {
      setAddingTeam(false);
    }
  }

  useEffect(() => {
    let cancelled = false;

    async function loadInitialSeries() {
      try {
        setLoading(true);
        setError("");

        const response = await getSeries();

        if (!cancelled) {
          setSeries(response);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error ? err.message : "Unable to load series.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadInitialSeries();

    return () => {
      cancelled = true;
    };
  }, []);

  function openCreateModal() {
    setCreateError("");
    setForm({
      name: "",
      totalMatches: "",
      startDate: "",
      endDate: "",
    });
    setIsCreateOpen(true);
  }

  function closeCreateModal() {
    if (creating) {
      return;
    }

    setIsCreateOpen(false);
    setCreateError("");
  }

  function handleFormChange(
    field: "name" | "totalMatches" | "startDate" | "endDate",
    value: string,
  ) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleCreateSeries(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const totalMatches = Number(form.totalMatches);

    if (!Number.isInteger(totalMatches) || totalMatches < 1) {
      setCreateError("Total matches must be at least 1.");
      return;
    }

    try {
      setCreating(true);
      setCreateError("");

      await createSeries({
        name: form.name.trim(),
        totalMatches,
        startDate: form.startDate,
        endDate: form.endDate || undefined,
      });

      setIsCreateOpen(false);
      setForm({
        name: "",
        totalMatches: "",
        startDate: "",
        endDate: "",
      });

      await loadSeries();
    } catch (err) {
      setCreateError(
        err instanceof Error ? err.message : "Unable to create series.",
      );
    } finally {
      setCreating(false);
    }
  }

  return (
    <>
      <PageMeta
        title="Series | CricketLocal"
        description="Manage CricketLocal cricket series."
      />

      <PageBreadcrumb pageTitle="Series" />

      <div className="space-y-6">
        <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-5 sm:flex-row sm:items-center sm:justify-between dark:border-gray-800">
            <div>
              <h3 className="font-semibold text-gray-800 dark:text-white/90">
                CricketLocal Series
              </h3>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                View and manage cricket series.
              </p>
            </div>

            <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
              <div className="text-sm text-gray-500 dark:text-gray-400 sm:mr-2">
                {loading
                  ? "Loading..."
                  : `${series.length} series${
                      series.length === 1 ? "" : "es"
                    }`}
              </div>

              <button
                type="button"
                onClick={openCreateModal}
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-brand-600"
              >
                + Create Series
              </button>
            </div>
          </div>

          {loading && (
            <div className="p-6 text-center text-sm text-gray-500 dark:text-gray-400">
              Loading series...
            </div>
          )}

          {!loading && error && (
            <div className="p-6">
              <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
                {error}
              </div>
            </div>
          )}

          {!loading && !error && series.length === 0 && (
            <div className="p-8 text-center">
              <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                No series found.
              </p>
              <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                CricketLocal series will appear here.
              </p>
            </div>
          )}

          {!loading && !error && series.length > 0 && (
            <div className="max-w-full overflow-x-auto">
              <Table>
                <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
                  <TableRow>
                    <TableCell
                      isHeader
                      className="px-5 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Series
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Matches
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
                      Dates
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Created
                    </TableCell>
                    <TableCell
                    isHeader
                    className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                    Actions
                    </TableCell>
                  </TableRow>
                </TableHeader>

                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {series.map((item) => (
                    <TableRow key={item.id}>
                      <TableCell className="px-5 py-4 text-start sm:px-6">
                        <div>
                          <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                            {item.name}
                          </span>
                          <span className="mt-1 block text-xs text-gray-500 dark:text-gray-400">
                            Series #{item.id}
                          </span>
                        </div>
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        {item.totalMatches}
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        {item.teams.length}
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start">
                        <Badge
                          size="sm"
                          color={getStatusBadgeColor(item.status)}
                        >
                          {item.status}
                        </Badge>
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        <div>
                          <span className="block">
                            {formatDate(item.startDate)}
                          </span>
                          {item.endDate && (
                            <span className="mt-1 block text-xs text-gray-400 dark:text-gray-500">
                              to {formatDate(item.endDate)}
                            </span>
                          )}
                        </div>
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        {formatDate(item.createdAt)}
                      </TableCell>
                      <TableCell>
                        <button
                            type="button"
                            onClick={() => openManageTeamsModal(item)}
                            className="rounded-lg border border-gray-300 px-3 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-gray-800"
                        >
                            Manage Teams
                        </button>
                        <button
                          type="button"
                          onClick={() => openManagePlayersModal(item)}
                          className="text-sm font-medium text-brand-500 hover:text-brand-600"
                        >
                          Manage Players
                        </button>
                    </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </div>
      </div>

      <Modal
        isOpen={isCreateOpen}
        onClose={closeCreateModal}
        className="max-w-[640px] p-5 sm:p-8"
      >
        <div className="pr-10">
          <h3 className="text-xl font-semibold text-gray-800 dark:text-white/90">
            Create Series
          </h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Create a new CricketLocal cricket series.
          </p>
        </div>

        <form onSubmit={handleCreateSeries} className="mt-6 space-y-5">
          {createError && (
            <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
              {createError}
            </div>
          )}

          <div>
            <label
              htmlFor="series-name"
              className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              Series Name
            </label>
            <input
              id="series-name"
              type="text"
              value={form.name}
              onChange={(event) =>
                handleFormChange("name", event.target.value)
              }
              maxLength={150}
              required
              placeholder="e.g. CMR ELITE SERIES"
              className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
            />
          </div>

          <div>
            <label
              htmlFor="series-total-matches"
              className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              Total Matches
            </label>
            <input
              id="series-total-matches"
              type="number"
              min={1}
              step={1}
              value={form.totalMatches}
              onChange={(event) =>
                handleFormChange("totalMatches", event.target.value)
              }
              required
              placeholder="e.g. 5"
              className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
            />
          </div>

          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
            <div>
              <label
                htmlFor="series-start-date"
                className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                Start Date
              </label>
              <input
                id="series-start-date"
                type="date"
                value={form.startDate}
                onChange={(event) =>
                  handleFormChange("startDate", event.target.value)
                }
                required
                className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>

            <div>
              <label
                htmlFor="series-end-date"
                className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
              >
                End Date
              </label>
              <input
                id="series-end-date"
                type="date"
                min={form.startDate || undefined}
                value={form.endDate}
                onChange={(event) =>
                  handleFormChange("endDate", event.target.value)
                }
                className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>
          </div>

          <div className="flex flex-col-reverse gap-3 pt-2 sm:flex-row sm:justify-end">
            <button
              type="button"
              onClick={closeCreateModal}
              disabled={creating}
              className="inline-flex h-11 items-center justify-center rounded-lg border border-gray-300 px-5 text-sm font-medium text-gray-700 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
            >
              Cancel
            </button>

            <button
              type="submit"
              disabled={creating}
              className="inline-flex h-11 items-center justify-center rounded-lg bg-brand-500 px-5 text-sm font-medium text-white transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {creating ? "Creating..." : "Create Series"}
            </button>
          </div>
        </form>
      </Modal>
      <Modal
        isOpen={isManageTeamsOpen}
        onClose={closeManageTeamsModal}
        className="max-w-[600px] p-5 lg:p-8"
        >
        <div>
            <h4 className="mb-2 text-title-sm font-semibold text-gray-800 dark:text-white/90">
            Manage Teams
            </h4>

            <p className="mb-6 text-sm text-gray-500 dark:text-gray-400">
            {selectedSeries
                ? `Manage participating teams for ${selectedSeries.name}.`
                : "Manage participating teams."}
            </p>

            {/* Current Teams */}
            <div className="mb-6">
            <h5 className="mb-3 text-sm font-medium text-gray-800 dark:text-white/90">
                Participating Teams
            </h5>

            {selectedSeries?.teams.length ? (
                <div className="space-y-2">
                {selectedSeries.teams.map((team) => (
                    <div
                    key={team.teamId}
                    className="rounded-lg border border-gray-200 px-4 py-3 dark:border-gray-700"
                    >
                    <span className="font-medium text-gray-800 dark:text-white/90">
                        {team.teamName}
                    </span>
                    <span className="ml-2 text-xs text-gray-500 dark:text-gray-400">
                        {team.shortName}
                    </span>
                    </div>
                ))}
                </div>
            ) : (
                <p className="text-sm text-gray-500 dark:text-gray-400">
                No teams have been added to this series yet.
                </p>
            )}
            </div>

            {/* Add Team */}
            <div>
            <label
                htmlFor="series-team"
                className="mb-2 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
                Add Team
            </label>

            <div className="flex flex-col gap-3 sm:flex-row">
                <select
                id="series-team"
                value={selectedTeamId}
                onChange={(event) => {
                    setSelectedTeamId(event.target.value);
                    setAddTeamError("");
                }}
                disabled={teamsLoading || addingTeam}
                className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none focus:border-brand-300 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 sm:flex-1"
                >
                <option value="">
                    {teamsLoading ? "Loading teams..." : "Select a team"}
                </option>

                {teams
                    .filter(
                    (team) =>
                        !selectedSeries?.teams.some(
                        (seriesTeam) => seriesTeam.teamId === team.id,
                        ),
                    )
                    .map((team) => (
                    <option key={team.id} value={team.id}>
                        {team.name} ({team.shortName})
                    </option>
                    ))}
                </select>

                <button
                type="button"
                onClick={handleAddTeam}
                disabled={addingTeam || teamsLoading || !selectedTeamId}
                className="h-11 rounded-lg bg-brand-500 px-5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
                >
                {addingTeam ? "Adding..." : "Add Team"}
                </button>
            </div>

            {teamsError && (
                <p className="mt-2 text-sm text-error-500">
                {teamsError}
                </p>
            )}

            {addTeamError && (
                <p className="mt-2 text-sm text-error-500">
                {addTeamError}
                </p>
            )}
            </div>

            <div className="mt-6 flex justify-end">
            <button
                type="button"
                onClick={closeManageTeamsModal}
                disabled={addingTeam}
                className="rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-gray-800"
            >
                Close
            </button>
            </div>
        </div>
    </Modal>
    <Modal
      isOpen={isManagePlayersOpen}
      onClose={() => {
        setIsManagePlayersOpen(false);
        setSelectedSeries(null);
        setParticipants([]);
        setParticipantsError("");
      }}
      className="max-w-[700px] p-6"
    >
      <div>
        <h3 className="text-xl font-semibold text-gray-800 dark:text-white/90">
          Manage Players
        </h3>

        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
          {selectedSeries?.name ?? "Series"}
        </p>

        <div className="mt-6">
          <div className="mb-6">
            <label className="mb-2 block text-sm font-medium text-gray-700 dark:text-gray-300">
              Add Global Player
            </label>

            <div className="flex gap-3">
              <select
                value={selectedPlayerId}
                onChange={(event) =>
                  setSelectedPlayerId(
                    event.target.value ? Number(event.target.value) : "",
                  )
                }
                disabled={globalPlayersLoading || addingPlayer}
                className="h-11 flex-1 rounded-lg border border-gray-300 bg-white px-4 text-sm text-gray-700 outline-none focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-300"
              >
                <option value="">
                  {globalPlayersLoading
                    ? "Loading global players..."
                    : "Select a global player"}
                </option>

                {globalPlayers.map((player) => (
                  <option key={player.id} value={player.id}>
                    {player.displayName}
                  </option>
                ))}
              </select>

              <button
                type="button"
                onClick={handleAddPlayer}
                disabled={addingPlayer || !selectedPlayerId}
                className="rounded-lg bg-brand-500 px-5 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {addingPlayer ? "Adding..." : "Add Player"}
              </button>
            </div>

            {globalPlayersError && (
              <p className="mt-2 text-sm text-red-500">
                {globalPlayersError}
              </p>
            )}

            {addPlayerError && (
              <p className="mt-2 text-sm text-red-500">
                {addPlayerError}
              </p>
            )}
          </div>
          <h4 className="mb-3 text-sm font-semibold text-gray-700 dark:text-gray-300">
            Series Participants
          </h4>

          {participantsLoading ? (
            <div className="rounded-lg border border-gray-200 p-6 text-center text-sm text-gray-500 dark:border-gray-800 dark:text-gray-400">
              Loading participants...
            </div>
          ) : participantsError ? (
            <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-600 dark:border-red-900/40 dark:bg-red-900/10 dark:text-red-400">
              {participantsError}
            </div>
          ) : participants.length === 0 ? (
            <div className="rounded-lg border border-gray-200 p-6 text-center text-sm text-gray-500 dark:border-gray-800 dark:text-gray-400">
              No players have been added to this series yet.
            </div>
          ) : (
            <div className="overflow-hidden rounded-lg border border-gray-200 dark:border-gray-800">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableCell
                      isHeader
                      className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400"
                    >
                      Player
                    </TableCell>

                    <TableCell
                      isHeader
                      className="px-4 py-3 text-left text-xs font-medium uppercase text-gray-500 dark:text-gray-400"
                    >
                      Status
                    </TableCell>

                    <TableCell
                      isHeader
                      className="px-4 py-3 text-right text-xs font-medium uppercase text-gray-500 dark:text-gray-400"
                    >
                      Action
                    </TableCell>
                  </TableRow>
                </TableHeader>

                <TableBody>
                  {participants.map((participant) => (
                    <TableRow key={participant.id}>
                      <TableCell className="px-4 py-3 text-sm font-medium text-gray-800 dark:text-white/90">
                        {participant.playerName}
                      </TableCell>

                      <TableCell className="px-4 py-3">
                        <Badge color="success" size="sm">
                          Active
                        </Badge>
                      </TableCell>

                      <TableCell className="px-4 py-3 text-right">
                        <button
                          type="button"
                          onClick={() => handleRemovePlayer(participant.playerId)}
                          className="text-sm font-medium text-red-500 hover:text-red-600"
                        >
                          Remove
                        </button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          )}
        </div>

        <div className="mt-6 flex justify-end">
          <button
            type="button"
            onClick={() => {
              setIsManagePlayersOpen(false);
              setSelectedSeries(null);
              setParticipants([]);
              setParticipantsError("");
            }}
            className="rounded-lg border border-gray-300 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
          >
            Close
          </button>
        </div>
      </div>
    </Modal>
    </>
  );
}