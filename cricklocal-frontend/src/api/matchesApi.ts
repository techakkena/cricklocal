import { apiDelete, apiGet, apiPost } from "./apiClient";
import type {
  AddPlayerToMatchRequest,
  ReplaceMatchPlayerRequest,
  InningsResponse,
  MatchLineupResponse,
  MatchResponse,
  PlayingXIResponse,
  ScorecardResponse,
  StartInningsRequest,
  InningsStateResponse,
  SetInningsStateRequest,
  RecordDeliveryRequest,
  DeliveryResponse,
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

export interface GenerateScoreOperatorAccessResponse {
  matchId: number;
  accessToken: string;
  securityCode: string;
  expiresAt: string;
}

export interface ValidateScoreOperatorAccessRequest {
  accessToken: string;
  securityCode: string;
}

export interface ValidateScoreOperatorAccessResponse {
  matchId: number;
  sessionToken: string;
  expiresAt: string;
}

export interface GenerateScoreDisplayAccessResponse {
  matchId: number;
  displayToken: string;
  expiresAt: string;
}

export interface ValidateScoreDisplayAccessResponse {
  matchId: number;
  expiresAt: string;
}

export function validateScoreOperatorAccess(
  request: ValidateScoreOperatorAccessRequest,
): Promise<ValidateScoreOperatorAccessResponse> {
  return apiPost<ValidateScoreOperatorAccessResponse>(
    "/api/matches/score-operator/access/validate",
    request,
  );
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

export function generateScoreOperatorAccess(
  matchId: number,
): Promise<GenerateScoreOperatorAccessResponse> {
  return apiPost<GenerateScoreOperatorAccessResponse>(
    `/api/matches/${matchId}/score-operator/access`,
    {},
  );
}

export function generateScoreDisplayAccess(
  matchId: number,
): Promise<GenerateScoreDisplayAccessResponse> {
  return apiPost<GenerateScoreDisplayAccessResponse>(
    `/api/matches/${matchId}/score-display/access`,
    {},
  );
}

export function validateScoreDisplayAccess(
  displayToken: string,
): Promise<ValidateScoreDisplayAccessResponse> {
  return apiGet<ValidateScoreDisplayAccessResponse>(
    `/api/matches/score-display/access/${displayToken}`,
  );
}

export function revokeScoreDisplayAccess(
  matchId: number,
): Promise<void> {
  return apiDelete<void>(
    `/api/matches/${matchId}/score-display/access`,
  );
}

export function revokeScoreOperatorAccess(
  matchId: number,
): Promise<void> {
  return apiDelete<void>(
    `/api/matches/${matchId}/score-operator/access`,
  );
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

export function setInningsState(
  inningsId: number,
  request: SetInningsStateRequest,
): Promise<InningsStateResponse> {
  return apiPost<InningsStateResponse>(
    `/api/innings/${inningsId}/state`,
    request,
  );
}

export function getInningsState(
  inningsId: number,
): Promise<InningsStateResponse> {
  return apiGet<InningsStateResponse>(
    `/api/innings/${inningsId}/state`,
  );
}

export function recordDelivery(
  inningsId: number,
  request: RecordDeliveryRequest,
): Promise<DeliveryResponse> {
  return apiPost<DeliveryResponse>(
    `/api/innings/${inningsId}/deliveries`,
    request,
  );
}

export function getInningsDeliveries(
  inningsId: number,
): Promise<DeliveryResponse[]> {
  return apiGet<DeliveryResponse[]>(
    `/api/innings/${inningsId}/deliveries`,
  );
}

export function undoLastDelivery(
  inningsId: number,
): Promise<void> {
  return apiPost<void>(
    `/api/innings/${inningsId}/deliveries/undo`,
    {},
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

export function replaceMatchPlayer(
  matchId: number,
  teamId: number,
  request: ReplaceMatchPlayerRequest,
): Promise<MatchLineupResponse> {
  return apiPost<MatchLineupResponse>(
    `/api/matches/${matchId}/lineup/team/${teamId}/replace`,
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