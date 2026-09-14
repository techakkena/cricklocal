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

export interface InningsScorecardResponse {
  inningsId: number;
  inningsNumber: number;
  battingTeamId: number;
  battingTeamName: string;
  bowlingTeamId: number;
  bowlingTeamName: string;
  totalRuns: number;
  wickets: number;
  legalBalls: number;
  status: string;
}

export interface MatchResultResponse {
  id: number;
  matchId: number;
  resultType: string;
  winningTeamId: number | null;
  winningTeamName: string | null;
  losingTeamId: number | null;
  losingTeamName: string | null;
  marginRuns: number | null;
  marginWickets: number | null;
  resultText: string | null;
}

export interface ScorecardResponse {
  matchId: number;
  matchName: string;
  status: string;
  innings: InningsScorecardResponse[];
  result: MatchResultResponse | null;
}

export interface TeamResponse {
  id: number;
  name: string;
  shortName: string;
  city: string | null;
  active: boolean;
  createdAt: string;
}

export type PlayerRole =
  | "BATTER"
  | "BOWLER"
  | "ALL_ROUNDER"
  | "WICKET_KEEPER";

export type BattingStyle = "RIGHT_HAND" | "LEFT_HAND";

export type BowlingStyle =
  | "RIGHT_ARM_FAST"
  | "RIGHT_ARM_MEDIUM"
  | "RIGHT_ARM_OFF_SPIN"
  | "RIGHT_ARM_LEG_SPIN"
  | "LEFT_ARM_FAST"
  | "LEFT_ARM_MEDIUM"
  | "LEFT_ARM_ORTHODOX"
  | "LEFT_ARM_WRIST_SPIN"
  | "NONE";

export interface PlayerTeamResponse {
  teamId: number;
  teamName: string;
  shortName: string;
  jerseyNumber: number | null;
  joinedAt: string | null;
  leftAt: string | null;
}

export interface PlayerResponse {
  id: number;
  firstName: string;
  lastName: string | null;
  displayName: string;
  phone: string | null;
  battingStyle: BattingStyle;
  bowlingStyle: BowlingStyle;
  role: PlayerRole;
  active: boolean;
  createdAt: string;
  teams: PlayerTeamResponse[];
}

export type SeriesStatus =
  | "PLANNED"
  | "LIVE"
  | "COMPLETED"
  | "CANCELLED";

export interface SeriesTeamResponse {
  teamId: number;
  teamName: string;
  shortName: string;
}

export interface SeriesResponse {
  id: number;
  name: string;
  totalMatches: number;
  status: SeriesStatus;
  startDate: string;
  endDate: string | null;
  createdAt: string;
  teams: SeriesTeamResponse[];
}