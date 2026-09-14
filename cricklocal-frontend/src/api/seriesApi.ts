import { apiGet, apiPost } from "./apiClient";
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