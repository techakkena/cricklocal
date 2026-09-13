import { useEffect, useRef, useState } from "react";
import PageBreadcrumb from "../components/common/PageBreadCrumb";
import PageMeta from "../components/common/PageMeta";
import Badge from "../components/ui/badge/Badge";
import Button from "../components/ui/button/Button";
import { Modal } from "../components/ui/modal";
import {
  createPlayer,
  getPlayers,
  importPlayersExcel,
  validatePlayerExcel,
  type CreatePlayerRequest,
  type PlayerImportResponse,
} from "../api/playersApi";
import type {
  BattingStyle,
  BowlingStyle,
  PlayerResponse,
  PlayerRole,
} from "../api/types";

const initialForm: CreatePlayerRequest = {
  firstName: "",
  lastName: "",
  displayName: "",
  phone: "",
  battingStyle: "RIGHT_HAND",
  bowlingStyle: "NONE",
  role: "BATTER",
};

function formatRole(role: PlayerRole): string {
  switch (role) {
    case "ALL_ROUNDER":
      return "All Rounder";
    case "WICKET_KEEPER":
      return "Wicket Keeper";
    case "BATTER":
      return "Batter";
    case "BOWLER":
      return "Bowler";
    default:
      return role;
  }
}

function formatBattingStyle(style: BattingStyle): string {
  return style === "LEFT_HAND" ? "Left Hand" : "Right Hand";
}

function formatBowlingStyle(style: BowlingStyle): string {
  switch (style) {
    case "RIGHT_ARM_FAST":
      return "Right Arm Fast";
    case "RIGHT_ARM_MEDIUM":
      return "Right Arm Medium";
    case "RIGHT_ARM_OFF_SPIN":
      return "Right Arm Off Spin";
    case "RIGHT_ARM_LEG_SPIN":
      return "Right Arm Leg Spin";
    case "LEFT_ARM_FAST":
      return "Left Arm Fast";
    case "LEFT_ARM_MEDIUM":
      return "Left Arm Medium";
    case "LEFT_ARM_ORTHODOX":
      return "Left Arm Orthodox";
    case "LEFT_ARM_WRIST_SPIN":
      return "Left Arm Wrist Spin";
    case "NONE":
      return "None";
    default:
      return style;
  }
}

function getInitials(player: PlayerResponse): string {
  const first = player.firstName?.charAt(0) ?? "";
  const last = player.lastName?.charAt(0) ?? "";

  return `${first}${last}`.toUpperCase() || player.displayName.charAt(0);
}

