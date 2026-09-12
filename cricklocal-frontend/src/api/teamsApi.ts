import { apiGet } from "./apiClient";
import type { TeamResponse } from "./types";

export function getTeams(): Promise<TeamResponse[]> {
  return apiGet<TeamResponse[]>("/api/teams");
}
