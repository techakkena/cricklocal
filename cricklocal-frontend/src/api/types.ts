export type MatchStatus =
  | "SCHEDULED"
  | "LIVE"
  | "COMPLETED"
  | "ABANDONED";

export type MatchTeamSide = "HOME" | "AWAY";

export type InningsStatus =
  | "LIVE"
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

export interface StartInningsRequest {
  inningsNumber: number;
  battingTeamId: number;
  bowlingTeamId: number;
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

export interface PartnershipResponse {
  id: number;
  inningsId: number;

  partnershipNumber: number;

  batterOneId: number;
  batterOneName: string;

  batterTwoId: number;
  batterTwoName: string;

  runs: number;
  balls: number;

  active: boolean;
}

export interface FallOfWicketResponse {
  id: number;
  inningsId: number;
  wicketNumber: number;
  dismissedPlayerId: number;
  dismissedPlayerName: string;
  score: number;
  overNumber: number;
  ballInOver: number;
  wicketType: WicketType;
  deliveryId: number;
  bowlerId: number;
  bowlerName: string;
}

export interface FieldingEventResponse {
  id: number;

  inningsId: number;
  deliveryId: number;

  fielderId: number;
  fielderName: string;

  dismissedPlayerId: number;
  dismissedPlayerName: string;

  wicketType: WicketType;

  createdAt: string;
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

  batting: BattingInningsResponse[];
  bowling: BowlingInningsResponse[];
  partnerships: PartnershipResponse[];
  fallOfWickets: FallOfWicketResponse[];
  fieldingEvents: FieldingEventResponse[];
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
  seriesName: string | null;
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

export interface MatchLineupResponse {
  id: number;
  matchId: number;
  teamId: number;
  teamName: string;
  teamShortName: string;
  playerId: number;
  playerName: string;
  jerseyNumber: number | null;
  playing: boolean;
  captain: boolean;
  wicketKeeper: boolean;
}

export interface PlayingXIResponse {
  playingXIId: number;
  matchId: number;
  teamId: number;
  teamName: string;
  teamShortName: string;
  finalizedAt: string | null;
  playerCount: number;
  captainPlayerId: number;
  captainName: string;
}

export interface AddPlayerToMatchRequest {
  teamId: number;
  playerId: number;
  playing: boolean;
  captain: boolean;
  wicketKeeper: boolean;
}

export type ExtraType =
  | "NONE"
  | "WIDE"
  | "NO_BALL"
  | "BYE"
  | "LEG_BYE"
  | "PENALTY";

export type WicketType =
  | "BOWLED"
  | "CAUGHT"
  | "LBW"
  | "RUN_OUT"
  | "STUMPED"
  | "HIT_WICKET"
  | "RETIRED_HURT"
  | "RETIRED_OUT"
  | "OBSTRUCTING_FIELD";

export type DismissalEnd = "STRIKER" | "NON_STRIKER";

export interface InningsStateResponse {
  id: number;
  inningsId: number;
  inningsNumber: number;
  strikerId: number;
  strikerName: string;
  nonStrikerId: number;
  nonStrikerName: string;
  currentBowlerId: number;
  currentBowlerName: string;
  currentOver: number;
  legalBallsInOver: number;
}

export interface SetInningsStateRequest {
  strikerId: number;
  nonStrikerId: number;
  bowlerId: number;
}

export interface RecordDeliveryRequest {
  batterId: number;
  nonStrikerId: number;
  bowlerId: number;
  runsOffBat: number;
  extraType: ExtraType;
  extraRuns: number;
  wicket: boolean;
  wicketType?: WicketType;
  dismissedPlayerId?: number;
  newBatterId?: number;
  dismissalEnd?: DismissalEnd;
  fielderId?: number;
}

export type DeliveryResult =
  | "DOT_BALL"
  | "RUNS"
  | "FOUR"
  | "SIX"
  | "EXTRA"
  | "WICKET";

export interface DeliveryResponse {
  id: number;
  inningsId: number;
  inningsNumber: number;
  deliveryNumber: number;
  overNumber: number;
  ballInOver: number;
  batterId: number;
  batterName: string;
  nonStrikerId: number;
  nonStrikerName: string;
  bowlerId: number;
  bowlerName: string;
  legalDelivery: boolean;
  runsOffBat: number;
  extraRuns: number;
  totalRuns: number;
  extraType: ExtraType;
  result: DeliveryResult;
  wicket: boolean;
  wicketType: WicketType | null;
  dismissalEnd: DismissalEnd | null;
  dismissedPlayerId: number | null;
  dismissedPlayerName: string | null;
  fielderId: number | null;
  fielderName: string | null;
  inningsTotalRuns: number;
  inningsWickets: number;
  inningsLegalBalls: number;
  createdAt: string;
}

export interface BattingInningsResponse {
  id: number;
  inningsId: number;
  playerId: number;
  playerName: string;
  battingPosition: number;
  runs: number;
  ballsFaced: number;
  fours: number;
  sixes: number;
  dots: number;
  extrasFaced: number;
  dismissed: boolean;
  dismissalType: string | null;
  dismissedByPlayerId: number | null;
  dismissedByPlayerName: string | null;
}

export interface BowlingInningsResponse {
  id: number;
  inningsId: number;
  playerId: number;
  playerName: string;
  overs: string;
  ballsBowled: number;
  runsConceded: number;
  wickets: number;
  maidens: number;
  foursConceded: number;
  sixesConceded: number;
  wides: number;
  noBalls: number;
}