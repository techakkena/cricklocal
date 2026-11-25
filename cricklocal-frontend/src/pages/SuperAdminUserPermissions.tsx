import { useEffect, useState } from "react";
import { apiGet, apiPost, apiPut } from "../api/apiClient";

interface UserSummary {
  userId: number;
  email: string;
  displayName: string;
  role: string;
  active: boolean;
}

interface Permission {
  moduleKey: string;
  moduleName: string;
  view: boolean;
  create: boolean;
  edit: boolean;
  delete: boolean;
  approve: boolean;
}

interface UserPermissionResponse {
  user: UserSummary;
  effectivePermissions: Permission[];
}

const actions = ["view", "create", "edit", "delete", "approve"] as const;
type Action = (typeof actions)[number];

export default function SuperAdminUserPermissions() {
  const [users, setUsers] = useState<UserSummary[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  const loadUsers = async () => {
    setLoading(true);
    try {
      const result = await apiGet<UserSummary[]>("/api/super-admin/permissions/users");
      setUsers(result);
      if (selectedUserId === null && result.length > 0) {
        setSelectedUserId(result[0].userId);
      }
    } finally {
      setLoading(false);
    }
  };

  const loadPermissions = async (userId: number) => {
    setMessage("");
    const result = await apiGet<UserPermissionResponse>(
      `/api/super-admin/permissions/users/${userId}`,
    );
    setPermissions(result.effectivePermissions);
  };

  useEffect(() => {
    loadUsers().catch((error) => setMessage(error.message));
  }, []);

  useEffect(() => {
    if (selectedUserId !== null) {
      loadPermissions(selectedUserId).catch((error) =>
        setMessage(error.message),
      );
    }
  }, [selectedUserId]);

  const toggle = (moduleKey: string, action: Action) => {
    setPermissions((current) =>
      current.map((permission) =>
        permission.moduleKey === moduleKey
          ? { ...permission, [action]: !permission[action] }
          : permission,
      ),
    );
  };

  const save = async () => {
    if (selectedUserId === null) return;

    setSaving(true);
    setMessage("");

    try {
      await apiPut(
        `/api/super-admin/permissions/users/${selectedUserId}`,
        permissions.map((permission) => ({
          moduleKey: permission.moduleKey,
          view: permission.view,
          create: permission.create,
          edit: permission.edit,
          delete: permission.delete,
          approve: permission.approve,
        })),
      );

      await loadPermissions(selectedUserId);
      setMessage("Permissions saved successfully.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Failed to save permissions.");
    } finally {
      setSaving(false);
    }
  };

  const reset = async () => {
    if (selectedUserId === null) return;

    setSaving(true);
    setMessage("");

    try {
      await apiPost<UserPermissionResponse>(
        `/api/super-admin/permissions/users/${selectedUserId}/reset`,
        {},
      );
      await loadPermissions(selectedUserId);
      setMessage("Permissions reset to role defaults.");
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Failed to reset permissions.");
    } finally {
      setSaving(false);
    }
  };

  const selectedUser = users.find((user) => user.userId === selectedUserId);

  if (loading) {
    return <div className="p-6 text-sm text-gray-500">Loading permissions...</div>;
  }

  return (
    <div className="space-y-6 p-6">
      <div>
        <h1 className="text-2xl font-semibold text-gray-900 dark:text-white">
          User Permissions
        </h1>
        <p className="mt-1 text-sm text-gray-500">
          Super Admin can override individual PLAYER and ADMIN permissions.
        </p>
      </div>

      <div className="rounded-xl border border-gray-200 bg-white p-4 dark:border-gray-800 dark:bg-gray-900">
        <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">
          User
        </label>
        <select
          value={selectedUserId ?? ""}
          onChange={(event) => setSelectedUserId(Number(event.target.value))}
          className="mt-2 w-full max-w-xl rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm dark:border-gray-700 dark:bg-gray-800"
        >
          {users.map((user) => (
            <option key={user.userId} value={user.userId}>
              {user.displayName} — {user.email} — {user.role}
            </option>
          ))}
        </select>

        {selectedUser && (
          <div className="mt-3 text-xs text-gray-500">
            Account: {selectedUser.active ? "Active" : "Inactive"} · Role:{" "}
            {selectedUser.role}
          </div>
        )}
      </div>

      <div className="overflow-x-auto rounded-xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-gray-900">
        <table className="min-w-[900px] w-full text-sm">
          <thead className="bg-gray-50 dark:bg-gray-800">
            <tr>
              <th className="px-4 py-3 text-left">Module</th>
              {actions.map((action) => (
                <th key={action} className="px-4 py-3 text-center uppercase">
                  {action}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {permissions.map((permission) => (
              <tr
                key={permission.moduleKey}
                className="border-t border-gray-100 dark:border-gray-800"
              >
                <td className="px-4 py-3">
                  <div className="font-medium text-gray-900 dark:text-white">
                    {permission.moduleName}
                  </div>
                  <div className="text-xs text-gray-400">
                    {permission.moduleKey}
                  </div>
                </td>
                {actions.map((action) => (
                  <td key={action} className="px-4 py-3 text-center">
                    <input
                      type="checkbox"
                      checked={permission[action]}
                      onChange={() => toggle(permission.moduleKey, action)}
                    />
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {message && (
        <div className="rounded-lg bg-gray-100 px-4 py-3 text-sm dark:bg-gray-800">
          {message}
        </div>
      )}

      <div className="flex gap-3">
        <button
          type="button"
          onClick={save}
          disabled={saving || !selectedUserId}
          className="rounded-lg bg-brand-500 px-5 py-2.5 text-sm font-medium text-white disabled:opacity-50"
        >
          {saving ? "Saving..." : "Save Permissions"}
        </button>
        <button
          type="button"
          onClick={reset}
          disabled={saving || !selectedUserId}
          className="rounded-lg border border-gray-300 px-5 py-2.5 text-sm font-medium text-gray-700 dark:border-gray-700 dark:text-gray-200"
        >
          Reset to Defaults
        </button>
      </div>
    </div>
  );
}
