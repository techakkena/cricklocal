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