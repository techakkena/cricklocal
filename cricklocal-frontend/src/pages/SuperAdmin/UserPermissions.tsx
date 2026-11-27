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

export default function UserPermissions() {
  const [users, setUsers] = useState<UserPermissionResponse[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(
    null
  );
  const [permissions, setPermissions] = useState<
    UserPermissionResponse | null
  >(null);

  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState<string | null>(null);
  const [message, setMessage] = useState("");

  useEffect(() => {
    loadUsers();
  }, []);

  async function loadUsers() {
    try {
      setLoading(true);

      const data = await getPermissionUsers();

      setUsers(data);

      if (data.length > 0) {
        setSelectedUserId(data[0].userId);
      }
    } catch (error) {
      console.error(error);
      setMessage("Failed to load users.");
    } finally {
      setLoading(false);
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
      setLoading(true);

      const data = await getUserPermissions(userId);

      setPermissions(data);
    } catch (error) {
      console.error(error);
      setMessage("Failed to load user permissions.");
    } finally {
      setLoading(false);
    }
  }

  const filteredUsers = useMemo(() => {
    const value = search.trim().toLowerCase();

    if (!value) {
      return users;
    }

    return users.filter(
      (user) =>
        user.displayName.toLowerCase().includes(value) ||
        user.email.toLowerCase().includes(value) ||
        user.role.toLowerCase().includes(value)
    );
  }, [users, search]);

  async function changePermission(
    permission: PermissionItemResponse,
    allowed: boolean | null
  ) {
    if (!permissions) {
      return;
    }

    const key = `${permission.moduleKey}:${permission.action}`;

    try {
      setSaving(key);
      setMessage("");

      const updated = await updateUserPermission(
        permissions.userId,
        {
          moduleKey: permission.moduleKey,
          action: permission.action,
          allowed,
        }
      );

      setPermissions(updated);

      setUsers((current) =>
        current.map((user) =>
          user.userId === updated.userId ? updated : user
        )
      );

      setMessage("Permission updated successfully.");
    } catch (error) {
      console.error(error);
      setMessage("Failed to update permission.");
    } finally {
      setSaving(null);
    }
  }

  async function resetAll() {
    if (!permissions) {
      return;
    }

    const confirmed = window.confirm(
      `Reset all permissions for ${permissions.displayName}?`
    );

    if (!confirmed) {
      return;
    }

    try {
      setLoading(true);
      setMessage("");

      await resetUserPermissions(permissions.userId);

      await loadSelectedUser(permissions.userId);

      setMessage("All permissions reset to role defaults.");
    } catch (error) {
      console.error(error);
      setMessage("Failed to reset permissions.");
    } finally {
      setLoading(false);
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

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-semibold text-gray-900 dark:text-white">
          User Permissions
        </h1>

        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Manage permissions for ADMIN and PLAYER users.
        </p>
      </div>

      {message && (
        <div className="rounded-lg border border-gray-200 bg-gray-50 px-4 py-3 text-sm text-gray-700 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-200">
          {message}
        </div>
      )}

      <div className="grid grid-cols-1 gap-6 xl:grid-cols-4">
        {/* User list */}
        <div className="rounded-xl border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-white/[0.03]">
          <h2 className="mb-3 text-lg font-medium text-gray-900 dark:text-white">
            Users
          </h2>

          <input
            type="text"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Search users..."
            className="mb-4 w-full rounded-lg border border-gray-300 px-3 py-2 text-sm outline-none dark:border-gray-700 dark:bg-gray-900 dark:text-white"
          />

          <div className="space-y-2">
            {filteredUsers.map((user) => (
              <button
                key={user.userId}
                type="button"
                onClick={() => setSelectedUserId(user.userId)}
                className={`w-full rounded-lg border px-3 py-3 text-left ${
                  selectedUserId === user.userId
                    ? "border-blue-500 bg-blue-50 dark:bg-blue-500/10"
                    : "border-gray-200 hover:bg-gray-50 dark:border-gray-800 dark:hover:bg-white/[0.04]"
                }`}
              >
                <div className="font-medium text-gray-900 dark:text-white">
                  {user.displayName}
                </div>

                <div className="mt-1 text-xs text-gray-500">
                  {user.email}
                </div>

                <div className="mt-2 text-xs font-medium">
                  {user.role}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Permissions */}
        <div className="xl:col-span-3">
          {!permissions ? (
            <div className="rounded-xl border border-gray-200 bg-white p-8 text-center dark:border-gray-800 dark:bg-white/[0.03]">
              {loading ? "Loading..." : "Select a user."}
            </div>
          ) : (
            <div className="rounded-xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">
              <div className="flex flex-col gap-4 border-b border-gray-200 p-5 sm:flex-row sm:items-center sm:justify-between dark:border-gray-800">
                <div>
                  <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                    {permissions.displayName}
                  </h2>

                  <p className="text-sm text-gray-500">
                    {permissions.email}
                  </p>

                  <span className="mt-2 inline-block rounded-full bg-gray-100 px-3 py-1 text-xs font-medium dark:bg-gray-800">
                    {permissions.role}
                  </span>
                </div>

                <button
                  type="button"
                  onClick={resetAll}
                  disabled={loading}
                  className="rounded-lg border border-red-300 px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 disabled:opacity-50"
                >
                  Reset All Permissions
                </button>
              </div>

              <div className="overflow-x-auto">
                <table className="min-w-full">
                  <thead>
                    <tr className="border-b border-gray-200 dark:border-gray-800">
                      <th className="px-5 py-3 text-left text-xs font-semibold uppercase text-gray-500">
                        Module
                      </th>

                      {ACTIONS.map((action) => (
                        <th
                          key={action}
                          className="px-4 py-3 text-center text-xs font-semibold uppercase text-gray-500"
                        >
                          {action}
                        </th>
                      ))}
                    </tr>
                  </thead>

                  <tbody>
                    {permissions.permissions
                      .filter(
                        (permission, index, array) =>
                          array.findIndex(
                            (item) =>
                              item.moduleKey === permission.moduleKey
                          ) === index
                      )
                      .map((modulePermission) => (
                        <tr
                          key={modulePermission.moduleKey}
                          className="border-b border-gray-100 dark:border-gray-800"
                        >
                          <td className="px-5 py-4">
                            <div className="font-medium text-gray-900 dark:text-white">
                              {modulePermission.moduleName}
                            </div>

                            <div className="text-xs text-gray-400">
                              {modulePermission.moduleKey}
                            </div>
                          </td>

                          {ACTIONS.map((action) => {
                            const permission = getPermission(
                              modulePermission.moduleKey,
                              action
                            );

                            if (!permission) {
                              return (
                                <td
                                  key={action}
                                  className="px-4 py-4 text-center"
                                >
                                  —
                                </td>
                              );
                            }

                            const key = `${permission.moduleKey}:${permission.action}`;

                            return (
                              <td
                                key={action}
                                className="px-4 py-4 text-center"
                              >
                                <PermissionCell
                                  permission={permission}
                                  saving={saving === key}
                                  onChange={changePermission}
                                />
                              </td>
                            );
                          })}
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

interface PermissionCellProps {
  permission: PermissionItemResponse;
  saving: boolean;
  onChange: (
    permission: PermissionItemResponse,
    allowed: boolean | null
  ) => void;
}

function PermissionCell({
  permission,
  saving,
  onChange,
}: PermissionCellProps) {
  const isOverride = permission.override !== null;

  return (
    <div className="flex min-w-[120px] flex-col items-center gap-2">
      <button
        type="button"
        disabled={saving}
        onClick={() =>
          onChange(
            permission,
            permission.effective ? false : true
          )
        }
        className={`rounded-full px-3 py-1 text-xs font-semibold ${
          permission.effective
            ? "bg-green-100 text-green-700"
            : "bg-red-100 text-red-700"
        }`}
      >
        {saving
          ? "Saving..."
          : permission.effective
            ? "Allowed"
            : "Denied"}
      </button>

      <div className="text-[11px] text-gray-500">
        Default:{" "}
        {permission.roleDefault ? "Allowed" : "Denied"}
      </div>

      <div className="text-[11px] text-gray-500">
        {isOverride ? (
          <button
            type="button"
            disabled={saving}
            onClick={() => onChange(permission, null)}
            className="text-blue-600 hover:underline"
          >
            Override • Reset
          </button>
        ) : (
          "Inherited"
        )}
      </div>
    </div>
  );
}