import { apiGet, apiPost, apiPostMultipart } from "./apiClient";
import type {
  BattingStyle,
  BowlingStyle,
  PlayerResponse,
  PlayerRole,
} from "./types";

export interface CreatePlayerRequest {
  firstName: string;
  lastName?: string;
  displayName: string;
  phone?: string;
  battingStyle: BattingStyle;
  bowlingStyle: BowlingStyle;
  role: PlayerRole;
}

export interface PlayerImportError {
  rowNumber: number;
  message: string;
}

export interface PlayerImportResponse {
  totalRows: number;
  createdRows: number;
  errors: PlayerImportError[];
}

export function getPlayers(): Promise<PlayerResponse[]> {
  return apiGet<PlayerResponse[]>("/api/players");
}

export function getPlayer(playerId: number): Promise<PlayerResponse> {
  return apiGet<PlayerResponse>(`/api/players/${playerId}`);
}

export function createPlayer(
  request: CreatePlayerRequest,
): Promise<PlayerResponse> {
  return apiPost<PlayerResponse>("/api/players", request);
}

export function validatePlayerExcel(
  file: File,
): Promise<PlayerImportResponse> {
  const formData = new FormData();
  formData.append("file", file);

  return apiPostMultipart<PlayerImportResponse>(
    "/api/players/import/validate",
    formData,
  );
}

export function importPlayersExcel(
  file: File,
): Promise<PlayerImportResponse> {
  const formData = new FormData();
  formData.append("file", file);

  return apiPostMultipart<PlayerImportResponse>(
    "/api/players/import",
    formData,
  );
}