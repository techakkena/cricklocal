import { apiGet } from "./apiClient";
import type { InningsResponse, MatchResponse } from "./types";

export function getMatches(): Promise<MatchResponse[]> {
  return apiGet<MatchResponse[]>("/api/matches");
}

export function getMatchInnings(
  matchId: number,
): Promise<InningsResponse[]> {
  return apiGet<InningsResponse[]>(
    `/api/matches/${matchId}/innings`,
  );
}