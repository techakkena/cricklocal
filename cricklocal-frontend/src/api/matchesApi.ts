import { apiGet } from "./apiClient";
import type {
  InningsResponse,
  MatchResponse,
  ScorecardResponse,
} from "./types";

export function getMatches(): Promise<MatchResponse[]> {
  return apiGet<MatchResponse[]>("/api/matches");
}

export function getMatchInnings(matchId: number): Promise<InningsResponse[]> {
  return apiGet<InningsResponse[]>(`/api/matches/${matchId}/innings`);
}

export function getMatchScorecard(
  matchId: number,
): Promise<ScorecardResponse> {
  return apiGet<ScorecardResponse>(
    `/api/matches/${matchId}/scorecard`,
  );
}