import { apiGet, apiPost } from "./apiClient";
import type {
  AddPlayerToMatchRequest,
  InningsResponse,
  MatchLineupResponse,
  MatchResponse,
  PlayingXIResponse,
  ScorecardResponse,
  StartInningsRequest,
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

export function startMatchInnings(
    matchId: number,
    request: StartInningsRequest,
  ): Promise<InningsResponse> {
    return apiPost<InningsResponse>(
      `/api/matches/${matchId}/innings`,
      request,
    );
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

export function getMatchLineup(
  matchId: number,
): Promise<MatchLineupResponse[]> {
  return apiGet<MatchLineupResponse[]>(
    `/api/matches/${matchId}/lineup`,
  );
}

export function getTeamMatchLineup(
  matchId: number,
  teamId: number,
): Promise<MatchLineupResponse[]> {
  return apiGet<MatchLineupResponse[]>(
    `/api/matches/${matchId}/lineup/team/${teamId}`,
  );
}

export function getFinalizedPlayingXI(
  matchId: number,
  teamId: number,
): Promise<PlayingXIResponse> {
  return apiGet<PlayingXIResponse>(
    `/api/matches/${matchId}/lineup/team/${teamId}/finalized`,
  );
}

export function addPlayerToMatch(
  matchId: number,
  request: AddPlayerToMatchRequest,
): Promise<MatchLineupResponse> {
  return apiPost<MatchLineupResponse>(
    `/api/matches/${matchId}/lineup`,
    request,
  );
}

export function finalizePlayingXI(
  matchId: number,
  teamId: number,
): Promise<PlayingXIResponse> {
  return apiPost<PlayingXIResponse>(
    `/api/matches/${matchId}/lineup/team/${teamId}/finalize`,
    {},
  );
}