import { apiGet, apiPost } from "./apiClient";

export type TossDecision = "BAT" | "BOWL";

export interface RecordTossRequest {
  winningTeamId: number;
  decision: TossDecision;
}

export interface TossResponse {
  id: number;
  matchId: number;
  winningTeamId: number;
  winningTeamName: string;
  winningTeamShortName: string;
  decision: TossDecision;
  createdAt: string;
}

export function recordToss(
  matchId: number,
  request: RecordTossRequest,
): Promise<TossResponse> {
  return apiPost<TossResponse>(
    `/api/matches/${matchId}/toss`,
    request,
  );
}

export function getToss(
  matchId: number,
): Promise<TossResponse> {
  return apiGet<TossResponse>(
    `/api/matches/${matchId}/toss`,
  );
}