import { useEffect, useState } from "react";
import { useParams } from "react-router";

import {
  getInningsDeliveries,
  getInningsState,
  getMatchScorecard,
  validateScoreDisplayAccess,
} from "../../api/matchesApi";

import type {
  DeliveryResponse,
  InningsStateResponse,
  ScorecardResponse,
} from "../../api/types";

function formatOvers(legalBalls: number): string {
  return `${Math.floor(legalBalls / 6)}.${legalBalls % 6}`;
}

function getStatusLabel(scorecard: ScorecardResponse): string {
  const liveInnings = scorecard.innings.find(
    (innings) =>
      innings.status === "LIVE" ||
      innings.status === "IN_PROGRESS",
  );

  if (scorecard.innings.length >= 2) {
    const secondInnings = scorecard.innings.find(
      (innings) => innings.inningsNumber === 2,
    );

    if (secondInnings?.status === "COMPLETED") {
      return "MATCH COMPLETED";
    }
  }

  if (liveInnings) {
    return "LIVE";
  }

  const latestInnings =
    scorecard.innings[scorecard.innings.length - 1];

  if (latestInnings?.status === "COMPLETED") {
    return "INNINGS BREAK";
  }

  return "MATCH CENTRE";
}

function getDeliveryRuns(delivery: DeliveryResponse): number {
  const item = delivery as DeliveryResponse & {
    runsOffBat?: number;
    extraRuns?: number;
  };

  return (
    (item.runsOffBat ?? 0) +
    (item.extraRuns ?? 0)
  );
}

function getDeliveryLabel(delivery: DeliveryResponse): string {
  const item = delivery as DeliveryResponse & {
    runsOffBat?: number;
    extraType?: string | null;
    extraRuns?: number;
    wicket?: boolean;
  };

  const runsOffBat = item.runsOffBat ?? 0;
  const extraRuns = item.extraRuns ?? 0;
  const extraType = item.extraType ?? null;
  const wicket = item.wicket ?? false;

  if (wicket) {
    return "W";
  }

  if (extraType === "WIDE") {
    return extraRuns > 1
      ? `Wd+${extraRuns - 1}`
      : "Wd";
  }

  if (extraType === "NO_BALL") {
    return runsOffBat > 0
      ? `Nb+${runsOffBat}`
      : "Nb";
  }

  if (extraType === "BYE") {
    return extraRuns > 0 ? `B${extraRuns}` : "B";
  }

  if (extraType === "LEG_BYE") {
    return extraRuns > 0 ? `Lb${extraRuns}` : "Lb";
  }

  return String(runsOffBat);
}

function getDeliveryTitle(delivery: DeliveryResponse): string {
  const item = delivery as DeliveryResponse & {
    wicketType?: string | null;
    dismissalType?: string | null;
  };

  if (item.wicketType) {
    return item.wicketType;
  }

  if (item.dismissalType) {
    return item.dismissalType;
  }

  return getDeliveryLabel(delivery);
}

function getCurrentOverNumber(
  legalBalls: number,
  deliveries: DeliveryResponse[],
): number {
  if (deliveries.length === 0) {
    return Math.floor(legalBalls / 6) + 1;
  }

  const lastDelivery =
    deliveries[deliveries.length - 1];

  const lastOverNumber =
    (
      lastDelivery as DeliveryResponse & {
        overNumber?: number;
      }
    ).overNumber;

  /*
   * If exactly six legal balls have completed an over,
   * keep showing the completed over until the next
   * delivery is recorded.
   */
  if (legalBalls > 0 && legalBalls % 6 === 0) {
    return lastOverNumber ?? Math.floor(legalBalls / 6);
  }

  return (
    lastOverNumber ??
    Math.floor(legalBalls / 6) + 1
  );
}

function getCurrentOverDeliveries(
  deliveries: DeliveryResponse[],
  overNumber: number,
): DeliveryResponse[] {
  return deliveries.filter((delivery) => {
    const item = delivery as DeliveryResponse & {
      overNumber?: number;
    };

    return item.overNumber === overNumber;
  });
}

