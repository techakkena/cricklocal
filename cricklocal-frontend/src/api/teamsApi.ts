import {  apiDelete, apiGet, apiPost, apiPostMultipart, apiPut } from "./apiClient";
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

export interface TeamRosterPlayerResponse {
  teamPlayerId: number;
  playerId: number;
  displayName: string;
  jerseyNumber: number;
  joinedAt: string;
  leftAt: string | null;
  active: boolean;
}

export interface AddPlayerToTeamRequest {
  jerseyNumber: number;
}

export interface TeamCaptainResponse {
  teamId: number;
  teamName: string;
  shortName: string;
  teamPlayerId: number;
  playerId: number;
  displayName: string;
  jerseyNumber: number;
  active: boolean;
}

export function getTeams(): Promise<TeamResponse[]> {
  return apiGet<TeamResponse[]>("/api/teams");
}

export function createTeam(
  request: CreateTeamRequest,
): Promise<TeamResponse> {
  return apiPost<TeamResponse>("/api/teams", request);
}

export function getTeamPlayers(
  teamId: number,
): Promise<TeamRosterPlayerResponse[]> {
  return apiGet<TeamRosterPlayerResponse[]>(
    `/api/teams/${teamId}/players`,
  );
}

export function getTeamCaptain(
  teamId: number,
): Promise<TeamCaptainResponse | null> {
  return apiGet<TeamCaptainResponse>(
    `/api/teams/${teamId}/captain`,
  );
}

export function addPlayerToTeam(
  teamId: number,
  playerId: number,
  request: AddPlayerToTeamRequest,
): Promise<unknown> {
  return apiPost<unknown>(
    `/api/teams/${teamId}/players/${playerId}`,
    request,
  );
}

export function removePlayerFromTeam(
    teamId: number,
    playerId: number,
  ): Promise<void> {
    return apiDelete<void>(
      `/api/teams/${teamId}/players/${playerId}`,
    );
}

export function setTeamCaptain(
  teamId: number,
  playerId: number,
): Promise<TeamCaptainResponse> {
  return apiPut<TeamCaptainResponse>(
    `/api/teams/${teamId}/captain/${playerId}`,
    {},
  );
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