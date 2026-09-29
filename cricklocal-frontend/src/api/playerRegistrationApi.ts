import { apiGet, apiPost } from "./apiClient";

export interface PlayerRegistrationInvitationResponse {
  playerId: number;
  displayName: string;
  firstName: string;
  lastName: string | null;
  battingStyle: string;
  bowlingStyle: string;
  role: string;
  registrationStatus: string;
}

export interface PlayerRegistrationCompleteResponse {
  playerId: number;
  displayName: string;
  registrationStatus: string;
}

export interface PlayerRegistrationUpdateRequest {
  firstName: string;
  lastName: string;
  displayName: string;
  phone: string;
  battingStyle: string;
  bowlingStyle: string;
  role: string;
}

export interface PlayerRegistrationManagementResponse {
  playerId: number;
  displayName: string;
  firstName: string;
  lastName: string;
  registrationStatus: string;
  invitationStatus: string;
  invitationCreatedAt: string | null;
  invitationExpiresAt: string | null;
}

export interface PlayerRegistrationSummaryResponse {
  totalPlayers: number;
  registeredPlayers: number;
  pendingPlayers: number;
  expiredInvitations: number;
}

export interface PlayerRegistrationDetailResponse {
  playerId: number;
  displayName: string;
  firstName: string;
  lastName: string;
  phone: string | null;
  battingStyle: string | null;
  bowlingStyle: string | null;
  role: string | null;
  registrationStatus: string;
  userId: number | null;
  userEmail: string | null;
  userDisplayName: string | null;
  invitationStatus: string;
  invitationCreatedAt: string | null;
  invitationExpiresAt: string | null;
  invitationCompletedAt: string | null;
}

export interface PlayerRegistrationRegenerateResponse {
  playerId: number;
  displayName: string;
  registrationStatus: string;
  invitationStatus: string;
  invitationCreatedAt: string;
  invitationExpiresAt: string;
  registrationLink: string;
}

export interface PlayerRegistrationLinkResponse {
  playerId: number;
  displayName: string;
  registrationStatus: string;
  invitationStatus: string;
  registrationLink: string;
  expiresAt: string;
}

export function getPlayerRegistrationInvitation(
  token: string,
): Promise<PlayerRegistrationInvitationResponse> {
  return apiGet<PlayerRegistrationInvitationResponse>(
    `/api/player-registration/invitations/${encodeURIComponent(token)}`,
  );
}

export function completePlayerRegistration(
  token: string,
): Promise<PlayerRegistrationCompleteResponse> {
  return apiPost<PlayerRegistrationCompleteResponse>(
    `/api/player-registration/invitations/${encodeURIComponent(token)}/complete`,
    {},
  );
}

export function confirmPlayerRegistration(
  token: string,
  request: PlayerRegistrationUpdateRequest,
): Promise<PlayerRegistrationCompleteResponse> {
  return apiPost<PlayerRegistrationCompleteResponse>(
    `/api/player-registration/invitations/${encodeURIComponent(token)}/confirm`,
    request,
  );
}

export function getPlayerRegistrationManagement(): Promise<
  PlayerRegistrationManagementResponse[]
> {
  return apiGet<PlayerRegistrationManagementResponse[]>(
    "/api/players/registrations",
  );
}

export function getPlayerRegistrationSummary(): Promise<PlayerRegistrationSummaryResponse> {
  return apiGet<PlayerRegistrationSummaryResponse>(
    "/api/players/registrations/summary",
  );
}

export function getPlayerRegistrationDetail(
  playerId: number,
): Promise<PlayerRegistrationDetailResponse> {
  return apiGet<PlayerRegistrationDetailResponse>(
    `/api/players/registrations/${playerId}`,
  );
}

export function regeneratePlayerRegistration(
  playerId: number,
): Promise<PlayerRegistrationRegenerateResponse> {
  return apiPost<PlayerRegistrationRegenerateResponse>(
    `/api/players/registrations/${playerId}/regenerate`,
    {},
  );
}

export function getPlayerRegistrationLink(
  playerId: number,
): Promise<PlayerRegistrationLinkResponse> {
  return apiGet<PlayerRegistrationLinkResponse>(
    `/api/players/registrations/${playerId}/link`,
  );
}

export function getPlayerRegistrationExportUrl(): string {
  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

  return `${apiBaseUrl}/api/players/registrations/export`;
}

export interface PlayerRegistrationInvitationBackfillResponse {
  totalPlayers: number;
  invitationsCreated: number;
  playersSkipped: number;
}

export function backfillPlayerRegistrationInvitations(): Promise<PlayerRegistrationInvitationBackfillResponse> {
  return apiPost<PlayerRegistrationInvitationBackfillResponse>(
    "/api/players/registrations/backfill-invitations",
    {},
  );
}