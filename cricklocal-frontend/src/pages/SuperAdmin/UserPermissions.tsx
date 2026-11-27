import { useEffect, useMemo, useState } from "react";
import {
  PermissionAction,
  PermissionItemResponse,
  UserPermissionResponse,
} from "../../api/types";

import {
  getPermissionUsers,
  getUserPermissions,
  updateUserPermission,
  resetUserPermissions,
} from "../../api/permissionsApi";

const ACTIONS: PermissionAction[] = [
  "VIEW",
  "CREATE",
  "EDIT",
  "DELETE",
  "APPROVE",
];

type PendingChanges = Record<string, boolean | null>;

const MODULE_GROUPS: Record<string, string[]> = {
  General: ["DASHBOARD", "SETTINGS"],
  "Players & Teams": [
    "TEAMS",
    "PLAYERS",
    "GLOBAL_PLAYERS",
    "LOCAL_PLAYERS",
    "PLAYER_REGISTRATIONS",
  ],
  Competition: [
    "SERIES",
    "MATCHES",
    "MATCH_PREPARATION",
  ],
  Scoring: [
    "MATCH_CENTER",
    "LIVE_SCORING",
    "SCORECARDS",
  ],
  Analytics: [
    "STATISTICS",
    "LEADERBOARDS",
    "HISTORY",
  ],
};

