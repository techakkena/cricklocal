import { apiGet, apiPost } from "./apiClient";
import type {
  InningsResponse,
  MatchResponse,
  ScorecardResponse,
} from "./types";

export interface CreateMatchRequest {
  name: string;
  format: string;
  totalOvers: number;
  maxPlayersPerTeam: number;
  scheduledAt: string;
  venue?: string;
  teamAId: number;
  seriesId?: number;
  matchNumber?: number;
  teamBId: number;
}

export function getMatches(): Promise<MatchResponse[]> {
  return apiGet<MatchResponse[]>("/api/matches");
}

export function getMatchById(matchId: number): Promise<MatchResponse> {
  return apiGet<MatchResponse>(`/api/matches/${matchId}`);
}

export function createMatch(
  request: CreateMatchRequest,
): Promise<MatchResponse> {
  return apiPost<MatchResponse>("/api/matches", request);
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