export default function Players() {
  const [players, setPlayers] = useState<PlayerResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [form, setForm] = useState<CreatePlayerRequest>(initialForm);
  const [formError, setFormError] = useState("");
  const [creating, setCreating] = useState(false);

  const [isImportModalOpen, setIsImportModalOpen] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [validating, setValidating] = useState(false);
  const [importing, setImporting] = useState(false);
  const [importError, setImportError] = useState("");
  const [validation, setValidation] =
    useState<PlayerImportResponse | null>(null);

  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    loadPlayers();
  }, []);

  async function loadPlayers() {
    try {
      setLoading(true);
      setError("");

      const data = await getPlayers();
      setPlayers(data);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to load players.",
      );
    } finally {
      setLoading(false);
    }
  }

  function openCreateModal() {
    setForm(initialForm);
    setFormError("");
    setIsCreateModalOpen(true);
  }

  function closeCreateModal() {
    if (creating) {
      return;
    }

    setIsCreateModalOpen(false);
    setForm(initialForm);
    setFormError("");
  }

  function handleInputChange(
    field: keyof CreatePlayerRequest,
    value: string,
  ) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleCreatePlayer(
    event: React.FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    if (!form.firstName.trim()) {
      setFormError("First name is required.");
      return;
    }

    if (!form.displayName.trim()) {
      setFormError("Display name is required.");
      return;
    }

    if (!form.battingStyle) {
      setFormError("Batting style is required.");
      return;
    }

    if (!form.bowlingStyle) {
      setFormError("Bowling style is required.");
      return;
    }

    if (!form.role) {
      setFormError("Player role is required.");
      return;
    }

    try {
      setCreating(true);
      setFormError("");

      const createdPlayer = await createPlayer({
        firstName: form.firstName.trim(),
        lastName: form.lastName?.trim() || undefined,
        displayName: form.displayName.trim(),
        phone: form.phone?.trim() || undefined,
        battingStyle: form.battingStyle,
        bowlingStyle: form.bowlingStyle,
        role: form.role,
      });

      setPlayers((current) => [createdPlayer, ...current]);
      closeCreateModal();
    } catch (err) {
      setFormError(
        err instanceof Error ? err.message : "Failed to create player.",
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
    setIsImportModalOpen(true);
  }

  function closeImportModal() {
    if (validating || importing) {
      return;
    }

    setIsImportModalOpen(false);
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

      const response = await validatePlayerExcel(selectedFile);
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

  async function handleImportPlayers() {
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

      const response = await importPlayersExcel(selectedFile);

      setValidation(response);

      if (response.errors.length > 0) {
        setImportError(
          "The Excel file could not be imported because validation failed.",
        );
        return;
      }

      setIsImportModalOpen(false);
      resetImportState();

      await loadPlayers();
    } catch (err) {
      setImportError(
        err instanceof Error ? err.message : "Unable to import players.",
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
        title="Players | CricketLocal"
        description="Manage CricketLocal players and player profiles."
      />

      <PageBreadcrumb pageTitle="Players" />

      <div className="space-y-6">
        {/* Header */}
        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-xl font-semibold text-gray-800 dark:text-white/90">
              Players
            </h1>

            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Manage player profiles and their team memberships.
            </p>
          </div>

          <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
            <a
              href="/templates/CricketLocal_Players_Template.xlsx"
              download
              className="inline-flex h-10 items-center justify-center rounded-lg border border-gray-300 px-4 text-sm font-medium text-gray-700 transition hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
            >
              Download Excel Template
            </a>

            <button
              type="button"
              onClick={openImportModal}
              className="inline-flex h-10 items-center justify-center rounded-lg border border-brand-500 px-4 text-sm font-medium text-brand-600 transition hover:bg-brand-50 dark:text-brand-400 dark:hover:bg-brand-500/10"
            >
              Upload Excel
            </button>

            <Button size="sm" onClick={openCreateModal}>
              + Add Player
            </Button>
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="rounded-xl border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-900/40 dark:bg-error-900/10 dark:text-error-400">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <span>{error}</span>

              <button
                type="button"
                onClick={loadPlayers}
                className="font-medium underline hover:no-underline"
              >
                Retry
              </button>
            </div>
          </div>
        )}

        {/* Players table */}
        <div className="overflow-hidden rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="border-b border-gray-200 px-5 py-4 dark:border-gray-800">
            <div className="flex items-center justify-between gap-3">
              <div>
                <h2 className="font-medium text-gray-800 dark:text-white/90">
                  Player Directory
                </h2>

                <p className="text-sm text-gray-500 dark:text-gray-400">
                  {players.length} player
                  {players.length === 1 ? "" : "s"}
                </p>
              </div>
            </div>
          </div>

          {loading ? (
            <div className="px-5 py-12 text-center text-sm text-gray-500 dark:text-gray-400">
              Loading players...
            </div>
          ) : players.length === 0 ? (
            <div className="px-5 py-12 text-center">
              <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                No players found.
              </p>

              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Add your first player to get started.
              </p>

              <div className="mt-4">
                <Button size="sm" onClick={openCreateModal}>
                  + Add Player
                </Button>
              </div>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-[1050px] w-full">
                <thead>
                  <tr className="border-b border-gray-100 dark:border-gray-800">
                    <th className="px-5 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                      Player
                    </th>

                    <th className="px-5 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                      Role
                    </th>

                    <th className="px-5 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                      Batting
                    </th>

                    <th className="px-5 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                      Bowling
                    </th>

                    <th className="px-5 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                      Phone
                    </th>

                    <th className="px-5 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                      Teams
                    </th>

                    <th className="px-5 py-3 text-left text-xs font-medium uppercase tracking-wider text-gray-500 dark:text-gray-400">
                      Status
                    </th>
                  </tr>
                </thead>

                <tbody className="divide-y divide-gray-100 dark:divide-gray-800">
                  {players.map((player) => (
                    <tr
                      key={player.id}
                      className="transition hover:bg-gray-50 dark:hover:bg-white/[0.02]"
                    >
                      <td className="px-5 py-4">
                        <div className="flex items-center gap-3">
                          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-brand-50 text-sm font-semibold text-brand-600 dark:bg-brand-500/10 dark:text-brand-400">
                            {getInitials(player)}
                          </div>

                          <div className="min-w-0">
                            <p className="truncate font-medium text-gray-800 dark:text-white/90">
                              {player.displayName}
                            </p>

                            <p className="truncate text-xs text-gray-500 dark:text-gray-400">
                              {player.firstName}
                              {player.lastName
                                ? ` ${player.lastName}`
                                : ""}
                            </p>
                          </div>
                        </div>
                      </td>

                      <td className="px-5 py-4">
                        <span className="text-sm text-gray-700 dark:text-gray-300">
                          {formatRole(player.role)}
                        </span>
                      </td>

                      <td className="px-5 py-4">
                        <span className="text-sm text-gray-700 dark:text-gray-300">
                          {formatBattingStyle(player.battingStyle)}
                        </span>
                      </td>

                      <td className="px-5 py-4">
                        <span className="text-sm text-gray-700 dark:text-gray-300">
                          {formatBowlingStyle(player.bowlingStyle)}
                        </span>
                      </td>

                      <td className="px-5 py-4">
                        <span className="text-sm text-gray-600 dark:text-gray-400">
                          {player.phone || "—"}
                        </span>
                      </td>

                      <td className="px-5 py-4">
                        {player.teams.length === 0 ? (
                          <span className="text-sm text-gray-400">
                            No team
                          </span>
                        ) : (
                          <div className="flex flex-wrap gap-1.5">
                            {player.teams.map((team) => (
                              <span
                                key={team.teamId}
                                className="rounded-md bg-gray-100 px-2 py-1 text-xs font-medium text-gray-700 dark:bg-white/5 dark:text-gray-300"
                              >
                                {team.shortName || team.teamName}
                              </span>
                            ))}
                          </div>
                        )}
                      </td>

                      <td className="px-5 py-4">
                        <Badge
                          size="sm"
                          color={player.active ? "success" : "error"}
                        >
                          {player.active ? "Active" : "Inactive"}
                        </Badge>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Create Player Modal */}
      <Modal
        isOpen={isCreateModalOpen}
        onClose={closeCreateModal}
        className="max-w-[700px] p-5 lg:p-8"
      >
        <form onSubmit={handleCreatePlayer}>
          <div className="mb-6">
            <h2 className="text-xl font-semibold text-gray-800 dark:text-white/90">
              Add Player
            </h2>

            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Create a global player profile. Team membership can be managed
              separately.
            </p>
          </div>

          {formError && (
            <div className="mb-5 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-900/40 dark:bg-error-900/10 dark:text-error-400">
              {formError}
            </div>
          )}

          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
            {/* First Name */}
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                First Name <span className="text-error-500">*</span>
              </label>

              <input
                type="text"
                value={form.firstName}
                onChange={(event) =>
                  handleInputChange("firstName", event.target.value)
                }
                maxLength={50}
                required
                placeholder="Enter first name"
                className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition placeholder:text-gray-400 focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-gray-500"
              />
            </div>

            {/* Last Name */}
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                Last Name
              </label>

              <input
                type="text"
                value={form.lastName}
                onChange={(event) =>
                  handleInputChange("lastName", event.target.value)
                }
                maxLength={50}
                placeholder="Enter last name"
                className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition placeholder:text-gray-400 focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-gray-500"
              />
            </div>

            {/* Display Name */}
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                Display Name <span className="text-error-500">*</span>
              </label>

              <input
                type="text"
                value={form.displayName}
                onChange={(event) =>
                  handleInputChange("displayName", event.target.value)
                }
                maxLength={100}
                required
                placeholder="Example: Virat Kohli"
                className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition placeholder:text-gray-400 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>

            {/* Phone */}
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                Phone
              </label>

              <input
                type="tel"
                value={form.phone}
                onChange={(event) =>
                  handleInputChange("phone", event.target.value)
                }
                maxLength={20}
                placeholder="Enter phone number"
                className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition placeholder:text-gray-400 focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              />
            </div>

            {/* Role */}
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                Role <span className="text-error-500">*</span>
              </label>

              <select
                value={form.role}
                onChange={(event) =>
                  handleInputChange("role", event.target.value)
                }
                required
                className="h-11 w-full rounded-lg border border-gray-300 bg-white px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              >
                <option value="BATTER">Batter</option>
                <option value="BOWLER">Bowler</option>
                <option value="ALL_ROUNDER">All Rounder</option>
                <option value="WICKET_KEEPER">Wicket Keeper</option>
              </select>
            </div>

            {/* Batting Style */}
            <div>
              <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                Batting Style <span className="text-error-500">*</span>
              </label>

              <select
                value={form.battingStyle}
                onChange={(event) =>
                  handleInputChange("battingStyle", event.target.value)
                }
                required
                className="h-11 w-full rounded-lg border border-gray-300 bg-white px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              >
                <option value="RIGHT_HAND">Right Hand</option>
                <option value="LEFT_HAND">Left Hand</option>
              </select>
            </div>

            {/* Bowling Style */}
            <div className="sm:col-span-2">
              <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                Bowling Style <span className="text-error-500">*</span>
              </label>

              <select
                value={form.bowlingStyle}
                onChange={(event) =>
                  handleInputChange("bowlingStyle", event.target.value)
                }
                required
                className="h-11 w-full rounded-lg border border-gray-300 bg-white px-4 text-sm text-gray-800 outline-none transition focus:border-brand-500 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
              >
                <option value="NONE">None</option>
                <option value="RIGHT_ARM_FAST">Right Arm Fast</option>
                <option value="RIGHT_ARM_MEDIUM">Right Arm Medium</option>
                <option value="RIGHT_ARM_OFF_SPIN">
                  Right Arm Off Spin
                </option>
                <option value="RIGHT_ARM_LEG_SPIN">
                  Right Arm Leg Spin
                </option>
                <option value="LEFT_ARM_FAST">Left Arm Fast</option>
                <option value="LEFT_ARM_MEDIUM">Left Arm Medium</option>
                <option value="LEFT_ARM_ORTHODOX">
                  Left Arm Orthodox
                </option>
                <option value="LEFT_ARM_WRIST_SPIN">
                  Left Arm Wrist Spin
                </option>
              </select>
            </div>
          </div>

          {/* Actions */}
          <div className="mt-7 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
            <button
              type="button"
              onClick={closeCreateModal}
              disabled={creating}
              className="inline-flex items-center justify-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-3 text-sm font-medium text-gray-700 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03]"
            >
              Cancel
            </button>

            <button
              type="submit"
              disabled={creating}
              className="inline-flex items-center justify-center gap-2 rounded-lg bg-brand-500 px-4 py-3 text-sm font-medium text-white shadow-theme-xs transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:bg-brand-300 disabled:opacity-50"
            >
              {creating ? "Creating..." : "Create Player"}
            </button>
          </div>
        </form>
      </Modal>

      {/* Import Players Modal */}
      <Modal
        isOpen={isImportModalOpen}
        onClose={closeImportModal}
        className="max-w-[760px] p-5 sm:p-8"
      >
        <div className="pr-10">
          <h3 className="text-xl font-semibold text-gray-800 dark:text-white/90">
            Import Players from Excel
          </h3>

          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Upload the predefined CricketLocal Excel template and validate it
            before importing.
          </p>
        </div>

        <div className="mt-6 space-y-5">
          {/* File selection */}
          <div className="rounded-xl border border-dashed border-gray-300 p-5 dark:border-gray-700">
            <label
              htmlFor="player-excel-file"
              className="block text-sm font-medium text-gray-700 dark:text-gray-300"
            >
              Excel File
            </label>

            <input
              ref={fileInputRef}
              id="player-excel-file"
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

          {/* Import error */}
          {importError && (
            <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
              {importError}
            </div>
          )}

          {/* Validation result */}
          {validation && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
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
                    Ready to Import
                  </p>

                  <p className="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                    {validation.errors.length === 0
                      ? validation.totalRows
                      : 0}
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
                  Validation successful. {validation.totalRows} player
                  {validation.totalRows === 1 ? "" : "s"} ready to import.
                </div>
              ) : (
                <div className="rounded-xl border border-error-200 bg-error-50 p-4 dark:border-error-500/20 dark:bg-error-500/10">
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

          {/* Actions */}
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
              onClick={handleImportPlayers}
              disabled={!canImport}
              className="inline-flex h-11 items-center justify-center rounded-lg bg-brand-500 px-5 text-sm font-medium text-white transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {importing ? "Importing..." : "Import Players"}
            </button>
          </div>
        </div>
      </Modal>
    </>
  );
}