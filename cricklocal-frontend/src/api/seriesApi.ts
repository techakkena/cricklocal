import { apiDelete, apiGet, apiPost } from "./apiClient";
import type { SeriesResponse } from "./types";

export interface CreateSeriesRequest {
  name: string;
  totalMatches: number;
  startDate: string;
  endDate?: string;
}

export interface AddTeamToSeriesRequest {
  teamId: number;
}

export interface AddPlayerToSeriesRequest {
  playerId: number;
}

export interface SeriesParticipationResponse {
  id: number;
  seriesId: number;
  playerId: number;
  playerName: string;
  active: boolean;
  createdAt: string;
}

export function getSeries(): Promise<SeriesResponse[]> {
  return apiGet<SeriesResponse[]>("/api/series");
}

export function getSeriesById(seriesId: number): Promise<SeriesResponse> {
  return apiGet<SeriesResponse>(`/api/series/${seriesId}`);
}

export function createSeries(
  request: CreateSeriesRequest,
): Promise<SeriesResponse> {
  return apiPost<SeriesResponse>("/api/series", request);
}

export function addTeamToSeries(
  seriesId: number,
  request: AddTeamToSeriesRequest,
): Promise<SeriesResponse> {
  return apiPost<SeriesResponse>(
    `/api/series/${seriesId}/teams`,
    request,
  );
}

export function addPlayerToSeries(
  seriesId: number,
  request: AddPlayerToSeriesRequest,
): Promise<void> {
  return apiPost<void>(
    `/api/series/${seriesId}/players`,
    request,
  );
}

export function getSeriesParticipants(
  seriesId: number,
): Promise<SeriesParticipationResponse[]> {
  return apiGet<SeriesParticipationResponse[]>(
    `/api/series/${seriesId}/players`,
  );
}

export function removePlayerFromSeries(
  seriesId: number,
  playerId: number,
): Promise<void> {
  return apiDelete<void>(
    `/api/series/${seriesId}/players/${playerId}`,
  );
}