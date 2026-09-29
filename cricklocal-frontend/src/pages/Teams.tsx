import { FormEvent, useEffect, useRef, useState } from "react";
import PageBreadcrumb from "../components/common/PageBreadCrumb";
import PageMeta from "../components/common/PageMeta";
import Badge from "../components/ui/badge/Badge";
import { Modal } from "../components/ui/modal";
import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableRow,
} from "../components/ui/table";
import {
  createTeam,
  getTeams,
  getTeamPlayers,
  getTeamCaptain,
  addPlayerToTeam,
  removePlayerFromTeam,
  setTeamCaptain,
  importTeamsExcel,
  validateTeamExcel,
  type TeamImportResponse,
  type TeamRosterPlayerResponse,
  type TeamCaptainResponse,
} from "../api/teamsApi";
import { getPlayers } from "../api/playersApi";
import type { PlayerResponse, TeamResponse } from "../api/types";

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

export default function Teams() {
  const [teams, setTeams] = useState<TeamResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState("");

  const [isImportOpen, setIsImportOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [validating, setValidating] = useState(false);
  const [importing, setImporting] = useState(false);
  const [importError, setImportError] = useState("");
  const [validation, setValidation] = useState<TeamImportResponse | null>(
    null,
  );

  const [isPlayersOpen, setIsPlayersOpen] = useState(false);
  const [selectedTeam, setSelectedTeam] = useState<TeamResponse | null>(null);
  const [teamPlayers, setTeamPlayers] = useState<
    TeamRosterPlayerResponse[]
  >([]);
  const [allPlayers, setAllPlayers] = useState<PlayerResponse[]>([]);
  const [playersLoading, setPlayersLoading] = useState(false);
  const [refreshingRoster, setRefreshingRoster] = useState(false);
  const [playersError, setPlayersError] = useState("");
  const [addingPlayer, setAddingPlayer] = useState(false);
  const [settingCaptain, setSettingCaptain] = useState(false);
  const [removingPlayerId, setRemovingPlayerId] = useState<number | null>(null);
  const [playerActionError, setPlayerActionError] = useState("");
  const [selectedPlayerId, setSelectedPlayerId] = useState("");
  const [jerseyNumber, setJerseyNumber] = useState("");
  const [captain, setCaptain] = useState<TeamCaptainResponse | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  const [form, setForm] = useState({
    name: "",
    shortName: "",
    city: "",
  });

  async function loadTeams() {
    try {
      setLoading(true);
      setError("");

      const response = await getTeams();
      setTeams(response);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Unable to load teams.",
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    let cancelled = false;

    async function loadInitialTeams() {
      try {
        setLoading(true);
        setError("");

        const response = await getTeams();

        if (!cancelled) {
          setTeams(response);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error ? err.message : "Unable to load teams.",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadInitialTeams();

    return () => {
      cancelled = true;
    };
  }, []);

  async function openPlayersModal(team: TeamResponse) {
    try {
      setSelectedTeam(team);
      setIsPlayersOpen(true);
      setPlayersLoading(true);
      setPlayersError("");
      setPlayerActionError("");
      setSelectedPlayerId("");
      setJerseyNumber("");

      const [roster, players] = await Promise.all([
        getTeamPlayers(team.id),
        getPlayers(),
      ]);

      setTeamPlayers(roster);
      setAllPlayers(players);

      try {
        const currentCaptain = await getTeamCaptain(team.id);
        setCaptain(currentCaptain);
      } catch (err) {
        const message =
          err instanceof Error ? err.message : "";

        if (message === "Team captain is not assigned") {
          setCaptain(null);
        } else {
          throw err;
        }
      }
    } catch (err) {
      setPlayersError(
        err instanceof Error
          ? err.message
          : "Unable to load team players.",
      );
    } finally {
      setPlayersLoading(false);
    }
  }

  function closePlayersModal() {
    if (playersLoading || addingPlayer || settingCaptain) {
      return;
    }

    setIsPlayersOpen(false);
    setSelectedTeam(null);
    setTeamPlayers([]);
    setPlayersError("");
    setPlayerActionError("");
    setSelectedPlayerId("");
    setJerseyNumber("");
    setCaptain(null);
  }

  async function refreshRoster() {
    if (!selectedTeam || refreshingRoster) {
      return;
    }

    try {
      setRefreshingRoster(true);
      setPlayerActionError("");

      const roster = await getTeamPlayers(selectedTeam.id);
      setTeamPlayers(roster);

      if (captain) {
        const captainStillActive = roster.some(
          (player) =>
            player.playerId === captain.playerId && player.active,
        );

        if (!captainStillActive) {
          setCaptain(null);
        }
      }
    } catch (err) {
      setPlayerActionError(
        err instanceof Error ? err.message : "Unable to refresh team players.",
      );
    } finally {
      setRefreshingRoster(false);
    }
  }

  async function handleAddPlayerToTeam() {
    if (!selectedTeam || !selectedPlayerId) {
      setPlayerActionError("Please select a player.");
      return;
    }

    if (jerseyNumber.trim() === "") {
      setPlayerActionError("Please enter a jersey number.");
      return;
    }

    const number = Number(jerseyNumber);

    if (!Number.isInteger(number) || number < 0 || number > 99) {
      setPlayerActionError(
        "Jersey number must be a whole number from 0 to 99.",
      );
      return;
    }

    try {
      setAddingPlayer(true);
      setPlayerActionError("");

      await addPlayerToTeam(selectedTeam.id, Number(selectedPlayerId), {
        jerseyNumber: number,
      });

      await refreshRoster();

      setSelectedPlayerId("");
      setJerseyNumber("");
    } catch (err) {
        const message =
          err instanceof Error
            ? err.message
            : "Unable to add player to team.";

        setPlayerActionError(message);

        if (message.includes("Jersey number is already assigned")) {
          window.alert(
            `Duplicate jersey number\n\nJersey #${number} is already assigned to an active player.`,
          );
        }
      } finally {
      setAddingPlayer(false);
    }
  }

  async function handleSetCaptain(playerId: number) {
    if (!selectedTeam) {
      return;
    }

    try {
      setSettingCaptain(true);
      setPlayerActionError("");

      const response = await setTeamCaptain(selectedTeam.id, playerId);

      setCaptain(response);
      await refreshRoster();
    } catch (err) {
      setPlayerActionError(
        err instanceof Error ? err.message : "Unable to set team captain.",
      );
    } finally {
      setSettingCaptain(false);
    }
  }

  async function handleRemovePlayer(playerId: number, displayName: string) {
      if (!selectedTeam) {
        return;
      }

      const confirmed = window.confirm(
        `Remove ${displayName} from ${selectedTeam.name}?`,
      );

      if (!confirmed) {
        return;
      }

      try {
        setRemovingPlayerId(playerId);
        setPlayerActionError("");

        await removePlayerFromTeam(selectedTeam.id, playerId);

        if (captain?.playerId === playerId) {
          setCaptain(null);
        }

        await refreshRoster();
      } catch (err) {
        setPlayerActionError(
          err instanceof Error
            ? err.message
            : "Unable to remove player from team.",
        );
      } finally {
        setRemovingPlayerId(null);
      }
  }

  const assignedPlayerIds = new Set(teamPlayers.map((player) => player.playerId));
  const availablePlayers = allPlayers.filter(
    (player) => !assignedPlayerIds.has(player.id) && player.active,
  );

  const selectedPlayer = allPlayers.find(
  (player) => player.id === Number(selectedPlayerId),
);

const otherTeamsForSelectedPlayer = selectedPlayer?.teams.filter(
  (team) => team.teamId !== selectedTeam?.id,
) ?? [];

  function openCreateModal() {
    setCreateError("");
    setForm({
      name: "",
      shortName: "",
      city: "",
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
    field: "name" | "shortName" | "city",
    value: string,
  ) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleCreateTeam(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    try {
      setCreating(true);
      setCreateError("");

      await createTeam({
        name: form.name.trim(),
        shortName: form.shortName.trim(),
        city: form.city.trim() || undefined,
      });

      setIsCreateOpen(false);
      setForm({
        name: "",
        shortName: "",
        city: "",
      });

      await loadTeams();
    } catch (err) {
      setCreateError(
        err instanceof Error ? err.message : "Unable to create team.",
      );
    } finally {
      setCreating(false);
    }
  }

  function resetImportState() {
    setSelectedFile(null);
    setValidation(null);
    setImportError("");

    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  }

  function openImportModal() {
    resetImportState();
    setIsImportOpen(true);
  }

  function closeImportModal() {
    if (validating || importing) {
      return;
    }

    setIsImportOpen(false);
    resetImportState();
  }

  function handleFileChange(file: File | null) {
    setImportError("");
    setValidation(null);

    if (!file) {
      setSelectedFile(null);
      return;
    }

    if (!file.name.toLowerCase().endsWith(".xlsx")) {
      setSelectedFile(null);
      setImportError("Please select an .xlsx Excel file.");

      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }

      return;
    }

    setSelectedFile(file);
  }

  async function handleValidateImport() {
    if (!selectedFile) {
      setImportError("Please select an Excel file first.");
      return;
    }

    try {
      setValidating(true);
      setImportError("");

      const response = await validateTeamExcel(selectedFile);
      setValidation(response);
    } catch (err) {
      setValidation(null);
      setImportError(
        err instanceof Error
          ? err.message
          : "Unable to validate Excel file.",
      );
    } finally {
      setValidating(false);
    }
  }

  async function handleImportTeams() {
    if (!selectedFile || !validation) {
      return;
    }

    if (validation.errors.length > 0) {
      setImportError("Fix the validation errors before importing.");
      return;
    }

    try {
      setImporting(true);
      setImportError("");

      const response = await importTeamsExcel(selectedFile);
      setValidation(response);

      if (response.errors.length > 0) {
        setImportError(
          "The Excel file could not be imported because validation failed.",
        );
        return;
      }

      setIsImportOpen(false);
      resetImportState();

      await loadTeams();
    } catch (err) {
      setImportError(
        err instanceof Error ? err.message : "Unable to import teams.",
      );
    } finally {
      setImporting(false);
    }
  }

  const canImport =
    selectedFile !== null &&
    validation !== null &&
    validation.errors.length === 0 &&
    !validating &&
    !importing;

  return (
    <>
      <PageMeta
        title="Teams | CricketLocal"
        description="Manage CricketLocal teams."
      />

      <PageBreadcrumb pageTitle="Teams" />

      <div className="space-y-6">
        <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-5 sm:flex-row sm:items-center sm:justify-between dark:border-gray-800">
            <div>
              <h3 className="font-semibold text-gray-800 dark:text-white/90">
                CricketLocal Teams
              </h3>
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                View and manage registered cricket teams.
              </p>
            </div>

            <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
              <div className="text-sm text-gray-500 dark:text-gray-400 sm:mr-2">
                {loading
                  ? "Loading..."
                  : `${teams.length} team${teams.length === 1 ? "" : "s"}`}
              </div>

              <a
                href="/templates/CricketLocal_Teams_Template.xlsx"
                download
                className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 transition hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
              >
                Download Excel Template
              </a>

              <button
                type="button"
                onClick={openImportModal}
                className="inline-flex items-center justify-center rounded-lg border border-brand-500 px-4 py-2.5 text-sm font-medium text-brand-600 transition hover:bg-brand-50 dark:text-brand-400 dark:hover:bg-brand-500/10"
              >
                Upload Excel
              </button>

              <button
                type="button"
                onClick={openCreateModal}
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-brand-600"
              >
                + Create Team
              </button>
            </div>
          </div>

          {loading && (
            <div className="p-6 text-center text-sm text-gray-500 dark:text-gray-400">
              Loading teams...
            </div>
          )}

          {!loading && error && (
            <div className="p-6">
              <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
                {error}
              </div>
            </div>
          )}

          {!loading && !error && teams.length === 0 && (
            <div className="p-8 text-center">
              <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                No teams found.
              </p>
              <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                Registered CricketLocal teams will appear here.
              </p>
            </div>
          )}

          {!loading && !error && teams.length > 0 && (
            <div className="max-w-full overflow-x-auto">
              <Table>
                <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
                  <TableRow>
                    <TableCell
                      isHeader
                      className="px-5 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Team
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-5 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Short Name
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-5 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      City
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-5 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Status
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-5 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Created
                    </TableCell>
                    <TableCell
                      isHeader
                      className="px-5 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Actions
                    </TableCell>
                  </TableRow>
                </TableHeader>

                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {teams.map((team) => (
                    <TableRow key={team.id}>
                      <TableCell className="px-5 py-4 text-start sm:px-6">
                        <div>
                          <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                            {team.name}
                          </span>
                          <span className="mt-1 block text-xs text-gray-500 dark:text-gray-400">
                            Team #{team.id}
                          </span>
                        </div>
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        {team.shortName}
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        {team.city || "—"}
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        <Badge
                          size="sm"
                          color={team.active ? "success" : "error"}
                        >
                          {team.active ? "Active" : "Inactive"}
                        </Badge>
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        {formatCreatedDate(team.createdAt)}
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start">
                        <button
                          type="button"
                          onClick={() => openPlayersModal(team)}
                          className="inline-flex items-center justify-center rounded-lg border border-brand-500 px-3 py-2 text-xs font-medium text-brand-600 transition hover:bg-brand-50 dark:text-brand-400 dark:hover:bg-brand-500/10"
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
        isOpen={isPlayersOpen}
        onClose={closePlayersModal}
        className="max-h-[90vh] max-w-[900px] overflow-hidden p-5 sm:p-8"
      >
        <div className="flex max-h-[calc(90vh-2rem)] min-h-0 flex-col pr-10">
          <div className="shrink-0">
            <h3 className="text-xl font-semibold text-gray-800 dark:text-white/90">
              Manage Players
            </h3>
            {selectedTeam && (
              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                {selectedTeam.name} ({selectedTeam.shortName})
              </p>
            )}
          </div>

          <div className="mt-6 min-h-0 flex-1 overflow-y-auto pr-1">
            <div className="space-y-6">
              {playersError && (
                <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
                  {playersError}
                </div>
              )}
              {playerActionError && (
                <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
                  {playerActionError}
                </div>
              )}

              {playersLoading ? (
                <div className="py-10 text-center text-sm text-gray-500 dark:text-gray-400">
                  Loading team players...
                </div>
              ) : (
                <>
                  <div className="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
                    <div className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between">
                      <div>
                        <p className="text-xs text-gray-500 dark:text-gray-400">Current Captain</p>
                        <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
                          {captain ? captain.displayName : "Not assigned"}
                        </p>
                      </div>
                      {captain && (
                        <Badge size="sm" color="success">Jersey #{captain.jerseyNumber}</Badge>
                      )}
                    </div>
                  </div>

                  <div className="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
                    <h4 className="font-semibold text-gray-800 dark:text-white/90">Assign Player</h4>
                    <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                      Add an existing CricketLocal player to this team.
                    </p>
                    <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-[1fr_160px_auto]">
                      <div>
                        <label htmlFor="team-player-select" className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">Player</label>
                        <select id="team-player-select" value={selectedPlayerId} onChange={(event) => setSelectedPlayerId(event.target.value)} disabled={addingPlayer} className="h-11 w-full rounded-lg border border-gray-300 bg-white px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90">
                          <option value="">Select player</option>
                          {availablePlayers.map((player) => (
                            <option key={player.id} value={player.id}>{player.displayName}</option>
                          ))}
                        </select>
                      </div>
                      <div>
                        <label htmlFor="team-player-jersey" className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">Jersey #</label>
                        <input id="team-player-jersey" type="number" min="0" max="99" value={jerseyNumber} onChange={(event) => setJerseyNumber(event.target.value)} disabled={addingPlayer} placeholder="0–99" className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90" />
                      </div>
                      <div className="flex items-end">
                        <button type="button" onClick={handleAddPlayerToTeam} disabled={addingPlayer || removingPlayerId !== null || !selectedPlayerId || jerseyNumber.trim() === ""} className="h-11 w-full rounded-lg bg-brand-500 px-5 text-sm font-medium text-white transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50 sm:w-auto">
                          {addingPlayer ? "Adding..." : "Add Player"}
                        </button>
                      </div>
                    </div>
                    {availablePlayers.length === 0 && (
                      <p className="mt-3 text-xs text-gray-500 dark:text-gray-400">All active global players are already assigned to this team.</p>
                    )}
                    {otherTeamsForSelectedPlayer.length > 0 && (
                      <div className="mt-3 rounded-lg border border-warning-200 bg-warning-50 px-4 py-3 dark:border-warning-500/20 dark:bg-warning-500/10">
                        <p className="text-sm font-medium text-warning-700 dark:text-warning-400">⚠ Player already registered with another team</p>
                        <div className="mt-1 space-y-1">
                          {otherTeamsForSelectedPlayer.map((team) => (
                            <p key={team.teamId} className="text-xs text-warning-600 dark:text-warning-300">
                              {team.teamName} ({team.shortName}){team.jerseyNumber !== null ? ` — Jersey #${team.jerseyNumber}` : ""}
                            </p>
                          ))}
                        </div>
                      </div>
                    )}
                  </div>

                  <div>
                    <div className="mb-3 flex items-center justify-between">
                      <div>
                        <h4 className="font-semibold text-gray-800 dark:text-white/90">Team Roster</h4>
                        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">{teamPlayers.length} player{teamPlayers.length === 1 ? "" : "s"}</p>
                      </div>
                      <button type="button" onClick={refreshRoster} disabled={refreshingRoster || settingCaptain || addingPlayer || removingPlayerId !== null} className="rounded-lg border border-gray-300 px-3 py-2 text-xs font-medium text-gray-700 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]">
                        {refreshingRoster ? "Refreshing..." : "Refresh"}
                      </button>
                    </div>

                    {teamPlayers.length === 0 ? (
                      <div className="rounded-xl border border-dashed border-gray-300 p-8 text-center dark:border-gray-700">
                        <p className="text-sm font-medium text-gray-700 dark:text-gray-300">No players assigned yet.</p>
                      </div>
                    ) : (
                      <div className="max-h-[42vh] max-w-full overflow-auto rounded-xl border border-gray-200 dark:border-gray-800">
                        <Table>
                          <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
                            <TableRow>
                              <TableCell isHeader className="px-4 py-3 text-start text-xs font-medium text-gray-500 dark:text-gray-400">Player</TableCell>
                              <TableCell isHeader className="px-4 py-3 text-start text-xs font-medium text-gray-500 dark:text-gray-400">Jersey</TableCell>
                              <TableCell isHeader className="px-4 py-3 text-start text-xs font-medium text-gray-500 dark:text-gray-400">Status</TableCell>
                              <TableCell isHeader className="px-4 py-3 text-start text-xs font-medium text-gray-500 dark:text-gray-400">Captain</TableCell>
                              <TableCell isHeader className="px-4 py-3 text-start text-xs font-medium text-gray-500 dark:text-gray-400">Action</TableCell>
                            </TableRow>
                          </TableHeader>
                          <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                            {teamPlayers.map((player) => {
                              const isCaptain = captain?.playerId === player.playerId;
                              return (
                                <TableRow key={player.teamPlayerId}>
                                  <TableCell className="px-4 py-3 text-start"><div><span className="block text-sm font-medium text-gray-800 dark:text-white/90">{player.displayName}</span><span className="block text-xs text-gray-500 dark:text-gray-400">Player #{player.playerId}</span></div></TableCell>
                                  <TableCell className="px-4 py-3 text-sm text-gray-500 dark:text-gray-400">#{player.jerseyNumber}</TableCell>
                                  <TableCell className="px-4 py-3"><Badge size="sm" color={player.active ? "success" : "error"}>{player.active ? "Active" : "Inactive"}</Badge></TableCell>
                                  <TableCell className="px-4 py-3">{isCaptain ? <Badge size="sm" color="warning">Captain</Badge> : <span className="text-xs text-gray-500 dark:text-gray-400">—</span>}</TableCell>
                                  <TableCell className="px-4 py-3"><div className="flex flex-wrap gap-2">
                                    <button type="button" onClick={() => handleSetCaptain(player.playerId)} disabled={!player.active || isCaptain || settingCaptain || removingPlayerId !== null} className="rounded-lg border border-brand-500 px-3 py-2 text-xs font-medium text-brand-600 transition hover:bg-brand-50 disabled:cursor-not-allowed disabled:opacity-50 dark:text-brand-400 dark:hover:bg-brand-500/10">{isCaptain ? "Current Captain" : settingCaptain ? "Setting..." : "Make Captain"}</button>
                                    <button type="button" onClick={() => handleRemovePlayer(player.playerId, player.displayName)} disabled={!player.active || settingCaptain || addingPlayer || removingPlayerId !== null} className="rounded-lg border border-error-500 px-3 py-2 text-xs font-medium text-error-600 transition hover:bg-error-50 disabled:cursor-not-allowed disabled:opacity-50 dark:text-error-400 dark:hover:bg-error-500/10">{removingPlayerId === player.playerId ? "Removing..." : "Remove"}</button>
                                  </div></TableCell>
                                </TableRow>
                              );
                            })}
                          </TableBody>
                        </Table>
                      </div>
                    )}
                  </div>
                </>
              )}
            </div>
          </div>

          <div className="shrink-0 flex justify-end pt-4">
            <button type="button" onClick={closePlayersModal} disabled={playersLoading || addingPlayer || settingCaptain || removingPlayerId !== null || refreshingRoster} className="inline-flex h-11 items-center justify-center rounded-lg border border-gray-300 px-5 text-sm font-medium text-gray-700 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]">
              Close
            </button>
          </div>
        </div>
      </Modal>

      <Modal
        isOpen={isCreateOpen}
        onClose={closeCreateModal}
        className="max-w-[640px] p-5 sm:p-8"
      >
        <div className="pr-10">
          <h3 className="text-xl font-semibold text-gray-800 dark:text-white/90">
            Create Team
          </h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Register a new CricketLocal team.
          </p>
        </div>

        <form onSubmit={handleCreateTeam} className="mt-6 space-y-5">
          {createError && (
            <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
              {createError}
            </div>
          )}

          <div>
            <label
              htmlFor="team-name"
              className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              Team Name
            </label>
            <input
              id="team-name"
              type="text"
              value={form.name}
              onChange={(event) =>
                handleFormChange("name", event.target.value)
              }
              maxLength={100}
              required
              placeholder="e.g. CMR WARRIORS"
              className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
            />
          </div>

          <div>
            <label
              htmlFor="team-short-name"
              className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              Short Name
            </label>
            <input
              id="team-short-name"
              type="text"
              value={form.shortName}
              onChange={(event) =>
                handleFormChange("shortName", event.target.value)
              }
              maxLength={20}
              required
              placeholder="e.g. WARRIORS"
              className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
            />
          </div>

          <div>
            <label
              htmlFor="team-city"
              className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              City
            </label>
            <input
              id="team-city"
              type="text"
              value={form.city}
              onChange={(event) =>
                handleFormChange("city", event.target.value)
              }
              maxLength={100}
              placeholder="e.g. Vizianagaram"
              className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
            />
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
              {creating ? "Creating..." : "Create Team"}
            </button>
          </div>
        </form>
      </Modal>

      <Modal
        isOpen={isImportOpen}
        onClose={closeImportModal}
        className="max-w-[760px] p-5 sm:p-8"
      >
        <div className="pr-10">
          <h3 className="text-xl font-semibold text-gray-800 dark:text-white/90">
            Import Teams from Excel
          </h3>
          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Upload the predefined CricketLocal Excel template and validate it
            before importing.
          </p>
        </div>

        <div className="mt-6 space-y-5">
          <div className="rounded-xl border border-dashed border-gray-300 p-5 dark:border-gray-700">
            <label
              htmlFor="team-excel-file"
              className="block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              Excel File
            </label>

            <input
              ref={fileInputRef}
              id="team-excel-file"
              type="file"
              accept=".xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
              onChange={(event) =>
                handleFileChange(event.target.files?.[0] ?? null)
              }
              className="mt-3 block w-full cursor-pointer rounded-lg border border-gray-300 bg-white text-sm text-gray-700 file:mr-4 file:border-0 file:bg-gray-100 file:px-4 file:py-2.5 file:text-sm file:font-medium file:text-gray-700 hover:file:bg-gray-200 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-300 dark:file:bg-gray-800 dark:file:text-gray-300"
            />

            <p className="mt-2 text-xs text-gray-500 dark:text-gray-400">
              Only .xlsx files are supported.
            </p>

            {selectedFile && (
              <div className="mt-4 rounded-lg bg-gray-50 px-4 py-3 text-sm text-gray-700 dark:bg-white/[0.03] dark:text-gray-300">
                Selected:{" "}
                <span className="font-medium">{selectedFile.name}</span>
              </div>
            )}
          </div>

          {importError && (
            <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
              {importError}
            </div>
          )}

          {validation && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                <div className="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
                  <p className="text-xs text-gray-500 dark:text-gray-400">
                    Total Rows
                  </p>
                  <p className="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                    {validation.totalRows}
                  </p>
                </div>

                <div className="rounded-xl border border-gray-200 p-4 dark:border-gray-800">
                  <p className="text-xs text-gray-500 dark:text-gray-400">
                    Validation Errors
                  </p>
                  <p className="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                    {validation.errors.length}
                  </p>
                </div>
              </div>

              {validation.errors.length === 0 ? (
                <div className="rounded-xl border border-success-200 bg-success-50 p-4 text-sm text-success-700 dark:border-success-500/20 dark:bg-success-500/10 dark:text-success-400">
                  Validation successful. {validation.totalRows} team
                  {validation.totalRows === 1 ? "" : "s"} ready to import.
                </div>
              ) : (
                <div className="rounded-xl border border-error-200 bg-error-50 p-4 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
                  <p className="text-sm font-medium text-error-700 dark:text-error-400">
                    Please fix the following Excel errors:
                  </p>

                  <div className="mt-3 max-h-56 overflow-y-auto">
                    <div className="space-y-2">
                      {validation.errors.map((item, index) => (
                        <div
                          key={`${item.rowNumber}-${item.message}-${index}`}
                          className="rounded-lg bg-white/70 px-3 py-2 text-sm text-error-700 dark:bg-gray-900/40 dark:text-error-300"
                        >
                          <span className="font-medium">
                            Row {item.rowNumber}:
                          </span>{" "}
                          {item.message}
                        </div>
                      ))}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          <div className="flex flex-col-reverse gap-3 pt-2 sm:flex-row sm:justify-end">
            <button
              type="button"
              onClick={closeImportModal}
              disabled={validating || importing}
              className="inline-flex h-11 items-center justify-center rounded-lg border border-gray-300 px-5 text-sm font-medium text-gray-700 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
            >
              Cancel
            </button>

            <button
              type="button"
              onClick={handleValidateImport}
              disabled={!selectedFile || validating || importing}
              className="inline-flex h-11 items-center justify-center rounded-lg border border-brand-500 px-5 text-sm font-medium text-brand-600 transition hover:bg-brand-50 disabled:cursor-not-allowed disabled:opacity-50 dark:text-brand-400 dark:hover:bg-brand-500/10"
            >
              {validating ? "Validating..." : "Validate Excel"}
            </button>

            <button
              type="button"
              onClick={handleImportTeams}
              disabled={!canImport}
              className="inline-flex h-11 items-center justify-center rounded-lg bg-brand-500 px-5 text-sm font-medium text-white transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {importing ? "Importing..." : "Import Teams"}
            </button>
          </div>
        </div>
      </Modal>
    </>
  );
}