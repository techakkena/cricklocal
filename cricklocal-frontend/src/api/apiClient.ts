const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: "include",
  });

  if (!response.ok) {
    let message = `API request failed: ${response.status} ${response.statusText}`;

    try {
      const errorBody = await response.json();

      if (errorBody?.message) {
        message = errorBody.message;
      }
    } catch {
      // Keep the default HTTP error message.
    }

    throw new Error(message);
  }

  return response.json() as Promise<T>;
}

export async function apiPost<T>(
  path: string,
  body: unknown,
  headers: Record<string, string> = {},
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...headers,
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    let message = `API request failed: ${response.status} ${response.statusText}`;

    try {
      const errorBody = (await response.json()) as {
        message?: string;
      };

      if (errorBody.message) {
        message = errorBody.message;
      }
    } catch {
      // Keep the default HTTP error message.
    }

    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const contentLength = response.headers.get("content-length");

  if (contentLength === "0") {
    return undefined as T;
  }

  const text = await response.text();

  if (!text.trim()) {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}

export async function apiPostNoContent(
  path: string,
  body: unknown = {},
  headers: Record<string, string> = {},
): Promise<void> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      ...headers,
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    let message = `API request failed: ${response.status} ${response.statusText}`;

    try {
      const errorBody = (await response.json()) as {
        message?: string;
      };

      if (errorBody.message) {
        message = errorBody.message;
      }
    } catch {
      // Keep the default HTTP error message.
    }

    throw new Error(message);
  }
}
export async function apiPut<T>(
  path: string,
  body: unknown,
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "PUT",
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  });

  if (!response.ok) {
    let message = `API request failed: ${response.status} ${response.statusText}`;

    try {
      const errorBody = (await response.json()) as {
        message?: string;
      };

      if (errorBody.message) {
        message = errorBody.message;
      }
    } catch {
      // Keep the default HTTP error message.
    }

    throw new Error(message);
  }

  return response.json() as Promise<T>;
}

export async function apiPostMultipart<T>(
  path: string,
  body: FormData,
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "POST",
    credentials: "include",
    body,
  });

  if (!response.ok) {
    let message = `API request failed: ${response.status} ${response.statusText}`;

    try {
      const errorBody = (await response.json()) as {
        message?: string;
      };

      if (errorBody.message) {
        message = errorBody.message;
      }
    } catch {
      // Keep the default HTTP error message.
    }

    throw new Error(message);
  }

  return response.json() as Promise<T>;
}

export async function apiDelete<T = void>(
  path: string,
): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: "DELETE",
    credentials: "include",
  });

  if (!response.ok) {
    let message = `API request failed: ${response.status} ${response.statusText}`;

    try {
      const errorBody = (await response.json()) as {
        message?: string;
      };

      if (errorBody.message) {
        message = errorBody.message;
      }
    } catch {
      // Ignore non-JSON error responses.
    }

    throw new Error(message);
  }

  return undefined as T;
}