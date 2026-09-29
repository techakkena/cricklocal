import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";

import { getMatchScorecard } from "../api/matchesApi";

import type {
  BattingInningsResponse,
  ScorecardResponse,
  FallOfWicketResponse,
  FieldingEventResponse,
} from "../api/types";

function formatOvers(legalBalls: number): string {
  return `${Math.floor(legalBalls / 6)}.${legalBalls % 6}`;
}

function formatStrikeRate(
  runs: number,
  balls: number,
): string {
  if (balls === 0) {
    return "0.00";
  }

  return ((runs / balls) * 100).toFixed(2);
}

function formatEconomy(
  runs: number,
  balls: number,
): string {
  if (balls === 0) {
    return "0.00";
  }

  return ((runs / balls) * 6).toFixed(2);
}

function getDismissalText(
  batter: BattingInningsResponse,
  fallOfWickets: FallOfWicketResponse[],
  fieldingEvents: FieldingEventResponse[],
): string {
  if (!batter.dismissed) {
    return "not out";
  }

  const wicket = fallOfWickets.find(
    (item) => item.dismissedPlayerId === batter.playerId,
  );

  const fieldingEvent = wicket
    ? fieldingEvents.find(
        (event) => event.deliveryId === wicket.deliveryId,
      )
    : undefined;

  const bowlerName = wicket?.bowlerName;
  const fielderName = fieldingEvent?.fielderName;

  switch (batter.dismissalType) {
    case "BOWLED":
      return bowlerName
        ? `b ${bowlerName}`
        : "b";

    case "CAUGHT":
      if (fielderName && bowlerName) {
        return `c ${fielderName} b ${bowlerName}`;
      }

      if (fielderName) {
        return `c ${fielderName}`;
      }

      return bowlerName
        ? `c b ${bowlerName}`
        : "c";

    case "STUMPED":
      if (fielderName && bowlerName) {
        return `st ${fielderName} b ${bowlerName}`;
      }

      if (fielderName) {
        return `st ${fielderName}`;
      }

      return bowlerName
        ? `st b ${bowlerName}`
        : "st";

    case "LBW":
      return bowlerName
        ? `lbw b ${bowlerName}`
        : "lbw";

    case "RUN_OUT":
      return fielderName
        ? `run out (${fielderName})`
        : "run out";

    case "HIT_WICKET":
      return bowlerName
        ? `hit wicket b ${bowlerName}`
        : "hit wicket";

    case "RETIRED_HURT":
      return "retired hurt";

    case "RETIRED_OUT":
      return "retired out";

    case "OBSTRUCTING_FIELD":
      return "obstructing the field";

    default:
      return batter.dismissalType ?? "out";
  }
}

function getFieldingSummary(
  fieldingEvents: FieldingEventResponse[],
) {
  const summary = new Map<
    number,
    {
      fielderId: number;
      fielderName: string;
      catches: number;
      stumpings: number;
      runOuts: number;
    }
  >();

  fieldingEvents.forEach((event) => {
    if (!summary.has(event.fielderId)) {
      summary.set(event.fielderId, {
        fielderId: event.fielderId,
        fielderName: event.fielderName,
        catches: 0,
        stumpings: 0,
        runOuts: 0,
      });
    }

    const fielder = summary.get(event.fielderId)!;

    switch (event.wicketType) {
      case "CAUGHT":
        fielder.catches += 1;
        break;

      case "STUMPED":
        fielder.stumpings += 1;
        break;

      case "RUN_OUT":
        fielder.runOuts += 1;
        break;

      default:
        break;
    }
  });

  return Array.from(summary.values());
}

