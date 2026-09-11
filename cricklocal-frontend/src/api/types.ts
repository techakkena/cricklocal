export type MatchStatus =
  | "SCHEDULED"
  | "LIVE"
  | "COMPLETED"
  | "ABANDONED";

export type MatchTeamSide = "HOME" | "AWAY";

export type InningsStatus =
  | "IN_PROGRESS"
  | "COMPLETED"
  | "DECLARED"
  | "ABANDONED";

export interface MatchTeamResponse {
  teamId: number;
  teamName: string;
  shortName: string;
  side: MatchTeamSide;
}

export interface MatchResponse {
  id: number;
  name: string;
  format: string;
  totalOvers: number;
  maxPlayersPerTeam: number;
  seriesId: number | null;
  seriesName: string | null;
  matchNumber: number | null;
  scheduledAt: string | null;
  venue: string | null;
  status: MatchStatus;
  createdAt: string;
  teams: MatchTeamResponse[];
}

export interface InningsResponse {
  id: number;
  matchId: number;
  inningsNumber: number;
  battingTeamId: number;
  battingTeamName: string;
  battingTeamShortName: string;
  bowlingTeamId: number;
  bowlingTeamName: string;
  bowlingTeamShortName: string;
  totalRuns: number;
  wickets: number;
  legalBalls: number;
  status: InningsStatus;
  createdAt: string;
  startedAt: string | null;
  completedAt: string | null;
}