import { apiGet, apiPost, apiPostMultipart } from "./apiClient";
import type { TeamResponse } from "./types";

export interface CreateTeamRequest {
  name: string;
  shortName: string;
  city?: string;
}

export interface TeamImportError {
  rowNumber: number;
  message: string;
}

export interface TeamImportResponse {
  totalRows: number;
  createdRows: number;
  errors: TeamImportError[];
}

export function getTeams(): Promise<TeamResponse[]> {
  return apiGet<TeamResponse[]>("/api/teams");
}

export function createTeam(
  request: CreateTeamRequest,
): Promise<TeamResponse> {
  return apiPost<TeamResponse>("/api/teams", request);
}

export function validateTeamExcel(
  file: File,
): Promise<TeamImportResponse> {
  const formData = new FormData();
  formData.append("file", file);

  return apiPostMultipart<TeamImportResponse>(
    "/api/teams/import/validate",
    formData,
  );
}

export function importTeamsExcel(
  file: File,
): Promise<TeamImportResponse> {
  const formData = new FormData();
  formData.append("file", file);

  return apiPostMultipart<TeamImportResponse>(
    "/api/teams/import",
    formData,
  );
}