export default function ScorecardDetails() {
  const { matchId } = useParams<{ matchId: string }>();

  const [scorecard, setScorecard] =
    useState<ScorecardResponse | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const numericMatchId = Number(matchId);

  useEffect(() => {
    if (!matchId) {
      setError("Match ID is missing.");
      setLoading(false);
      return;
    }

    if (!Number.isFinite(numericMatchId)) {
      setError("Invalid match ID.");
      setLoading(false);
      return;
    }

    async function loadScorecard() {
      try {
        setLoading(true);
        setError("");

        const response =
          await getMatchScorecard(numericMatchId);

        setScorecard(response);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to load scorecard.",
        );
      } finally {
        setLoading(false);
      }
    }

    void loadScorecard();
  }, [matchId, numericMatchId]);



  if (loading) {
    return (
      <div className="rounded-2xl border border-gray-200 bg-white p-8 dark:border-gray-800 dark:bg-white/[0.03]">
        <p className="text-sm text-gray-500 dark:text-gray-400">
          Loading scorecard...
        </p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="rounded-2xl border border-red-200 bg-white p-8 dark:border-red-900/40 dark:bg-white/[0.03]">
        <h2 className="text-lg font-semibold text-red-600 dark:text-red-400">
          Unable to load scorecard
        </h2>

        <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
          {error}
        </p>

        <Link
          to="/scorecards"
          className="mt-5 inline-flex rounded-lg bg-brand-500 px-4 py-2 text-sm font-medium text-white hover:bg-brand-600"
        >
          Back to Scorecards
        </Link>
      </div>
    );
  }

  if (!scorecard) {
    return (
      <div className="rounded-2xl border border-gray-200 bg-white p-8 dark:border-gray-800 dark:bg-white/[0.03]">
        <p className="text-sm text-gray-500 dark:text-gray-400">
          No scorecard data found.
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">

      {/* Breadcrumb */}
      <div>
        <div className="flex flex-wrap items-center gap-2 text-sm text-gray-500 dark:text-gray-400">

          <Link
            to="/"
            className="hover:text-brand-500"
          >
            Home
          </Link>

          <span>/</span>

          <Link
            to="/scorecards"
            className="hover:text-brand-500"
          >
            Scorecards
          </Link>

          <span>/</span>

          <span className="text-gray-800 dark:text-white/90">
            Match {scorecard.matchId}
          </span>

        </div>

      </div>
      <div className="mb-6">
        {/* Header */}
        <h1 className="text-2xl font-bold text-brand-600 dark:text-brand-600">
          Score Card
        </h1>
        {scorecard.seriesName && (
          <p className="mt-2 text-2xl font-semibold uppercase tracking-wide text-brand-600 dark:text-brand-400">
            {scorecard.seriesName}
          </p>
        )}

        <h1 className="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
          {scorecard.matchName}
        </h1>
        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Complete Match Scorecard
        </p>

        <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
          Match #{scorecard.matchId}
        </p>
      </div>

      {/* Match Header */}
      <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">

        <div className="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">

          <div>
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
              Match Status
            </p>

            <p className="mt-1 text-lg font-bold text-gray-800 dark:text-white/90">
              {scorecard.status}
            </p>
          </div>

          <div className="rounded-xl bg-gray-50 px-5 py-4 dark:bg-white/[0.05]">

            <p className="text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
              Match Status
            </p>

            <p className="mt-1 text-lg font-bold text-gray-800 dark:text-white/90">
              {scorecard.status}
            </p>

          </div>

        </div>
      </div>

      {/* Match Result */}
{scorecard.result && (
  <div className="rounded-2xl border border-green-200 bg-green-50 p-6 dark:border-green-900/40 dark:bg-green-950/20">

    <p className="text-xs font-semibold uppercase tracking-wide text-green-700 dark:text-green-400">
      Match Result
    </p>

    <p className="mt-2 text-sm font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
      Winner
    </p>

    <p className="mt-1 text-xl font-bold text-green-800 dark:text-green-300">
      {scorecard.result.winningTeamName ?? "—"}
    </p>

    <div className="mt-5 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">

      {/* Result Type */}
      <div className="rounded-xl bg-white p-4 dark:bg-white/[0.05]">
        <p className="text-xs text-gray-500 dark:text-gray-400">
          Result Type
        </p>

        <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
          {scorecard.result.marginRuns !== null
            ? `WON BY ${scorecard.result.marginRuns} RUNS`
            : scorecard.result.marginWickets !== null
              ? `WON BY ${scorecard.result.marginWickets} WICKETS`
              : "—"}
        </p>
      </div>

      {/* Winner */}
      <div className="rounded-xl bg-white p-4 dark:bg-white/[0.05]">
        <p className="text-xs text-gray-500 dark:text-gray-400">
          Winner
        </p>

        <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
          {scorecard.result.winningTeamName ?? "—"}
        </p>
      </div>

      {/* Losing Team */}
      <div className="rounded-xl bg-white p-4 dark:bg-white/[0.05]">
        <p className="text-xs text-gray-500 dark:text-gray-400">
          Losing Team
        </p>

        <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
          {scorecard.result.losingTeamName ?? "—"}
        </p>
      </div>

      {/* Margin */}
      <div className="rounded-xl bg-white p-4 dark:bg-white/[0.05]">
        <p className="text-xs text-gray-500 dark:text-gray-400">
          Margin
        </p>

        <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
          {scorecard.result.marginRuns !== null
            ? `${scorecard.result.marginRuns} runs`
            : scorecard.result.marginWickets !== null
              ? `${scorecard.result.marginWickets} wickets`
              : "—"}
        </p>
      </div>

    </div>
  </div>
)}

      {/* Innings */}
      <div className="space-y-10">

        {scorecard.innings.map((innings) => {

          const batting = innings.batting ?? [];
          const bowling = innings.bowling ?? [];
          const partnerships =
            innings.partnerships ?? [];
          const fallOfWickets =
            innings.fallOfWickets ?? [];
          const fieldingEvents =
            innings.fieldingEvents ?? [];
          const fieldingSummary =
            getFieldingSummary(fieldingEvents);

          return (
            <div
              key={innings.inningsId}
              className="space-y-6"
            >

              {/* Innings Summary */}
              <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">

                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

                  <div>
                    <p className="text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
                      Innings {innings.inningsNumber}
                    </p>

                    <h2 className="mt-1 text-xl font-bold text-gray-800 dark:text-white/90">
                      {innings.battingTeamName}
                    </h2>

                    <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                      vs {innings.bowlingTeamName}
                    </p>
                  </div>

                  <div className="text-left sm:text-right">

                    <p className="text-3xl font-bold text-gray-800 dark:text-white/90">
                      {innings.totalRuns}/{innings.wickets}
                    </p>

                    <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                      {formatOvers(innings.legalBalls)} overs
                    </p>

                  </div>

                </div>

              </div>

              {/* Batting */}
              <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">

                <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">

                  <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
                    Batting
                  </h3>

                  <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                    {innings.battingTeamName}
                  </p>

                </div>

                {batting.length === 0 ? (
                  <div className="p-6">
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      No batting data available.
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">

                    <table className="w-full min-w-[850px] text-left text-sm">

                      <thead>
                        <tr className="border-b border-gray-200 bg-gray-50 dark:border-gray-800 dark:bg-white/[0.02]">

                          <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                            Batter
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            R
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            B
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            4s
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            6s
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            SR
                          </th>

                          <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                            Dismissal
                          </th>

                        </tr>
                      </thead>

                      <tbody>
                        {batting.map((batter) => (
                          <tr
                            key={batter.id}
                            className="border-b border-gray-100 last:border-0 dark:border-gray-800/60"
                          >

                            <td className="px-4 py-4">

                              <div className="font-medium text-gray-800 dark:text-white/90">
                                {batter.playerName}
                              </div>

                              <div className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                                Position {batter.battingPosition}
                              </div>

                            </td>

                            <td className="px-4 py-4 text-center font-bold text-gray-800 dark:text-white/90">
                              {batter.runs}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {batter.ballsFaced}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {batter.fours}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {batter.sixes}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {formatStrikeRate(
                                batter.runs,
                                batter.ballsFaced,
                              )}
                            </td>

                            <td className="px-4 py-4 text-gray-600 dark:text-gray-400">
                            {getDismissalText(
                              batter,
                              fallOfWickets,
                              fieldingEvents,
                            )}
                          </td>

                          </tr>
                        ))}
                      </tbody>

                      <tfoot>
                        <tr className="border-t border-gray-200 bg-gray-50 dark:border-gray-800 dark:bg-white/[0.02]">

                          <td className="px-4 py-4 font-semibold text-gray-800 dark:text-white/90">
                            Total
                          </td>

                          <td className="px-4 py-4 text-center font-bold text-gray-800 dark:text-white/90">
                            {innings.totalRuns}
                          </td>

                          <td
                            colSpan={5}
                            className="px-4 py-4 text-right text-sm text-gray-500 dark:text-gray-400"
                          >
                            {innings.totalRuns}/
                            {innings.wickets} (
                            {formatOvers(
                              innings.legalBalls,
                            )}{" "}
                            overs)
                          </td>

                        </tr>
                      </tfoot>

                    </table>

                  </div>
                )}

              </div>

              {/* Bowling */}
              <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">

                <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">

                  <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
                    Bowling
                  </h3>

                  <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                    {innings.bowlingTeamName}
                  </p>

                </div>

                {bowling.length === 0 ? (
                  <div className="p-6">
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      No bowling data available.
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">

                    <table className="w-full min-w-[900px] text-left text-sm">

                      <thead>
                        <tr className="border-b border-gray-200 bg-gray-50 dark:border-gray-800 dark:bg-white/[0.02]">

                          <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                            Bowler
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            O
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            M
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            R
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            W
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            Econ
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            4s
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            6s
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            WD
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            NB
                          </th>

                        </tr>
                      </thead>

                      <tbody>
                        {bowling.map((bowler) => (
                          <tr
                            key={bowler.id}
                            className="border-b border-gray-100 last:border-0 dark:border-gray-800/60"
                          >

                            <td className="px-4 py-4 font-medium text-gray-800 dark:text-white/90">
                              {bowler.playerName}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {formatOvers(
                                bowler.ballsBowled,
                              )}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {bowler.maidens}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {bowler.runsConceded}
                            </td>

                            <td className="px-4 py-4 text-center font-bold text-gray-800 dark:text-white/90">
                              {bowler.wickets}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {formatEconomy(
                                bowler.runsConceded,
                                bowler.ballsBowled,
                              )}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {bowler.foursConceded}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {bowler.sixesConceded}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {bowler.wides}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {bowler.noBalls}
                            </td>

                          </tr>
                        ))}
                      </tbody>

                    </table>

                  </div>
                )}

              </div>

              {/* Fall of Wickets */}
              <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">

                <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">

                  <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
                    Fall of Wickets
                  </h3>

                  <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                    Wicket progression for {innings.battingTeamName}
                  </p>

                </div>

                {fallOfWickets.length === 0 ? (
                  <div className="p-6">
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      No wickets recorded.
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">

                    <table className="w-full min-w-[650px] text-left text-sm">

                      <thead>
                        <tr className="border-b border-gray-200 bg-gray-50 dark:border-gray-800 dark:bg-white/[0.02]">

                          <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                            Wicket
                          </th>

                          <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                            Score
                          </th>

                          <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                            Batter
                          </th>

                          <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                            Dismissal
                          </th>

                          <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                            Over
                          </th>

                        </tr>
                      </thead>

                      <tbody>
                        {fallOfWickets.map((wicket) => (
                          <tr
                            key={wicket.id}
                            className="border-b border-gray-100 last:border-0 dark:border-gray-800/60"
                          >

                            <td className="px-4 py-4 font-semibold text-gray-800 dark:text-white/90">
                              {wicket.wicketNumber}
                            </td>

                            <td className="px-4 py-4 font-bold text-gray-800 dark:text-white/90">
                              {wicket.score}/{wicket.wicketNumber}
                            </td>

                            <td className="px-4 py-4 text-gray-800 dark:text-white/90">
                              {wicket.dismissedPlayerName}
                            </td>

                            <td className="px-4 py-4 text-gray-600 dark:text-gray-400">
                              {wicket.wicketType}
                            </td>

                            <td className="px-4 py-4 text-gray-600 dark:text-gray-400">
                              {wicket.overNumber}.
                              {wicket.ballInOver}
                            </td>

                          </tr>
                        ))}
                      </tbody>

                    </table>

                  </div>
                )}

              </div>

              {/* Partnerships */}
              <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">

                <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">

                  <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
                    Partnerships
                  </h3>

                  <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                    Batting partnerships for {innings.battingTeamName}
                  </p>

                </div>

                {partnerships.length === 0 ? (
                  <div className="p-6">
                    <p className="text-sm text-gray-500 dark:text-gray-400">
                      No partnership data available.
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto">

                    <table className="w-full min-w-[650px] text-left text-sm">

                      <thead>
                        <tr className="border-b border-gray-200 bg-gray-50 dark:border-gray-800 dark:bg-white/[0.02]">

                          <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                            Partnership
                          </th>

                          <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                            Batters
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            Runs
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            Balls
                          </th>

                          <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                            Status
                          </th>

                        </tr>
                      </thead>

                      <tbody>
                        {partnerships.map((partnership) => (
                          <tr
                            key={partnership.id}
                            className="border-b border-gray-100 last:border-0 dark:border-gray-800/60"
                          >

                            <td className="px-4 py-4 font-semibold text-gray-800 dark:text-white/90">
                              {partnership.partnershipNumber}
                            </td>

                            <td className="px-4 py-4 text-gray-800 dark:text-white/90">

                              <div>
                                {partnership.batterOneName}
                              </div>

                              <div className="text-xs text-gray-500 dark:text-gray-400">
                                {partnership.batterTwoName}
                              </div>

                            </td>

                            <td className="px-4 py-4 text-center font-bold text-gray-800 dark:text-white/90">
                              {partnership.runs}
                            </td>

                            <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                              {partnership.balls}
                            </td>

                            <td className="px-4 py-4 text-center">

                              {partnership.active ? (
                                <span className="rounded-full bg-green-100 px-3 py-1 text-xs font-semibold text-green-700 dark:bg-green-900/30 dark:text-green-400">
                                  Active
                                </span>
                              ) : (
                                <span className="rounded-full bg-gray-100 px-3 py-1 text-xs font-semibold text-gray-600 dark:bg-gray-800 dark:text-gray-400">
                                  Completed
                                </span>
                              )}

                            </td>

                          </tr>
                        ))}
                      </tbody>

                    </table>

                  </div>
                )}

              </div>

              {/* Fielding */}
      <div className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]">

        <div className="border-b border-gray-200 px-6 py-5 dark:border-gray-800">

          <h3 className="text-lg font-semibold text-gray-800 dark:text-white/90">
            Fielding
          </h3>

          <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
            Fielding performance
          </p>

        </div>

        {fieldingEvents.length === 0 ? (
          <div className="p-6">
            <p className="text-sm text-gray-500 dark:text-gray-400">
              No fielding events recorded.
            </p>
          </div>
        ) : (
          <div className="p-6">

            <div className="overflow-x-auto">

              <table className="w-full min-w-[600px] text-left text-sm">

                <thead>
                  <tr className="border-b border-gray-200 bg-gray-50 dark:border-gray-800 dark:bg-white/[0.02]">

                    <th className="px-4 py-3 font-semibold text-gray-500 dark:text-gray-400">
                      Fielder
                    </th>

                    <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                      Catches
                    </th>

                    <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                      Stumpings
                    </th>

                    <th className="px-4 py-3 text-center font-semibold text-gray-500 dark:text-gray-400">
                      Run Outs
                    </th>

                  </tr>
                </thead>

                <tbody>

                  {fieldingSummary.map((fielder) => (
                    <tr
                      key={fielder.fielderId}
                      className="border-b border-gray-100 last:border-0 dark:border-gray-800/60"
                    >

                      <td className="px-4 py-4 font-medium text-gray-800 dark:text-white/90">
                        {fielder.fielderName}
                      </td>

                      <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                        {fielder.catches}
                      </td>

                      <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                        {fielder.stumpings}
                      </td>

                      <td className="px-4 py-4 text-center text-gray-700 dark:text-gray-300">
                        {fielder.runOuts}
                      </td>

                    </tr>
                  ))}

                </tbody>

              </table>

            </div>

          </div>
        )}

      </div>

    </div>
  );
})}

</div>

{/* Back */}

<div className="flex justify-start pb-6">

  <Link
    to="/scorecards"
    className="inline-flex rounded-lg border border-gray-200 bg-white px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:bg-white/[0.03] dark:text-gray-300 dark:hover:bg-white/[0.05]"
  >
    ← Back to Scorecards
  </Link>

</div>

</div>
);
}