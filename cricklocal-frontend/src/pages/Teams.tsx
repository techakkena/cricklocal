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
  importTeamsExcel,
  validateTeamExcel,
  type TeamImportResponse,
} from "../api/teamsApi";
import type { TeamResponse } from "../api/types";

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
        err instanceof Error ? err.message : "Unable to validate Excel file.",
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