export default function UserPermissions() {
  const [users, setUsers] = useState<UserPermissionResponse[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);

  const [permissions, setPermissions] =
    useState<UserPermissionResponse | null>(null);

  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState<
    "ALL" | "ADMIN" | "PLAYER"
  >("ALL");

  const [loadingUsers, setLoadingUsers] = useState(true);
  const [loadingPermissions, setLoadingPermissions] = useState(false);
  const [saving, setSaving] = useState(false);
  const [resetting, setResetting] = useState(false);

  const [pendingChanges, setPendingChanges] =
    useState<PendingChanges>({});

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadUsers();
  }, []);

  async function loadUsers() {
    try {
      setLoadingUsers(true);
      setError("");

      const data = await getPermissionUsers();

      setUsers(data);

      if (data.length > 0) {
        setSelectedUserId(data[0].userId);
      }
    } catch (err) {
      console.error(err);
      setError("Failed to load users.");
    } finally {
      setLoadingUsers(false);
    }
  }

  useEffect(() => {
    if (selectedUserId === null) {
      setPermissions(null);
      return;
    }

    loadSelectedUser(selectedUserId);
  }, [selectedUserId]);

  async function loadSelectedUser(userId: number) {
    try {
      setLoadingPermissions(true);
      setError("");
      setMessage("");
      setPendingChanges({});

      const data = await getUserPermissions(userId);

      setPermissions(data);
    } catch (err) {
      console.error(err);
      setError("Failed to load user permissions.");
    } finally {
      setLoadingPermissions(false);
    }
  }

  const filteredUsers = useMemo(() => {
    const value = search.trim().toLowerCase();

    return users.filter((user) => {
      const matchesRole =
        roleFilter === "ALL" || user.role === roleFilter;

      const matchesSearch =
        !value ||
        user.displayName.toLowerCase().includes(value) ||
        user.email.toLowerCase().includes(value) ||
        user.role.toLowerCase().includes(value);

      return matchesRole && matchesSearch;
    });
  }, [users, search, roleFilter]);

  
  const hasChanges = Object.keys(pendingChanges).length > 0;

  const summary = useMemo(() => {
    if (!permissions) {
      return {
        allowed: 0,
        denied: 0,
        inherited: 0,
        overrides: 0,
      };
    }

    let allowed = 0;
    let denied = 0;
    let inherited = 0;
    let overrides = 0;

    permissions.permissions.forEach((permission) => {
      const key = permissionKey(permission);

      const effective = hasPendingChange(key)
        ? pendingChanges[key] === null
          ? permission.roleDefault === true
          : pendingChanges[key] === true
        : permission.effective;

      if (effective) {
        allowed++;
      } else {
        denied++;
      }

      if (permission.override === null) {
        inherited++;
      } else {
        overrides++;
      }
    });

    return {
      allowed,
      denied,
      inherited,
      overrides,
    };
  }, [permissions, pendingChanges]);

  function permissionKey(permission: PermissionItemResponse) {
    return `${permission.moduleKey}:${permission.action}`;
  }

  function hasPendingChange(key: string) {
    return Object.prototype.hasOwnProperty.call(
      pendingChanges,
      key
    );
  }

  function getDisplayedPermission(
    permission: PermissionItemResponse
  ) {
    const key = permissionKey(permission);

    if (!hasPendingChange(key)) {
      return permission;
    }

    const pending = pendingChanges[key];

    return {
      ...permission,
      effective:
        pending === null
          ? permission.roleDefault === true
          : pending === true,
      override: pending,
    };
  }

  function changePermission(
    permission: PermissionItemResponse,
    allowed: boolean | null
  ) {
    const key = permissionKey(permission);

    const originalOverride = permission.override;

    if (allowed === originalOverride) {
      setPendingChanges((current) => {
        const next = { ...current };
        delete next[key];
        return next;
      });

      return;
    }

    setPendingChanges((current) => ({
      ...current,
      [key]: allowed,
    }));

    setMessage("");
    setError("");
  }

  function togglePermission(permission: PermissionItemResponse) {
    const displayed = getDisplayedPermission(permission);

    changePermission(
      permission,
      displayed.effective ? false : true
    );
  }

  function resetPermission(permission: PermissionItemResponse) {
    changePermission(permission, null);
  }

  async function saveChanges() {
    if (!permissions || !hasChanges) {
      return;
    }

    try {
      setSaving(true);
      setError("");
      setMessage("");

      let updated = permissions;

      for (const permission of permissions.permissions) {
        const key = permissionKey(permission);

        if (!hasPendingChange(key)) {
          continue;
        }

        updated = await updateUserPermission(
          permissions.userId,
          {
            moduleKey: permission.moduleKey,
            action: permission.action,
            allowed: pendingChanges[key],
          }
        );
      }

      setPermissions(updated);

      setUsers((current) =>
        current.map((user) =>
          user.userId === updated.userId ? updated : user
        )
      );

      setPendingChanges({});
      setMessage("Permission changes saved successfully.");
    } catch (err) {
      console.error(err);
      setError("Failed to save permission changes.");
    } finally {
      setSaving(false);
    }
  }

  function discardChanges() {
    setPendingChanges({});
    setMessage("");
    setError("");
  }

  async function resetAll() {
    if (!permissions) {
      return;
    }

    const confirmed = window.confirm(
      `Reset all permissions for ${permissions.displayName} to role defaults?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setResetting(true);
      setError("");
      setMessage("");
      setPendingChanges({});

      await resetUserPermissions(permissions.userId);

      await loadSelectedUser(permissions.userId);

      setMessage(
        "All permissions have been reset to role defaults."
      );
    } catch (err) {
      console.error(err);
      setError("Failed to reset permissions.");
    } finally {
      setResetting(false);
    }
  }

  function getPermission(
    moduleKey: string,
    action: PermissionAction
  ) {
    return permissions?.permissions.find(
      (permission) =>
        permission.moduleKey === moduleKey &&
        permission.action === action
    );
  }

  function getModules() {
    if (!permissions) {
      return [];
    }

    const uniqueModules = Array.from(
      new Map(
        permissions.permissions.map((permission) => [
          permission.moduleKey,
          permission,
        ])
      ).values()
    );

    return uniqueModules;
  }

  function getGroupedModules() {
    const modules = getModules();

    const result: {
      group: string;
      modules: PermissionItemResponse[];
    }[] = [];

    Object.entries(MODULE_GROUPS).forEach(
      ([group, moduleKeys]) => {
        const groupModules = moduleKeys
          .map((key) =>
            modules.find(
              (module) => module.moduleKey === key
            )
          )
          .filter(Boolean) as PermissionItemResponse[];

        if (groupModules.length > 0) {
          result.push({
            group,
            modules: groupModules,
          });
        }
      }
    );

    const groupedKeys = Object.values(MODULE_GROUPS).flat();

    const remaining = modules.filter(
      (module) => !groupedKeys.includes(module.moduleKey)
    );

    if (remaining.length > 0) {
      result.push({
        group: "Other",
        modules: remaining,
      });
    }

    return result;
  }

  const adminCount = users.filter(
    (user) => user.role === "ADMIN"
  ).length;

  const playerCount = users.filter(
    (user) => user.role === "PLAYER"
  ).length;

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <div className="mb-2 flex items-center gap-2 text-sm text-gray-500 dark:text-gray-400">
            <span>Super Admin</span>
            <span>/</span>
            <span>User Permissions</span>
          </div>

          <h1 className="text-2xl font-semibold text-gray-900 dark:text-white">
            User Permissions
          </h1>

          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Manage module access and permissions for ADMIN and PLAYER
            users.
          </p>
        </div>

        {hasChanges && (
          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={discardChanges}
              disabled={saving}
              className="rounded-lg border border-gray-300 bg-white px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:bg-gray-900 dark:text-gray-300 dark:hover:bg-white/[0.04]"
            >
              Discard
            </button>

            <button
              type="button"
              onClick={saveChanges}
              disabled={saving}
              className="rounded-lg bg-brand-500 px-5 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {saving ? "Saving Changes..." : "Save Changes"}
            </button>
          </div>
        )}
      </div>

      {/* Messages */}
      {message && (
        <div className="flex items-center justify-between rounded-lg border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-700 dark:border-green-900/40 dark:bg-green-900/10 dark:text-green-400">
          <span>{message}</span>

          <button
            type="button"
            onClick={() => setMessage("")}
            className="text-xs font-medium hover:underline"
          >
            Dismiss
          </button>
        </div>
      )}

      {error && (
        <div className="flex items-center justify-between rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 dark:border-red-900/40 dark:bg-red-900/10 dark:text-red-400">
          <span>{error}</span>

          <button
            type="button"
            onClick={() => setError("")}
            className="text-xs font-medium hover:underline"
          >
            Dismiss
          </button>
        </div>
      )}

      {/* Summary Cards */}
      <div className="grid grid-cols-2 gap-4 xl:grid-cols-4">
        <SummaryCard
          label="Total Users"
          value={users.length}
          description="Permission-managed users"
        />

        <SummaryCard
          label="Administrators"
          value={adminCount}
          description="ADMIN accounts"
        />

        <SummaryCard
          label="Players"
          value={playerCount}
          description="PLAYER accounts"
        />

        <SummaryCard
          label="Pending Changes"
          value={Object.keys(pendingChanges).length}
          description="Unsaved permissions"
          highlight={hasChanges}
        />
      </div>

      {/* Main Layout */}
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
        {/* Users Panel */}
        <div className="rounded-xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
          <div className="border-b border-gray-200 p-5 dark:border-gray-800">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                  Users
                </h2>

                <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                  Select a user to manage access.
                </p>
              </div>

              <span className="rounded-full bg-gray-100 px-2.5 py-1 text-xs font-semibold text-gray-600 dark:bg-gray-800 dark:text-gray-300">
                {filteredUsers.length}
              </span>
            </div>

            {/* Search */}
            <div className="relative mt-4">
              <input
                type="text"
                value={search}
                onChange={(event) =>
                  setSearch(event.target.value)
                }
                placeholder="Search users..."
                className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm outline-none transition focus:border-brand-500 focus:ring-2 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white dark:placeholder:text-gray-500"
              />
            </div>

            {/* Role Filters */}
            <div className="mt-3 flex gap-2">
              {(["ALL", "ADMIN", "PLAYER"] as const).map(
                (role) => (
                  <button
                    key={role}
                    type="button"
                    onClick={() => setRoleFilter(role)}
                    className={`rounded-full px-3 py-1.5 text-xs font-medium transition ${
                      roleFilter === role
                        ? "bg-brand-500 text-white"
                        : "bg-gray-100 text-gray-600 hover:bg-gray-200 dark:bg-gray-800 dark:text-gray-300 dark:hover:bg-gray-700"
                    }`}
                  >
                    {role === "ALL" ? "All" : role}
                  </button>
                )
              )}
            </div>
          </div>

          <div className="max-h-[650px] overflow-y-auto p-3">
            {loadingUsers ? (
              <div className="px-3 py-8 text-center text-sm text-gray-500">
                Loading users...
              </div>
            ) : filteredUsers.length === 0 ? (
              <div className="px-3 py-8 text-center text-sm text-gray-500">
                No users found.
              </div>
            ) : (
              <div className="space-y-2">
                {filteredUsers.map((user) => {
                  const selected =
                    selectedUserId === user.userId;

                  return (
                    <button
                      key={user.userId}
                      type="button"
                      onClick={() =>
                        setSelectedUserId(user.userId)
                      }
                      className={`w-full rounded-xl border p-3 text-left transition ${
                        selected
                          ? "border-brand-500 bg-brand-50 dark:border-brand-500 dark:bg-brand-500/10"
                          : "border-gray-200 bg-white hover:border-gray-300 hover:bg-gray-50 dark:border-gray-800 dark:bg-transparent dark:hover:bg-white/[0.04]"
                      }`}
                    >
                      <div className="flex items-start gap-3">
                        <div
                          className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-sm font-semibold ${
                            selected
                              ? "bg-brand-500 text-white"
                              : "bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300"
                          }`}
                        >
                          {getInitials(user.displayName)}
                        </div>

                        <div className="min-w-0 flex-1">
                          <div className="truncate text-sm font-semibold text-gray-900 dark:text-white">
                            {user.displayName}
                          </div>

                          <div className="mt-0.5 truncate text-xs text-gray-500 dark:text-gray-400">
                            {user.email}
                          </div>

                          <div className="mt-2 flex items-center gap-2">
                            <RoleBadge role={user.role} />

                            <span className="text-[11px] text-gray-400">
                              ID: {user.userId}
                            </span>
                          </div>
                        </div>
                      </div>
                    </button>
                  );
                })}
              </div>
            )}
          </div>
        </div>

        {/* Permission Panel */}
        <div className="min-w-0">
          {loadingPermissions ? (
            <div className="rounded-xl border border-gray-200 bg-white p-12 text-center dark:border-gray-800 dark:bg-white/[0.03]">
              <div className="text-sm text-gray-500 dark:text-gray-400">
                Loading permissions...
              </div>
            </div>
          ) : !permissions ? (
            <div className="rounded-xl border border-gray-200 bg-white p-12 text-center dark:border-gray-800 dark:bg-white/[0.03]">
              <div className="text-sm text-gray-500 dark:text-gray-400">
                Select a user to manage permissions.
              </div>
            </div>
          ) : (
            <div className="space-y-6">
              {/* Selected User */}
              <div className="rounded-xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
                <div className="flex flex-col gap-5 p-5 lg:flex-row lg:items-center lg:justify-between">
                  <div className="flex items-center gap-4">
                    <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-brand-500 text-lg font-semibold text-white">
                      {getInitials(permissions.displayName)}
                    </div>

                    <div>
                      <div className="flex flex-wrap items-center gap-2">
                        <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                          {permissions.displayName}
                        </h2>

                        <RoleBadge role={permissions.role} />
                      </div>

                      <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                        {permissions.email}
                      </p>

                      <div className="mt-2 flex flex-wrap items-center gap-3 text-xs text-gray-400">
                        <span>
                          User ID: {permissions.userId}
                        </span>

                        <span>•</span>

                        <span>
                          {permissions.role === "ADMIN"
                            ? "Administrator"
                            : "Player"}{" "}
                          role defaults
                        </span>
                      </div>
                    </div>
                  </div>

                  <button
                    type="button"
                    onClick={resetAll}
                    disabled={resetting || saving}
                    className="rounded-lg border border-red-200 bg-white px-4 py-2.5 text-sm font-medium text-red-600 transition hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-red-900/40 dark:bg-transparent dark:text-red-400 dark:hover:bg-red-900/10"
                  >
                    {resetting
                      ? "Resetting..."
                      : "Reset All Permissions"}
                  </button>
                </div>
              </div>

              {/* Permission Summary */}
              <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
                <PermissionSummaryCard
                  label="Allowed"
                  value={summary.allowed}
                  description="Effective access"
                  type="allowed"
                />

                <PermissionSummaryCard
                  label="Denied"
                  value={summary.denied}
                  description="Effective restrictions"
                  type="denied"
                />

                <PermissionSummaryCard
                  label="Inherited"
                  value={summary.inherited}
                  description="Using role defaults"
                  type="inherited"
                />

                <PermissionSummaryCard
                  label="Overrides"
                  value={summary.overrides}
                  description="Explicit user settings"
                  type="override"
                />
              </div>

              {/* Legend */}
              <div className="rounded-xl border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-white/[0.03]">
                <div className="flex flex-wrap items-center gap-5 text-xs text-gray-500 dark:text-gray-400">
                  <LegendItem
                    label="Allowed"
                    className="bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                  />

                  <LegendItem
                    label="Denied"
                    className="bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400"
                  />

                  <LegendItem
                    label="Inherited"
                    className="bg-gray-100 text-gray-600 dark:bg-gray-800 dark:text-gray-300"
                  />

                  <LegendItem
                    label="Override"
                    className="bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400"
                  />
                </div>
              </div>

              {/* Permission Groups */}
              {getGroupedModules().map((group) => (
                <div
                  key={group.group}
                  className="rounded-xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
                >
                  <div className="border-b border-gray-200 px-5 py-4 dark:border-gray-800">
                    <h3 className="text-sm font-semibold text-gray-900 dark:text-white">
                      {group.group}
                    </h3>

                    <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                      Configure access for {group.group.toLowerCase()}.
                    </p>
                  </div>

                  <div className="overflow-x-auto">
                    <table className="min-w-[900px] w-full">
                      <thead>
                        <tr className="border-b border-gray-200 bg-gray-50/70 dark:border-gray-800 dark:bg-white/[0.02]">
                          <th className="px-5 py-3 text-left text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
                            Module
                          </th>

                          {ACTIONS.map((action) => (
                            <th
                              key={action}
                              className="px-3 py-3 text-center text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400"
                            >
                              {action}
                            </th>
                          ))}
                        </tr>
                      </thead>

                      <tbody>
                        {group.modules.map(
                          (modulePermission) => (
                            <tr
                              key={modulePermission.moduleKey}
                              className="border-b border-gray-100 last:border-b-0 dark:border-gray-800"
                            >
                              <td className="px-5 py-4">
                                <div className="text-sm font-semibold text-gray-900 dark:text-white">
                                  {
                                    modulePermission.moduleName
                                  }
                                </div>

                                <div className="mt-0.5 text-[11px] text-gray-400">
                                  {
                                    modulePermission.moduleKey
                                  }
                                </div>
                              </td>

                              {ACTIONS.map((action) => {
                                const permission =
                                  getPermission(
                                    modulePermission.moduleKey,
                                    action
                                  );

                                if (!permission) {
                                  return (
                                    <td
                                      key={action}
                                      className="px-3 py-4 text-center text-gray-300 dark:text-gray-600"
                                    >
                                      —
                                    </td>
                                  );
                                }

                                const displayed =
                                  getDisplayedPermission(
                                    permission
                                  );

                                const key =
                                  permissionKey(permission);

                                const pending =
                                  hasPendingChange(key);

                                return (
                                  <td
                                    key={action}
                                    className="px-3 py-4 text-center"
                                  >
                                    <PermissionControl
                                      permission={displayed}
                                      pending={pending}
                                      disabled={saving}
                                      onToggle={() =>
                                        togglePermission(
                                          permission
                                        )
                                      }
                                      onReset={() =>
                                        resetPermission(
                                          permission
                                        )
                                      }
                                    />
                                  </td>
                                );
                              })}
                            </tr>
                          )
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              ))}

              {/* Bottom Actions */}
              {hasChanges && (
                <div className="sticky bottom-4 z-10 rounded-xl border border-brand-200 bg-white/95 p-4 shadow-lg backdrop-blur dark:border-brand-900/40 dark:bg-gray-900/95">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                      <div className="text-sm font-semibold text-gray-900 dark:text-white">
                        Unsaved permission changes
                      </div>

                      <div className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                        {Object.keys(pendingChanges).length} permission
                        {Object.keys(pendingChanges).length === 1
                          ? ""
                          : "s"} changed.
                      </div>
                    </div>

                    <div className="flex gap-3">
                      <button
                        type="button"
                        onClick={discardChanges}
                        disabled={saving}
                        className="rounded-lg border border-gray-300 px-4 py-2.5 text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.04]"
                      >
                        Discard
                      </button>

                      <button
                        type="button"
                        onClick={saveChanges}
                        disabled={saving}
                        className="rounded-lg bg-brand-500 px-5 py-2.5 text-sm font-medium text-white hover:bg-brand-600 disabled:opacity-50"
                      >
                        {saving
                          ? "Saving..."
                          : "Save Changes"}
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function SummaryCard({
  label,
  value,
  description,
  highlight = false,
}: {
  label: string;
  value: number;
  description: string;
  highlight?: boolean;
}) {
  return (
    <div
      className={`rounded-xl border bg-white p-4 dark:bg-white/[0.03] ${
        highlight
          ? "border-brand-200 dark:border-brand-900/40"
          : "border-gray-200 dark:border-gray-800"
      }`}
    >
      <div className="text-xs font-medium text-gray-500 dark:text-gray-400">
        {label}
      </div>

      <div className="mt-2 text-2xl font-semibold text-gray-900 dark:text-white">
        {value}
      </div>

      <div className="mt-1 text-xs text-gray-400">
        {description}
      </div>
    </div>
  );
}

function PermissionSummaryCard({
  label,
  value,
  description,
  type,
}: {
  label: string;
  value: number;
  description: string;
  type: "allowed" | "denied" | "inherited" | "override";
}) {
  const styles = {
    allowed:
      "border-green-200 bg-green-50 dark:border-green-900/30 dark:bg-green-900/10",
    denied:
      "border-red-200 bg-red-50 dark:border-red-900/30 dark:bg-red-900/10",
    inherited:
      "border-gray-200 bg-gray-50 dark:border-gray-800 dark:bg-gray-900/30",
    override:
      "border-blue-200 bg-blue-50 dark:border-blue-900/30 dark:bg-blue-900/10",
  };

  const valueStyles = {
    allowed: "text-green-700 dark:text-green-400",
    denied: "text-red-700 dark:text-red-400",
    inherited: "text-gray-700 dark:text-gray-300",
    override: "text-blue-700 dark:text-blue-400",
  };

  return (
    <div className={`rounded-xl border p-4 ${styles[type]}`}>
      <div className="text-xs font-medium text-gray-500 dark:text-gray-400">
        {label}
      </div>

      <div
        className={`mt-2 text-2xl font-semibold ${valueStyles[type]}`}
      >
        {value}
      </div>

      <div className="mt-1 text-xs text-gray-400">
        {description}
      </div>
    </div>
  );
}

function RoleBadge({
  role,
}: {
  role: string;
}) {
  const styles =
    role === "ADMIN"
      ? "bg-purple-100 text-purple-700 dark:bg-purple-900/30 dark:text-purple-400"
      : role === "SUPER_ADMIN"
        ? "bg-brand-100 text-brand-700 dark:bg-brand-900/30 dark:text-brand-400"
        : "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400";

  return (
    <span
      className={`rounded-full px-2.5 py-1 text-[10px] font-semibold uppercase tracking-wide ${styles}`}
    >
      {role.replace("_", " ")}
    </span>
  );
}

function LegendItem({
  label,
  className,
}: {
  label: string;
  className: string;
}) {
  return (
    <span className="flex items-center gap-2">
      <span
        className={`rounded-full px-2.5 py-1 text-[10px] font-semibold ${className}`}
      >
        {label}
      </span>
    </span>
  );
}

function PermissionControl({
  permission,
  pending,
  disabled,
  onToggle,
  onReset,
}: {
  permission: PermissionItemResponse;
  pending: boolean;
  disabled: boolean;
  onToggle: () => void;
  onReset: () => void;
}) {
  const isAllowed = permission.effective;
  const isOverride = permission.override !== null;

  return (
    <div className="flex min-w-[125px] flex-col items-center gap-1.5">
      <button
        type="button"
        onClick={onToggle}
        disabled={disabled}
        className={`min-w-[82px] rounded-full px-3 py-1.5 text-[11px] font-semibold transition disabled:cursor-not-allowed disabled:opacity-50 ${
          isAllowed
            ? "bg-green-100 text-green-700 hover:bg-green-200 dark:bg-green-900/30 dark:text-green-400 dark:hover:bg-green-900/50"
            : "bg-red-100 text-red-700 hover:bg-red-200 dark:bg-red-900/30 dark:text-red-400 dark:hover:bg-red-900/50"
        }`}
      >
        {isAllowed ? "Allowed" : "Denied"}
      </button>

      <div className="text-[10px] text-gray-400">
        Default:{" "}
        <span className="font-medium">
          {permission.roleDefault
            ? "Allowed"
            : "Denied"}
        </span>
      </div>

      {pending ? (
        <span className="rounded-full bg-yellow-100 px-2 py-0.5 text-[9px] font-semibold text-yellow-700 dark:bg-yellow-900/30 dark:text-yellow-400">
          Unsaved
        </span>
      ) : isOverride ? (
        <button
          type="button"
          onClick={onReset}
          disabled={disabled}
          className="text-[10px] font-medium text-blue-600 hover:underline disabled:opacity-50 dark:text-blue-400"
        >
          Override • Reset
        </button>
      ) : (
        <span className="text-[10px] text-gray-400">
          Inherited
        </span>
      )}
    </div>
  );
}

function getInitials(name: string) {
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}