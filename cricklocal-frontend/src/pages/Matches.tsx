import { FormEvent, useEffect, useState } from "react";
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
  createMatch,
  getMatches,
} from "../api/matchesApi";
import { getSeries } from "../api/seriesApi";
import { getTeams } from "../api/teamsApi";
import type {
  MatchResponse,
  SeriesResponse,
  TeamResponse,
} from "../api/types";
import { Link } from "react-router";

function formatDateTime(dateValue: string | null) {
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
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

function getStatusBadgeColor(
  status: MatchResponse["status"],
): "success" | "warning" | "error" | "info" {
  switch (status) {
    case "LIVE":
      return "success";
    case "COMPLETED":
      return "info";
    case "ABANDONED":
      return "error";
    default:
      return "warning";
  }
}

function getTeamName(
  match: MatchResponse,
  side: "HOME" | "AWAY",
) {
  return (
    match.teams.find((team) => team.side === side)?.shortName ??
    match.teams.find((team) => team.side === side)?.teamName ??
    "—"
  );
}

export default function Matches() {
  const [matches, setMatches] = useState<MatchResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const [isCreateOpen, setIsCreateOpen] = useState(false);
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState("");

  const [teams, setTeams] = useState<TeamResponse[]>([]);
  const [series, setSeries] = useState<SeriesResponse[]>([]);
  const [referenceDataLoading, setReferenceDataLoading] = useState(false);
  const [referenceDataError, setReferenceDataError] = useState("");

  const [form, setForm] = useState({
    name: "",
    format: "T20",
    totalOvers: "20",
    maxPlayersPerTeam: "11",
    scheduledAt: "",
    venue: "",
    teamAId: "",
    teamBId: "",
    seriesId: "",
    matchNumber: "",
  });

  async function loadMatches() {
    try {
      setLoading(true);
      setError("");

      const response = await getMatches();
      setMatches(response);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Unable to load matches.",
      );
    } finally {
      setLoading(false);
    }
  }

  async function loadReferenceData() {
    try {
      setReferenceDataLoading(true);
      setReferenceDataError("");

      const [teamsResponse, seriesResponse] = await Promise.all([
        getTeams(),
        getSeries(),
      ]);

      setTeams(teamsResponse);
      setSeries(seriesResponse);
    } catch (err) {
      setReferenceDataError(
        err instanceof Error
          ? err.message
          : "Unable to load teams and series.",
      );
    } finally {
      setReferenceDataLoading(false);
    }
  }

  useEffect(() => {
    void loadMatches();
  }, []);

  function openCreateModal() {
    setCreateError("");
    setReferenceDataError("");

    setForm({
      name: "",
      format: "T20",
      totalOvers: "20",
      maxPlayersPerTeam: "11",
      scheduledAt: "",
      venue: "",
      teamAId: "",
      teamBId: "",
      seriesId: "",
      matchNumber: "",
    });

    setIsCreateOpen(true);
    void loadReferenceData();
  }

  function closeCreateModal() {
    if (creating) {
      return;
    }

    setIsCreateOpen(false);
    setCreateError("");
    setReferenceDataError("");
  }

  function handleFormChange(
    field: keyof typeof form,
    value: string,
  ) {
    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleCreateMatch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setCreateError("");

    if (!form.name.trim()) {
      setCreateError("Match name is required.");
      return;
    }

    if (!form.teamAId || !form.teamBId) {
      setCreateError("Both Team A and Team B are required.");
      return;
    }

    if (form.teamAId === form.teamBId) {
      setCreateError("Team A and Team B must be different.");
      return;
    }

    if (!form.scheduledAt) {
      setCreateError("Scheduled date and time are required.");
      return;
    }

    const totalOvers = Number(form.totalOvers);
    const maxPlayersPerTeam = Number(form.maxPlayersPerTeam);

    if (!Number.isInteger(totalOvers) || totalOvers < 1) {
      setCreateError("Total overs must be at least 1.");
      return;
    }

    if (
      !Number.isInteger(maxPlayersPerTeam) ||
      maxPlayersPerTeam < 1
    ) {
      setCreateError("Max players per team must be at least 1.");
      return;
    }

    try {
      setCreating(true);

      const scheduledAt = new Date(form.scheduledAt);

      if (Number.isNaN(scheduledAt.getTime())) {
        setCreateError("Scheduled date and time is invalid.");
        return;
      }

      await createMatch({
        name: form.name.trim(),
        format: form.format,
        totalOvers,
        maxPlayersPerTeam,
        scheduledAt: scheduledAt.toISOString(),
        venue: form.venue.trim() || undefined,
        teamAId: Number(form.teamAId),
        teamBId: Number(form.teamBId),
        seriesId: form.seriesId
          ? Number(form.seriesId)
          : undefined,
        matchNumber: form.matchNumber
          ? Number(form.matchNumber)
          : undefined,
      });

      setIsCreateOpen(false);
      setCreateError("");
      await loadMatches();
    } catch (err) {
      setCreateError(
        err instanceof Error
          ? err.message
          : "Unable to create match.",
      );
    } finally {
      setCreating(false);
    }
  }

  return (
    <>
      <PageMeta
        title="Matches | CricketLocal"
        description="Create, schedule, and manage CricketLocal matches."
      />

      <PageBreadcrumb pageTitle="Matches" />

      <div className="space-y-6">
        <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="flex flex-col gap-4 border-b border-gray-100 px-5 py-5 sm:flex-row sm:items-center sm:justify-between dark:border-gray-800">
            <div>
              <h3 className="font-semibold text-gray-800 dark:text-white/90">
                CricketLocal Matches
              </h3>

              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Create, schedule, and manage cricket matches.
              </p>
            </div>

            <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
              <div className="text-sm text-gray-500 dark:text-gray-400 sm:mr-2">
                {loading
                  ? "Loading..."
                  : `${matches.length} match${
                      matches.length === 1 ? "" : "es"
                    }`}
              </div>

              <button
                type="button"
                onClick={openCreateModal}
                className="inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-brand-600"
              >
                + Create Match
              </button>
            </div>
          </div>

          {loading && (
            <div className="p-6 text-center text-sm text-gray-500 dark:text-gray-400">
              Loading matches...
            </div>
          )}

          {!loading && error && (
            <div className="p-6">
              <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
                {error}
              </div>
            </div>
          )}

          {!loading && !error && matches.length === 0 && (
            <div className="p-8 text-center">
              <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                No matches found.
              </p>

              <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                Create your first CricketLocal match to get started.
              </p>

              <button
                type="button"
                onClick={openCreateModal}
                className="mt-4 inline-flex items-center justify-center rounded-lg bg-brand-500 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-brand-600"
              >
                + Create Match
              </button>
            </div>
          )}

          {!loading && !error && matches.length > 0 && (
            <div className="max-w-full overflow-x-auto">
              <Table>
                <TableHeader className="border-b border-gray-100 dark:border-white/[0.05]">
                  <TableRow>
                    <TableCell
                      isHeader
                      className="px-5 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Match
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
                      Format
                    </TableCell>

                    <TableCell
                      isHeader
                      className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Series
                    </TableCell>

                    <TableCell
                      isHeader
                      className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Scheduled
                    </TableCell>

                    <TableCell
                      isHeader
                      className="px-4 py-3 text-start font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                    >
                      Venue
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
                      Actions
                    </TableCell>

                  </TableRow>
                </TableHeader>

                <TableBody className="divide-y divide-gray-100 dark:divide-white/[0.05]">
                  {matches.map((match) => (
                    <TableRow key={match.id}>
                      <TableCell className="px-5 py-4 text-start sm:px-6">
                        <div>
                          <span className="block font-medium text-gray-800 text-theme-sm dark:text-white/90">
                            {match.name}
                          </span>

                          <span className="mt-1 block text-xs text-gray-500 dark:text-gray-400">
                            {match.matchNumber
                              ? `Match #${match.matchNumber}`
                              : `Match #${match.id}`}
                          </span>
                        </div>
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-700 text-theme-sm dark:text-gray-300">
                        <div className="whitespace-nowrap">
                          <span className="font-medium">
                            {getTeamName(match, "HOME")}
                          </span>

                          <span className="mx-2 text-gray-400">
                            vs
                          </span>

                          <span className="font-medium">
                            {getTeamName(match, "AWAY")}
                          </span>
                        </div>
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        <div>
                          <span className="block">
                            {match.format}
                          </span>

                          <span className="mt-1 block text-xs text-gray-400 dark:text-gray-500">
                            {match.totalOvers} overs ·{" "}
                            {match.maxPlayersPerTeam} players
                          </span>
                        </div>
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        {match.seriesName ?? "Standalone"}
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        {formatDateTime(match.scheduledAt)}
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start text-gray-500 text-theme-sm dark:text-gray-400">
                        {match.venue ?? "—"}
                      </TableCell>

                      <TableCell className="px-4 py-3 text-start">
                        <Badge
                          size="sm"
                          color={getStatusBadgeColor(match.status)}
                        >
                          {match.status}
                        </Badge>
                      </TableCell>
                      <TableCell className="px-4 py-4 text-start">
                      <Link
                        to={`/matches/${match.id}`}
                        className="inline-flex items-center justify-center rounded-lg border border-gray-300 px-3 py-2 text-xs font-medium text-gray-700 transition hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
                      >
                        Manage
                      </Link>
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
        className="max-w-3xl"
      >
        <form
          onSubmit={handleCreateMatch}
          className="flex max-h-[85vh] flex-col"
        >
          <div className="border-b border-gray-100 px-5 py-4 dark:border-gray-800 sm:px-6">
            <h4 className="text-lg font-semibold text-gray-800 dark:text-white/90">
              Create Match
            </h4>

            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Schedule a new CricketLocal match.
            </p>
          </div>

          <div className="flex-1 overflow-y-auto px-5 py-5 sm:px-6">
            {referenceDataLoading && (
              <div className="mb-4 rounded-lg border border-blue-200 bg-blue-50 px-4 py-3 text-sm text-blue-700 dark:border-blue-500/20 dark:bg-blue-500/10 dark:text-blue-400">
                Loading teams and series...
              </div>
            )}

            {referenceDataError && (
              <div className="mb-4 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
                {referenceDataError}
              </div>
            )}

            {createError && (
              <div className="mb-4 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
                {createError}
              </div>
            )}

            <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
              <div className="md:col-span-2">
                <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  Match Name <span className="text-error-500">*</span>
                </label>

                <input
                  type="text"
                  value={form.name}
                  onChange={(event) =>
                    handleFormChange("name", event.target.value)
                  }
                  placeholder="e.g. CMR Strikers vs CMR Royals"
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-gray-500"
                  disabled={creating}
                  required
                />
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  Format <span className="text-error-500">*</span>
                </label>

                <select
                  value={form.format}
                  onChange={(event) =>
                    handleFormChange("format", event.target.value)
                  }
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  disabled={creating}
                  required
                >
                  <option value="T10">T10</option>
                  <option value="T20">T20</option>
                  <option value="T20_BLITZ">T20 Blitz</option>
                  <option value="ODI">ODI</option>
                  <option value="TEST">Test</option>
                  <option value="CUSTOM">Custom</option>
                </select>
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  Total Overs <span className="text-error-500">*</span>
                </label>

                <input
                  type="number"
                  min="1"
                  value={form.totalOvers}
                  onChange={(event) =>
                    handleFormChange(
                      "totalOvers",
                      event.target.value,
                    )
                  }
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  disabled={creating}
                  required
                />
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  Max Players / Team{" "}
                  <span className="text-error-500">*</span>
                </label>

                <input
                  type="number"
                  min="1"
                  value={form.maxPlayersPerTeam}
                  onChange={(event) =>
                    handleFormChange(
                      "maxPlayersPerTeam",
                      event.target.value,
                    )
                  }
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  disabled={creating}
                  required
                />
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  Scheduled At <span className="text-error-500">*</span>
                </label>

                <input
                  type="datetime-local"
                  value={form.scheduledAt}
                  onChange={(event) =>
                    handleFormChange(
                      "scheduledAt",
                      event.target.value,
                    )
                  }
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  disabled={creating}
                  required
                />
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  Venue
                </label>

                <input
                  type="text"
                  value={form.venue}
                  onChange={(event) =>
                    handleFormChange("venue", event.target.value)
                  }
                  placeholder="e.g. CMR Cricket Ground"
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-gray-500"
                  disabled={creating}
                />
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  Team A <span className="text-error-500">*</span>
                </label>

                <select
                  value={form.teamAId}
                  onChange={(event) =>
                    handleFormChange("teamAId", event.target.value)
                  }
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  disabled={creating || referenceDataLoading}
                  required
                >
                  <option value="">Select Team A</option>

                  {teams.map((team) => (
                    <option key={team.id} value={team.id}>
                      {team.shortName
                        ? `${team.shortName} — ${team.name}`
                        : team.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  Team B <span className="text-error-500">*</span>
                </label>

                <select
                  value={form.teamBId}
                  onChange={(event) =>
                    handleFormChange("teamBId", event.target.value)
                  }
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  disabled={creating || referenceDataLoading}
                  required
                >
                  <option value="">Select Team B</option>

                  {teams.map((team) => (
                    <option key={team.id} value={team.id}>
                      {team.shortName
                        ? `${team.shortName} — ${team.name}`
                        : team.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  Series
                </label>

                <select
                  value={form.seriesId}
                  onChange={(event) =>
                    handleFormChange(
                      "seriesId",
                      event.target.value,
                    )
                  }
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                  disabled={creating || referenceDataLoading}
                >
                  <option value="">Standalone Match</option>

                  {series.map((item) => (
                    <option key={item.id} value={item.id}>
                      {item.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                  Match Number
                </label>

                <input
                  type="number"
                  min="1"
                  value={form.matchNumber}
                  onChange={(event) =>
                    handleFormChange(
                      "matchNumber",
                      event.target.value,
                    )
                  }
                  placeholder="Optional"
                  className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 text-sm text-gray-800 outline-none transition focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-gray-500"
                  disabled={creating}
                />
              </div>
            </div>
          </div>

          <div className="flex flex-col-reverse gap-3 border-t border-gray-100 px-5 py-4 sm:flex-row sm:justify-end dark:border-gray-800 sm:px-6">
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
              disabled={creating || referenceDataLoading}
              className="inline-flex h-11 items-center justify-center rounded-lg bg-brand-500 px-5 text-sm font-medium text-white transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {creating ? "Creating..." : "Create Match"}
            </button>
          </div>
        </form>
      </Modal>
    </>
  );
}