export default function ScoreDisplay() {
  const { displayToken } = useParams<{
    displayToken: string;
  }>();

  const [scorecard, setScorecard] =
    useState<ScorecardResponse | null>(null);

  const [deliveries, setDeliveries] = useState<
    DeliveryResponse[]
  >([]);

  const [inningsState, setInningsState] =
    useState<InningsStateResponse | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [lastUpdated, setLastUpdated] =
    useState<Date | null>(null);

  const [darkMode, setDarkMode] = useState(true);

  const [expandedInnings, setExpandedInnings] =
    useState<number | null>(null);

  useEffect(() => {
    if (!displayToken) {
      setError("Display token is missing.");
      setLoading(false);
      return;
    }

    const token = displayToken;

    async function loadDisplay() {
      try {
        setError("");

        const access =
          await validateScoreDisplayAccess(token);

        const response =
          await getMatchScorecard(access.matchId);

        setScorecard(response);

        const liveInnings =
          response.innings.find(
            (innings) =>
              innings.status === "LIVE" ||
              innings.status === "IN_PROGRESS",
          ) ??
          response.innings[
            response.innings.length - 1
          ];

        if (liveInnings) {
          const [
            inningsDeliveries,
            currentState,
          ] = await Promise.all([
            getInningsDeliveries(
              liveInnings.inningsId,
            ),
            getInningsState(
              liveInnings.inningsId,
            ),
          ]);

          setDeliveries(inningsDeliveries);
          setInningsState(currentState);

          setExpandedInnings(
            liveInnings.inningsNumber,
          );
        } else {
          setDeliveries([]);
          setInningsState(null);

          if (
            response.innings.length > 0
          ) {
            setExpandedInnings(
              response.innings[
                response.innings.length - 1
              ].inningsNumber,
            );
          }
        }

        setLastUpdated(new Date());
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Unable to load live display.",
        );
      } finally {
        setLoading(false);
      }
    }

    void loadDisplay();

    const intervalId = window.setInterval(() => {
      void loadDisplay();
    }, 5000);

    return () => {
      window.clearInterval(intervalId);
    };
  }, [displayToken]);

  if (loading) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-950">
        <p className="text-sm text-gray-300">
          Loading live display...
        </p>
      </div>
    );
  }

  if (error && !scorecard) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-950 px-4">
        <div className="w-full max-w-md rounded-2xl border border-gray-800 bg-gray-900 p-6 text-center">
          <h1 className="text-xl font-semibold text-white">
            Unable to open live display
          </h1>

          <p className="mt-3 text-sm text-gray-400">
            {error}
          </p>
        </div>
      </div>
    );
  }

  if (!scorecard) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-gray-950">
        <p className="text-sm text-gray-400">
          No scorecard data available.
        </p>
      </div>
    );
  }

  const liveInnings =
    scorecard.innings.find(
      (innings) =>
        innings.status === "LIVE" ||
        innings.status === "IN_PROGRESS",
    ) ??
    scorecard.innings[
      scorecard.innings.length - 1
    ];

  const isSecondInnings =
    liveInnings?.inningsNumber === 2;

  const firstInnings =
    scorecard.innings.find(
      (innings) => innings.inningsNumber === 1,
    );

  const target =
    isSecondInnings && firstInnings
      ? firstInnings.totalRuns + 1
      : null;

  const runsRequired =
    target !== null && liveInnings
      ? Math.max(
          target - liveInnings.totalRuns,
          0,
        )
      : null;

  const statusLabel =
    getStatusLabel(scorecard);

  const currentOverNumber =
    liveInnings
      ? getCurrentOverNumber(
          liveInnings.legalBalls,
          deliveries,
        )
      : 1;

  const currentOverDeliveries =
    getCurrentOverDeliveries(
      deliveries,
      currentOverNumber,
    );

  const strikerId =
    inningsState?.strikerId;

  const nonStrikerId =
    inningsState?.nonStrikerId;

  const currentBowlerId =
    inningsState?.currentBowlerId;

  const strikerName =
    inningsState?.strikerName ?? "—";

  const nonStrikerName =
    inningsState?.nonStrikerName ?? "—";

  const currentBowlerName =
    inningsState?.currentBowlerName ?? "—";

  const pageClasses = darkMode
    ? "min-h-screen bg-gray-950 text-white"
    : "min-h-screen bg-gray-100 text-gray-900";

  const cardClasses = darkMode
    ? "border-gray-800 bg-gray-900"
    : "border-gray-200 bg-white";

  const secondaryText = darkMode
    ? "text-gray-400"
    : "text-gray-500";

  return (
    <div className={pageClasses}>
      <div className="mx-auto min-h-screen max-w-7xl px-3 py-4 sm:px-5 sm:py-6 lg:px-8">

        {/* Header */}
        <header className="mb-4 flex items-center justify-between gap-3">
          <div className="min-w-0">
            {scorecard.seriesName && (
              <p className="truncate text-xs font-semibold uppercase tracking-[0.2em] text-brand-500 sm:text-sm">
                {scorecard.seriesName}
              </p>
            )}

            <h1 className="mt-1 truncate text-lg font-bold sm:text-2xl">
              {scorecard.matchName}
            </h1>
          </div>

          <button
            type="button"
            onClick={() =>
              setDarkMode((value) => !value)
            }
            className={`shrink-0 rounded-xl border px-3 py-2 text-xs font-semibold transition ${
              darkMode
                ? "border-gray-700 bg-gray-900 text-gray-200 hover:bg-gray-800"
                : "border-gray-300 bg-white text-gray-700 hover:bg-gray-50"
            }`}
          >
            {darkMode
              ? "☀ Light"
              : "🌙 Dark"}
          </button>
        </header>

        {/* Live score hero */}
        <section
          className={`overflow-hidden rounded-3xl border ${cardClasses}`}
        >
          <div className="px-4 py-5 sm:px-8 sm:py-8">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex items-center gap-2">
                <span
                  className={`inline-flex items-center gap-2 rounded-full px-3 py-1 text-xs font-bold ${
                    statusLabel === "LIVE"
                      ? "bg-success-500/15 text-success-500"
                      : "bg-brand-500/15 text-brand-500"
                  }`}
                >
                  {statusLabel === "LIVE" && (
                    <span className="h-2 w-2 animate-pulse rounded-full bg-success-500" />
                  )}

                  {statusLabel}
                </span>
              </div>

              <div
                className={`text-xs ${secondaryText}`}
              >
                {lastUpdated
                  ? `Updated ${lastUpdated.toLocaleTimeString()}`
                  : "Updating..."}
              </div>
            </div>

            {liveInnings && (
              <div className="mt-6 text-center">
                <p
                  className={`text-xs font-semibold uppercase tracking-widest ${secondaryText}`}
                >
                  Batting
                </p>

                <h2 className="mt-2 text-2xl font-bold sm:text-4xl">
                  {liveInnings.battingTeamName}
                </h2>

                <div className="mt-4">
                  <span className="text-5xl font-black tracking-tight sm:text-7xl">
                    {liveInnings.totalRuns}
                    <span className="mx-1">
                      /
                    </span>
                    {liveInnings.wickets}
                  </span>
                </div>

                <p
                  className={`mt-3 text-base font-semibold sm:text-xl ${secondaryText}`}
                >
                  {formatOvers(
                    liveInnings.legalBalls,
                  )}{" "}
                  overs
                </p>
              </div>
            )}

            {liveInnings && (
              <div className="mt-7 grid grid-cols-2 gap-3 sm:gap-4">
                <div
                  className={`rounded-2xl border p-4 text-center ${cardClasses}`}
                >
                  <p
                    className={`text-xs uppercase tracking-wide ${secondaryText}`}
                  >
                    Batting
                  </p>

                  <p className="mt-1 truncate text-sm font-bold sm:text-lg">
                    {liveInnings.battingTeamName}
                  </p>
                </div>

                <div
                  className={`rounded-2xl border p-4 text-center ${cardClasses}`}
                >
                  <p
                    className={`text-xs uppercase tracking-wide ${secondaryText}`}
                  >
                    Bowling
                  </p>

                  <p className="mt-1 truncate text-sm font-bold sm:text-lg">
                    {liveInnings.bowlingTeamName}
                  </p>
                </div>
              </div>
            )}
          </div>

          {/* Chase panel */}
          {isSecondInnings &&
            target !== null &&
            runsRequired !== null &&
            liveInnings && (
              <div className="border-t border-gray-800 px-4 py-5 text-center sm:px-8">
                {runsRequired > 0 ? (
                  <>
                    <p
                      className={`text-xs font-semibold uppercase tracking-widest ${secondaryText}`}
                    >
                      Chasing
                    </p>

                    <p className="mt-2 text-xl font-bold sm:text-2xl">
                      {liveInnings.battingTeamName} need{" "}
                      <span className="text-brand-500">
                        {runsRequired}
                      </span>{" "}
                      runs to win
                    </p>

                    <p
                      className={`mt-1 text-sm ${secondaryText}`}
                    >
                      Target {target}
                    </p>
                  </>
                ) : (
                  <>
                    <p className="text-xl font-bold text-success-500 sm:text-2xl">
                      Target reached
                    </p>

                    <p
                      className={`mt-1 text-sm ${secondaryText}`}
                    >
                      {liveInnings.battingTeamName} have
                      completed the chase.
                    </p>
                  </>
                )}
              </div>
            )}
        </section>

        {/* Current Players */}
        {liveInnings && (
          <section className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-3">
            <div className="md:col-span-3">
              <p
                className={`text-xs font-semibold uppercase tracking-widest ${secondaryText}`}
              >
                Batting — {liveInnings.battingTeamName}
              </p>
            </div>

            {/* Striker */}
            <div
              className={`rounded-2xl border p-5 ${cardClasses}`}
            >
              <p
                className={`text-xs font-semibold uppercase tracking-widest ${secondaryText}`}
              >
                Striker
              </p>

              <h3 className="mt-2 truncate text-xl font-bold">
                {strikerName}
              </h3>

              {strikerId &&
                liveInnings.batting && (
                  (() => {
                    const batter =
                      liveInnings.batting.find(
                        (player) => player.playerId === strikerId,
                    );

                    if (!batter) {
                      return null;
                    }

                    return (
                      <div className="mt-4 grid grid-cols-3 gap-2">
                        <div>
                          <p
                            className={`text-xs ${secondaryText}`}
                          >
                            Runs
                          </p>

                          <p className="mt-1 text-lg font-bold">
                            {batter.runs}
                          </p>
                        </div>

                        <div>
                          <p
                            className={`text-xs ${secondaryText}`}
                          >
                            Balls
                          </p>

                          <p className="mt-1 text-lg font-bold">
                            {batter.ballsFaced}
                          </p>
                        </div>

                        <div>
                          <p
                            className={`text-xs ${secondaryText}`}
                          >
                            SR
                          </p>

                          <p className="mt-1 text-lg font-bold">
                            {batter.ballsFaced > 0
                              ? (
                                  (batter.runs /
                                    batter.ballsFaced) *
                                  100
                                ).toFixed(1)
                              : "0.0"}
                          </p>
                        </div>
                      </div>
                    );
                  })()
                )}
            </div>

            {/* Non-striker */}
            <div
              className={`rounded-2xl border p-5 ${cardClasses}`}
            >
              <p
                className={`text-xs font-semibold uppercase tracking-widest ${secondaryText}`}
              >
                Non-Striker
              </p>

              <h3 className="mt-2 truncate text-xl font-bold">
                {nonStrikerName}
              </h3>

              {nonStrikerId &&
                liveInnings.batting && (
                  (() => {
                    const batter =
                      liveInnings.batting.find(
                        (player) =>
                          player.playerId ===
                          nonStrikerId,
                      );

                    if (!batter) {
                      return null;
                    }

                    return (
                      <div className="mt-4 grid grid-cols-3 gap-2">
                        <div>
                          <p
                            className={`text-xs ${secondaryText}`}
                          >
                            Runs
                          </p>

                          <p className="mt-1 text-lg font-bold">
                            {batter.runs}
                          </p>
                        </div>

                        <div>
                          <p
                            className={`text-xs ${secondaryText}`}
                          >
                            Balls
                          </p>

                          <p className="mt-1 text-lg font-bold">
                            {batter.ballsFaced}
                          </p>
                        </div>

                        <div>
                          <p
                            className={`text-xs ${secondaryText}`}
                          >
                            SR
                          </p>

                          <p className="mt-1 text-lg font-bold">
                            {batter.ballsFaced > 0
                              ? (
                                  (batter.runs /
                                    batter.ballsFaced) *
                                  100
                                ).toFixed(1)
                              : "0.0"}
                          </p>
                        </div>
                      </div>
                    );
                  })()
                )}
            </div>
            <div className="md:col-span-3">
              <p
                className={`text-xs font-semibold uppercase tracking-widest ${secondaryText}`}
              >
                Bowling — {liveInnings.bowlingTeamName}
              </p>
            </div>

            {/* Current Bowler */}
            <div
              className={`rounded-2xl border p-5 ${cardClasses}`}
            >
              <p
                className={`text-xs font-semibold uppercase tracking-widest ${secondaryText}`}
              >
                Current Bowler
              </p>

              <h3 className="mt-2 truncate text-xl font-bold">
                {currentBowlerName}
              </h3>

              {currentBowlerId &&
                liveInnings.bowling && (
                  (() => {
                    const bowler =
                      liveInnings.bowling.find(
                        (player) =>
                          player.playerId ===
                          currentBowlerId,
                      );

                    if (!bowler) {
                      return null;
                    }

                    return (
                      <>
                        <div className="mt-4 grid grid-cols-3 gap-2">
                          <div>
                            <p
                              className={`text-xs ${secondaryText}`}
                            >
                              Overs
                            </p>

                            <p className="mt-1 text-lg font-bold">
                              {bowler.overs}
                            </p>
                          </div>

                          <div>
                            <p
                              className={`text-xs ${secondaryText}`}
                            >
                              Runs
                            </p>

                            <p className="mt-1 text-lg font-bold">
                              {bowler.runsConceded}
                            </p>
                          </div>

                          <div>
                            <p
                              className={`text-xs ${secondaryText}`}
                            >
                              Wkts
                            </p>

                            <p className="mt-1 text-lg font-bold">
                              {bowler.wickets}
                            </p>
                          </div>
                        </div>

                        <p
                          className={`mt-3 text-xs ${secondaryText}`}
                        >
                          Economy{" "}
                          {bowler.ballsBowled > 0
                            ? (
                                (bowler.runsConceded /
                                  bowler.ballsBowled) *
                                6
                              ).toFixed(1)
                            : "0.0"}
                        </p>
                      </>
                    );
                  })()
                )}
            </div>
          </section>
        )}

        {/* Current Over */}
        {liveInnings && (
          <section
            className={`mt-4 rounded-2xl border p-5 ${cardClasses}`}
          >
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p
                  className={`text-xs font-semibold uppercase tracking-widest ${secondaryText}`}
                >
                  Current Over
                </p>

                <h2 className="mt-1 text-2xl font-black">
                  Over {currentOverNumber}
                </h2>
              </div>

              <div className="text-right">
                <p
                  className={`text-xs ${secondaryText}`}
                >
                  Over Runs
                </p>

                <p className="mt-1 text-2xl font-black">
                  {currentOverDeliveries.reduce(
                    (total, delivery) =>
                      total +
                      getDeliveryRuns(
                        delivery,
                      ),
                    0,
                  )}
                </p>
              </div>
            </div>

            <div className="mt-5 flex flex-wrap gap-3">
              {currentOverDeliveries.length >
              0 ? (
                currentOverDeliveries.map(
                  (delivery, index) => {
                    const label =
                      getDeliveryLabel(
                        delivery,
                      );

                    const title =
                      getDeliveryTitle(
                        delivery,
                      );

                    return (
                      <div
                        key={
                          delivery.id ??
                          `${currentOverNumber}-${index}`
                        }
                        title={title}
                        className={`flex h-12 min-w-12 items-center justify-center rounded-full border px-3 text-sm font-black ${
                          label === "W"
                            ? "border-red-500 bg-red-500/15 text-red-500"
                            : label.startsWith("Wd")
                              ? "border-yellow-500 bg-yellow-500/15 text-yellow-500"
                              : label.startsWith("Nb")
                                ? "border-orange-500 bg-orange-500/15 text-orange-500"
                                : label.startsWith("B") ||
                                    label.startsWith("Lb")
                                  ? "border-blue-500 bg-blue-500/15 text-blue-500"
                                  : "border-gray-700 bg-gray-800 text-white"
                        }`}
                      >
                        {label}
                      </div>
                    );
                  },
                )
              ) : (
                <p
                  className={`text-sm ${secondaryText}`}
                >
                  No deliveries recorded in this over yet.
                </p>
              )}
            </div>

            <div className="mt-4 grid grid-cols-1 gap-2 text-sm sm:grid-cols-3">
              <div
                className={`rounded-xl border p-3 ${cardClasses}`}
              >
                <span
                  className={secondaryText}
                >
                  Ball
                </span>

                <span className="ml-2 font-bold">
                  {currentOverDeliveries.length}
                </span>
              </div>

              <div
                className={`rounded-xl border p-3 ${cardClasses}`}
              >
                <span
                  className={secondaryText}
                >
                  Legal balls
                </span>

                <span className="ml-2 font-bold">
                  {inningsState?.legalBallsInOver ??
                    0}
                </span>
              </div>

              <div
                className={`rounded-xl border p-3 ${cardClasses}`}
              >
                <span
                  className={secondaryText}
                >
                  Over
                </span>

                <span className="ml-2 font-bold">
                  {currentOverNumber}
                </span>
              </div>
            </div>
          </section>
        )}

        {/* Innings */}
        <section className="mt-4 space-y-3">
          {scorecard.innings.map(
            (innings) => {
              const expanded =
                expandedInnings ===
                innings.inningsNumber;

              return (
                <div
                  key={innings.inningsId}
                  className={`overflow-hidden rounded-2xl border ${cardClasses}`}
                >
                  {/* Innings Header */}
                  <button
                    type="button"
                    onClick={() =>
                      setExpandedInnings(
                        expanded
                          ? null
                          : innings.inningsNumber,
                      )
                    }
                    className="flex w-full items-center justify-between gap-4 px-4 py-4 text-left sm:px-6"
                  >
                    <div className="min-w-0">
                      <p
                        className={`text-xs uppercase tracking-wide ${secondaryText}`}
                      >
                        Innings{" "}
                        {innings.inningsNumber}
                      </p>

                      <p className="mt-1 truncate text-base font-bold sm:text-xl">
                        {innings.battingTeamName}
                      </p>
                    </div>

                    <div className="flex shrink-0 items-center gap-3">
                      <div className="text-right">
                        <p className="text-lg font-black sm:text-2xl">
                          {innings.totalRuns}/
                          {innings.wickets}
                        </p>

                        <p
                          className={`text-xs ${secondaryText}`}
                        >
                          {formatOvers(
                            innings.legalBalls,
                          )}{" "}
                          ov
                        </p>
                      </div>

                      <span
                        className={`text-lg transition-transform ${
                          expanded
                            ? "rotate-180"
                            : ""
                        }`}
                      >
                        ▼
                      </span>
                    </div>
                  </button>

                  {/* Expanded Innings */}
                  {expanded && (
                    <div className="border-t border-gray-800 px-4 py-5 sm:px-6">
                      {/* Innings Summary */}
                      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
                        <div>
                          <p
                            className={`text-xs ${secondaryText}`}
                          >
                            Score
                          </p>

                          <p className="mt-1 text-xl font-bold">
                            {innings.totalRuns}/
                            {innings.wickets}
                          </p>
                        </div>

                        <div>
                          <p
                            className={`text-xs ${secondaryText}`}
                          >
                            Overs
                          </p>

                          <p className="mt-1 text-xl font-bold">
                            {formatOvers(
                              innings.legalBalls,
                            )}
                          </p>
                        </div>

                        <div>
                          <p
                            className={`text-xs ${secondaryText}`}
                          >
                            Status
                          </p>

                          <p className="mt-1 text-sm font-bold">
                            {innings.status}
                          </p>
                        </div>

                        <div>
                          <p
                            className={`text-xs ${secondaryText}`}
                          >
                            Bowling
                          </p>

                          <p className="mt-1 truncate text-sm font-bold">
                            {innings.bowlingTeamName}
                          </p>
                        </div>
                      </div>

                      {/* Batting Scorecard */}
                      <div className="mt-6">
                        <div className="mb-3 flex items-center justify-between">
                          <h3 className="text-base font-bold sm:text-lg">
                            Batting
                          </h3>

                          <span
                            className={`text-xs ${secondaryText}`}
                          >
                            {innings.batting
                              ?.length ?? 0}{" "}
                            players
                          </span>
                        </div>

                        {innings.batting &&
                        innings.batting.length >
                          0 ? (
                          <div className="overflow-x-auto rounded-xl border border-gray-800">
                            <table className="w-full min-w-[650px] text-left">
                              <thead
                                className={
                                  darkMode
                                    ? "bg-gray-800/70"
                                    : "bg-gray-50"
                                }
                              >
                                <tr>
                                  <th className="px-3 py-3 text-xs font-semibold uppercase tracking-wide">
                                    Player
                                  </th>

                                  <th className="px-3 py-3 text-right text-xs font-semibold uppercase tracking-wide">
                                    R
                                  </th>

                                  <th className="px-3 py-3 text-right text-xs font-semibold uppercase tracking-wide">
                                    B
                                  </th>

                                  <th className="px-3 py-3 text-right text-xs font-semibold uppercase tracking-wide">
                                    4s
                                  </th>

                                  <th className="px-3 py-3 text-right text-xs font-semibold uppercase tracking-wide">
                                    6s
                                  </th>

                                  <th className="px-3 py-3 text-right text-xs font-semibold uppercase tracking-wide">
                                    SR
                                  </th>

                                  <th className="px-3 py-3 text-right text-xs font-semibold uppercase tracking-wide">
                                    Status
                                  </th>
                                </tr>
                              </thead>

                              <tbody>
                                {innings.batting.map(
                                  (player) => {
                                    const strikeRate =
                                      player.ballsFaced >
                                      0
                                        ? (
                                            (player.runs /
                                              player.ballsFaced) *
                                            100
                                          ).toFixed(1)
                                        : "0.0";

                                    const isStriker =
                                      innings.inningsId ===
                                        liveInnings?.inningsId &&
                                      player.playerId === strikerId

                                    const isNonStriker =
                                      innings.inningsId ===
                                        liveInnings?.inningsId &&
                                      player.playerId === nonStrikerId

                                    return (
                                      <tr
                                        key={player.id}
                                        className={
                                          darkMode
                                            ? "border-t border-gray-800"
                                            : "border-t border-gray-200"
                                        }
                                      >
                                        <td className="px-3 py-3">
                                          <div className="flex items-center gap-2">
                                            <span className="font-semibold">
                                              {player.playerName}
                                            </span>

                                            {isStriker && (
                                              <span className="rounded-full bg-brand-500/15 px-2 py-0.5 text-[10px] font-bold text-brand-500">
                                                STRIKER
                                              </span>
                                            )}

                                            {isNonStriker && (
                                              <span className="rounded-full bg-gray-500/15 px-2 py-0.5 text-[10px] font-bold text-gray-400">
                                                NON-STRIKER
                                              </span>
                                            )}
                                          </div>

                                          {player.dismissed &&
                                            player.dismissalType && (
                                              <div
                                                className={`mt-1 text-xs ${secondaryText}`}
                                              >
                                                {
                                                  player.dismissalType
                                                }

                                                {player.dismissedByPlayerName
                                                  ? ` · ${player.dismissedByPlayerName}`
                                                  : ""}
                                              </div>
                                            )}
                                        </td>

                                        <td className="px-3 py-3 text-right font-bold">
                                          {player.runs}
                                        </td>

                                        <td
                                          className={`px-3 py-3 text-right ${secondaryText}`}
                                        >
                                          {
                                            player.ballsFaced
                                          }
                                        </td>

                                        <td
                                          className={`px-3 py-3 text-right ${secondaryText}`}
                                        >
                                          {
                                            player.fours
                                          }
                                        </td>

                                        <td
                                          className={`px-3 py-3 text-right ${secondaryText}`}
                                        >
                                          {
                                            player.sixes
                                          }
                                        </td>

                                        <td className="px-3 py-3 text-right font-semibold">
                                          {
                                            strikeRate
                                          }
                                        </td>

                                        <td className="px-3 py-3 text-right">
                                          {player.dismissed ? (
                                            <span className="text-xs font-semibold text-red-500">
                                              OUT
                                            </span>
                                          ) : (
                                            <span className="text-xs font-semibold text-success-500">
                                              NOT OUT
                                            </span>
                                          )}
                                        </td>
                                      </tr>
                                    );
                                  },
                                )}
                              </tbody>
                            </table>
                          </div>
                        ) : (
                          <p
                            className={`text-sm ${secondaryText}`}
                          >
                            No batting data available.
                          </p>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              );
            },
          )}
        </section>

        {/* Refresh indicator */}
        <div
          className={`mt-5 text-center text-xs ${secondaryText}`}
        >
          Live display refreshes automatically every 5 seconds
        </div>
      </div>
    </div>
  );
}