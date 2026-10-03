import { useEffect, useState } from "react";
import { useParams } from "react-router";

import {
  getMatchById,
  getMatchInnings,
  getMatchScorecard,
  getMatchLineup,
  startMatchInnings,
} from "../api/matchesApi";

import {
  getDeliveries,
  getInningsState,
  recordDelivery,
  setInningsState,
  undoLastDelivery,
} from "../api/scoringApi";

import type {
  DeliveryResponse,
  DismissalEnd,
  InningsResponse,
  InningsStateResponse,
  MatchLineupResponse,
  MatchResponse,
  ScorecardResponse,
  WicketType,
} from "../api/types";


type MultiRunExtra = "BYE" | "LEG_BYE";

export default function ScoreOperator() {
  const { matchId } = useParams<{ matchId: string }>();

  const [match, setMatch] = useState<MatchResponse | null>(null);
  const [innings, setInnings] = useState<InningsResponse | null>(null);
  const [scorecard, setScorecard] =
    useState<ScorecardResponse | null>(null);

  const [deliveries, setDeliveries] =
  useState<DeliveryResponse[]>([]);

  const [lastKnownOver, setLastKnownOver] =
    useState<number | null>(null);
  const [needsNextBowler, setNeedsNextBowler] =
    useState(false);
  const [matchLineup, setMatchLineup] =
    useState<MatchLineupResponse[]>([]);
  
  const [startingSecondInnings, setStartingSecondInnings] =
    useState(false);

  const [initializingInnings, setInitializingInnings] =
    useState(false);

  const [selectedOpeningStrikerId, setSelectedOpeningStrikerId] =
    useState<number | null>(null);

  const [selectedOpeningNonStrikerId, setSelectedOpeningNonStrikerId] =
    useState<number | null>(null);

  const [selectedOpeningBowlerId, setSelectedOpeningBowlerId] =
    useState<number | null>(null);
  
  const handleStartSecondInnings = async () => {
          if (!match) {
            setScoreError("Match is not available.");
            return;
          }

          if (!innings) {
            setScoreError("Current innings is not available.");
            return;
          }

          if (innings.status !== "COMPLETED") {
            setScoreError("The first innings is not completed yet.");
            return;
          }

          try {
            setStartingSecondInnings(true);
            setScoreError("");

            const secondInnings = await startMatchInnings(match.id, {
              inningsNumber: 2,
              battingTeamId: innings.bowlingTeamId,
              bowlingTeamId: innings.battingTeamId,
            });

            setInnings(secondInnings);

            await refreshScoreState(secondInnings.id);
          } catch (err) {
            setScoreError(
              err instanceof Error
                ? err.message
                : "Failed to start the second innings.",
            );
          } finally {
            setStartingSecondInnings(false);
          }
      };

  const [showWicketPanel, setShowWicketPanel] =
    useState(false);

  const [selectedWicketType, setSelectedWicketType] =
    useState<WicketType | null>(null);

  const [selectedDismissedPlayerId, setSelectedDismissedPlayerId] =
    useState<number | null>(null);

  const [selectedNewBatterId, setSelectedNewBatterId] =
    useState<number | null>(null);

  const [selectedFielderId, setSelectedFielderId] =
    useState<number | null>(null);

  const [selectedDismissalEnd, setSelectedDismissalEnd] =
  useState<DismissalEnd>("STRIKER");

  const [inningsState, setInningsStateData] =
    useState<InningsStateResponse | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [scoring, setScoring] = useState(false);
  const [scoreError, setScoreError] = useState("");
  
  const [selectedNextBowlerId, setSelectedNextBowlerId] =
    useState<number | null>(null);

  // No Ball uses +1 extra automatically plus selectable batter runs.
  // Bye and Leg Bye use a multi-run extra selector.
  const [selectedExtra, setSelectedExtra] =
  useState<"NO_BALL" | MultiRunExtra | null>(null);

  const numericMatchId = Number(matchId);

  const refreshScoreState = async (inningsId: number) => {
      const [
        updatedInnings,
        updatedState,
        updatedScorecard,
        updatedDeliveries,
      ] = await Promise.all([
        getMatchInnings(numericMatchId),
        getInningsState(inningsId),
        getMatchScorecard(numericMatchId),
        getDeliveries(inningsId),
      ]);

      const state = await getInningsState(inningsId);

      if (lastKnownOver === null) {
        setLastKnownOver(state.currentOver);
      } else if (state.currentOver > lastKnownOver) {
        setNeedsNextBowler(true);
        setLastKnownOver(state.currentOver);
      }

      setInningsStateData(state);

      const updatedLiveInnings =
        updatedInnings.find(
          (item) => item.id === inningsId,
        ) ??
        updatedInnings.find(
          (item) => item.status === "IN_PROGRESS",
        ) ??
        updatedInnings.find(
          (item) => item.status === "LIVE",
        ) ??
        updatedInnings[updatedInnings.length - 1];

      setInnings(updatedLiveInnings ?? null);
      setInningsStateData(updatedState);
      setScorecard(updatedScorecard);
      setDeliveries(updatedDeliveries);
    };

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

    async function loadScoreOperator() {
      try {
        setLoading(true);
        setError("");

        const [
          matchResponse,
          inningsResponse,
          scorecardResponse,
          lineupResponse,
        ] = await Promise.all([
          getMatchById(numericMatchId),
          getMatchInnings(numericMatchId),
          getMatchScorecard(numericMatchId),
           getMatchLineup(numericMatchId),
        ]);

        setMatch(matchResponse);
        setScorecard(scorecardResponse);
        setMatchLineup(lineupResponse);
        const liveInnings =
          inningsResponse.find(
            (item) => item.status === "IN_PROGRESS",
          ) ??
          inningsResponse.find(
            (item) => item.status === "LIVE",
          ) ??
          inningsResponse[
            inningsResponse.length - 1
          ];

        setInnings(liveInnings ?? null);

        if (liveInnings) {
          try {
            const state = await getInningsState(liveInnings.id);
            setInningsStateData(state);
          } catch (err) {
            const message =
              err instanceof Error ? err.message : "";

            if (
              message.includes("404") ||
              message.includes("Innings state not found")
            ) {
              // Newly started innings does not have state yet.
              // The user must initialize striker, non-striker and bowler.
              setInningsStateData(null);
              setLastKnownOver(null);
              setNeedsNextBowler(false);
            } else {
              throw err;
            }
          }
        }
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Failed to load score operator.",
        );
      } finally {
        setLoading(false);
      }

    }

    void loadScoreOperator();
  }, [matchId, numericMatchId]);

  /*
   * Common delivery submission.
   *
   * Cricket behavior:
   *
   * NONE:
   *   runsOffBat = selected runs
   *   extraRuns = 0
   *   legal delivery = backend decides
   *
   * WIDE:
   *   runsOffBat = 0
   *   extraRuns = 1
   *
   * NO_BALL:
   *   runsOffBat = 0
   *   extraRuns = 1
   *
   * BYE:
   *   runsOffBat = 0
   *   extraRuns = selected bye runs
   *
   * LEG_BYE:
   *   runsOffBat = 0
   *   extraRuns = selected leg-bye runs
   */
  const submitDelivery = async (
    runsOffBat: number,
    extraType:
      | "NONE"
      | "WIDE"
      | "NO_BALL"
      | "BYE"
      | "LEG_BYE",
    extraRuns: number,
  ) => {
    if (!innings || !inningsState) {
      setScoreError(
        "Innings state is not available.",
      );
      return;
    }

    try {
      setScoring(true);
      setScoreError("");

      await recordDelivery(innings.id, {
        batterId: inningsState.strikerId,
        nonStrikerId:
          inningsState.nonStrikerId,
        bowlerId:
          inningsState.currentBowlerId,
        runsOffBat,
        extraType,
        extraRuns,
        wicket: false,
      });

      await refreshScoreState(innings.id);

      setSelectedExtra(null);
    } catch (err) {
      setScoreError(
        err instanceof Error
          ? err.message
          : "Failed to record delivery.",
      );
    } finally {
      setScoring(false);
    }
  };

  // Normal batting runs.
  const handleRuns = async (runs: number) => {
    await submitDelivery(
      runs,
      "NONE",
      0,
    );
  };

// Wide is currently fixed at +1.
const handleWide = async () => {
  await submitDelivery(0, "WIDE", 1);
};

// No Ball always contributes +1 extra.
// The selected number is batter runs.
const handleNoBall = async (batterRuns: number) => {
  await submitDelivery(batterRuns, "NO_BALL", 1);
};

// Bye and Leg Bye can be multiple extra runs.
const handleMultiRunExtra = async (extraRuns: number) => {
    if (!selectedExtra || selectedExtra === "NO_BALL") {
        return;
    }

    await submitDelivery(0, selectedExtra, extraRuns);
};

const handleWicket = async () => {
    if (!innings || !inningsState) {
      setScoreError("Innings state is not available.");
      return;
    }

    if (!selectedWicketType) {
      setScoreError("Please select a wicket type.");
      return;
    }

    if (!selectedDismissedPlayerId) {
      setScoreError("Please select the dismissed player.");
      return;
    }

    if (!selectedNewBatterId) {
      setScoreError("Please select the new batter.");
      return;
    }

    if (
      (selectedWicketType === "CAUGHT" ||
        selectedWicketType === "STUMPED" ||
        selectedWicketType === "RUN_OUT") &&
      !selectedFielderId
    ) {
      setScoreError("Please select the fielder.");
      return;
    }

    if (
      selectedWicketType !== "RUN_OUT" &&
      selectedDismissalEnd === "NON_STRIKER"
    ) {
      setScoreError(
        "Only Run Out can dismiss the non-striker.",
      );
      return;
    }

    try {
      setScoring(true);
      setScoreError("");

      await recordDelivery(innings.id, {
        batterId: inningsState.strikerId,
        nonStrikerId: inningsState.nonStrikerId,
        bowlerId: inningsState.currentBowlerId,
        runsOffBat: 0,
        extraType: "NONE",
        extraRuns: 0,
        wicket: true,
        wicketType: selectedWicketType,
        dismissedPlayerId:
          selectedDismissedPlayerId,
        newBatterId: selectedNewBatterId,
        dismissalEnd:
          selectedWicketType === "RUN_OUT"
            ? selectedDismissalEnd
            : "STRIKER",
        fielderId:
          selectedFielderId ?? undefined,
      });

      await refreshScoreState(innings.id);

      setShowWicketPanel(false);
      setSelectedWicketType(null);
      setSelectedDismissedPlayerId(null);
      setSelectedNewBatterId(null);
      setSelectedFielderId(null);
      setSelectedDismissalEnd("STRIKER");
    } catch (err) {
      setScoreError(
        err instanceof Error
          ? err.message
          : "Failed to record wicket.",
      );
    } finally {
      setScoring(false);
    }
  };

  const handleUndo = async () => {
      if (!innings) {
        setScoreError("Innings is not available.");
        return;
      }

      try {
        setScoring(true);
        setScoreError("");

        await undoLastDelivery(innings.id);

        await refreshScoreState(innings.id);
      } catch (err) {
        setScoreError(
          err instanceof Error
            ? err.message
            : "Failed to undo last delivery.",
        );
      } finally {
        setScoring(false);
      }
    };

  const handleStartNextOver = async () => {
      if (!innings || !inningsState) {
        setScoreError("Innings state is not available.");
        return;
      }

      if (!selectedNextBowlerId) {
        setScoreError("Please select the next bowler.");
        return;
      }

      try {
        setScoring(true);
        setScoreError("");

        const updatedState = await setInningsState(
          innings.id,
          {
            strikerId: inningsState.strikerId,
            nonStrikerId: inningsState.nonStrikerId,
            bowlerId: selectedNextBowlerId,
          },
        );

        setInningsStateData(updatedState);
        setSelectedNextBowlerId(null);
        setNeedsNextBowler(false);
      } catch (err) {
        setScoreError(
          err instanceof Error
            ? err.message
            : "Failed to start the next over.",
        );
      } finally {
        setScoring(false);
      }
    };

  const handleInitializeInnings = async () => {
      if (!innings) {
        setScoreError("Innings is not available.");
        return;
      }

      if (innings.status !== "LIVE") {
        setScoreError("Innings is not live.");
        return;
      }

      if (
        !selectedOpeningStrikerId ||
        !selectedOpeningNonStrikerId ||
        !selectedOpeningBowlerId
      ) {
        setScoreError(
          "Please select striker, non-striker and bowler.",
        );
        return;
      }

      if (
        selectedOpeningStrikerId ===
        selectedOpeningNonStrikerId
      ) {
        setScoreError(
          "Striker and non-striker must be different players.",
        );
        return;
      }

      try {
        setInitializingInnings(true);
        setScoreError("");

        const initializedState = await setInningsState(
          innings.id,
          {
            strikerId: selectedOpeningStrikerId,
            nonStrikerId: selectedOpeningNonStrikerId,
            bowlerId: selectedOpeningBowlerId,
          },
        );

        setInningsStateData(initializedState);
        setLastKnownOver(initializedState.currentOver);
        setNeedsNextBowler(false);

        setSelectedOpeningStrikerId(null);
        setSelectedOpeningNonStrikerId(null);
        setSelectedOpeningBowlerId(null);
      } catch (err) {
        setScoreError(
          err instanceof Error
            ? err.message
            : "Failed to initialize innings.",
        );
      } finally {
        setInitializingInnings(false);
      }
    };

  if (loading) {
    return (
      <div className="p-6">
        <div className="rounded-2xl border border-gray-200 bg-white p-8 dark:border-gray-800 dark:bg-white/[0.03]">
          Loading Score Operator...
        </div>
      </div>
    );
  }

  if (error) {
      return (
      <div className="p-6">
        <div className="rounded-2xl border border-error-200 bg-error-50 p-6 text-error-700 dark:border-error-900/40 dark:bg-error-900/10 dark:text-error-400">
          {error}
        </div>
      </div>
    );
    
  }

  if (!match) {
    return (
      <div className="p-6">
        <div className="rounded-2xl border border-gray-200 bg-white p-8 dark:border-gray-800 dark:bg-white/[0.03]">
          Match not found.
        </div>
      </div>
    );
  }

  const battingInnings = innings
    ? scorecard?.innings.find(
        (item) =>
          item.inningsId === innings.id,
      )
    : null;

  const battingTeamLineup = matchLineup.filter(
    (player) =>
      player.teamId === innings?.battingTeamId &&
      player.playing,
  );

  const bowlingTeamLineup = matchLineup.filter(
    (player) =>
      player.teamId === innings?.bowlingTeamId &&
      player.playing,
  );

  const nextBowlerOptions = bowlingTeamLineup.filter(
    (player) =>
      player.playerId !== inningsState?.currentBowlerId,
  );

    const battedPlayerIds = new Set(
    battingInnings?.batting?.map(
      (player) => player.playerId,
    ) ?? [],
  );

  const nextBatterOptions = battingTeamLineup.filter(
    (player) =>
      !battedPlayerIds.has(player.playerId) &&
      player.playerId !== inningsState?.strikerId &&
      player.playerId !== inningsState?.nonStrikerId,
  );

  const legalBalls =
    battingInnings?.legalBalls ??
    innings?.legalBalls ??
    0;

  const totalOvers = match.totalOvers ?? 0;

  const remainingLegalBalls = Math.max(
    0,
    totalOvers * 6 - legalBalls,
  );

  const pendingOvers = `${Math.floor(remainingLegalBalls / 6)}.${
    remainingLegalBalls % 6
  }`;

  const remainingWickets = Math.max(
    0,
    10 - (battingInnings?.wickets ?? innings?.wickets ?? 0),
  );

  const firstInningsScore =
    scorecard?.innings.find(
      (item) => item.inningsNumber === 1,
    )?.totalRuns ?? null;

  const currentScore =
    battingInnings?.totalRuns ??
    innings?.totalRuns ??
    0;

  const runsToChase =
    innings?.inningsNumber === 2 &&
    firstInningsScore !== null
      ? Math.max(0, firstInningsScore + 1 - currentScore)
      : null;

  const currentOverNumber =
    inningsState?.currentOver ?? 1;

  const displayOverNumber = needsNextBowler
    ? Math.max(1, currentOverNumber - 1)
    : currentOverNumber;

  const currentOverDeliveries =
    deliveries.filter(
      (delivery) =>
        delivery.overNumber === displayOverNumber,
    );
  
  console.log("Over state:", {
    currentOver: inningsState?.currentOver,
    legalBallsInOver: inningsState?.legalBallsInOver,
    overComplete:
      inningsState?.legalBallsInOver === 6,
  });

  return (
    <div className="space-y-6 p-6">

      {/* Match Header */}
      <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-semibold text-gray-800 dark:text-white/90">
              Score Operator
            </h1>

            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              {match.name}
            </p>
          </div>

          <span className="inline-flex w-fit rounded-full bg-success-50 px-3 py-1 text-sm font-medium text-success-700 dark:bg-success-500/10 dark:text-success-400">
            {match.status}
          </span>
        </div>
      </div>

      {/* Score */}
      <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
        <div className="text-center">

          <p className="text-sm font-medium uppercase tracking-wide text-gray-500 dark:text-gray-400">
            {battingInnings?.battingTeamName ??
              innings?.battingTeamName ??
              "Batting Team"}
          </p>

          <div className="mt-3 text-5xl font-bold text-gray-800 dark:text-white/90">
            {battingInnings?.totalRuns ??
              innings?.totalRuns ??
              0}

            <span className="text-3xl">
              /
              {battingInnings?.wickets ??
                innings?.wickets ??
                0}
            </span>
          </div>

          <p className="mt-2 text-lg text-gray-500 dark:text-gray-400">
            {Math.floor(legalBalls / 6)}.
            {legalBalls % 6} overs
          </p>
        </div>
      </div>
      {/* Current Over */}
      <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-semibold text-gray-800 dark:text-white/90">
              Current Over
            </h2>

            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Over {displayOverNumber}
            </p>
          </div>

          <span className="text-sm font-medium text-gray-500 dark:text-gray-400">
            {currentOverDeliveries.length} delivery
            {currentOverDeliveries.length !== 1 ? "ies" : ""}
          </span>
          <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-3">
            <div className="rounded-xl border border-gray-200 bg-gray-50 p-4 dark:border-gray-700 dark:bg-gray-900/40">
              <p className="text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-gray-400">
                Pending Overs
              </p>
              <p className="mt-1 text-xl font-bold text-gray-800 dark:text-white/90">
                {pendingOvers}
              </p>
            </div>

            <div className="rounded-xl border border-gray-200 bg-gray-50 p-4 dark:border-gray-700 dark:bg-gray-900/40">
              <p className="text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-gray-400">
                Remaining Wickets
              </p>
              <p className="mt-1 text-xl font-bold text-gray-800 dark:text-white/90">
                {remainingWickets}
              </p>
            </div>

            {runsToChase !== null && (
              <div className="col-span-2 rounded-xl border border-blue-200 bg-blue-50 p-4 dark:border-blue-900/40 dark:bg-blue-950/20 sm:col-span-1">
                <p className="text-xs font-medium uppercase tracking-wide text-blue-600 dark:text-blue-400">
                  Runs to Chase
                </p>
                <p className="mt-1 text-xl font-bold text-blue-700 dark:text-blue-300">
                  {runsToChase}
                </p>
              </div>
            )}
          </div>
        </div>

        {currentOverDeliveries.length === 0 ? (
          <p className="mt-5 text-sm text-gray-500 dark:text-gray-400">
            No deliveries recorded in this over yet.
          </p>
        ) : (
          <div className="mt-5 flex flex-wrap gap-3">
            {currentOverDeliveries.map((delivery) => (
              <div
                key={delivery.id}
                className="min-w-[72px] rounded-xl border border-gray-200 bg-gray-50 px-3 py-3 text-center dark:border-gray-700 dark:bg-gray-900/40"
              >
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  {delivery.overNumber}.{delivery.ballInOver}
                </p>

                <p className="mt-1 text-lg font-bold text-gray-800 dark:text-white/90">
                  {delivery.wicket
                    ? "W"
                    : delivery.extraType !== "NONE"
                      ? `${delivery.totalRuns} ${delivery.extraType.replace("_", " ")}`
                      : delivery.totalRuns}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Current Players */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">

        {/* Striker */}
        <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Striker
          </p>

          <p className="mt-2 text-xl font-semibold text-gray-800 dark:text-white/90">
            {inningsState?.strikerName ?? "—"}
          </p>

          {inningsState && (
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Player #{inningsState.strikerId}
            </p>
          )}
        </div>

        {/* Non-Striker */}
        <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Non-Striker
          </p>

          <p className="mt-2 text-xl font-semibold text-gray-800 dark:text-white/90">
            {inningsState?.nonStrikerName ?? "—"}
          </p>

          {inningsState && (
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Player #{inningsState.nonStrikerId}
            </p>
          )}
        </div>

        {/* Bowler */}
        <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Current Bowler
          </p>

          <p className="mt-2 text-xl font-semibold text-gray-800 dark:text-white/90">
            {inningsState?.currentBowlerName ?? "—"}
          </p>

          {inningsState && (
            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Player #{inningsState.currentBowlerId}
            </p>
          )}
        </div>
      </div>

      {/* Add Next Bowler */}
      {needsNextBowler && (
      <div className="mt-5 rounded-xl border border-blue-200 bg-blue-50 p-4 dark:border-blue-900/40 dark:bg-blue-950/20">
        <div className="mb-4">
          <h3 className="text-sm font-semibold text-gray-800 dark:text-white">
            Over Complete
          </h3>

          <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
            Select the bowler for the next over.
          </p>
        </div>

        <label className="mb-2 block text-xs font-medium text-gray-700 dark:text-gray-300">
          Next Bowler
        </label>

        <select
          value={selectedNextBowlerId ?? ""}
          onChange={(event) =>
            setSelectedNextBowlerId(
              event.target.value
                ? Number(event.target.value)
                : null,
            )
          }
          className="block w-full min-w-0 rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-700 outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
        >
          <option value="">Select next bowler</option>

          {nextBowlerOptions.map((player) => (
            <option
              key={player.playerId}
              value={player.playerId}
            >
              {player.playerName}
              {player.jerseyNumber != null
                ? ` (#${player.jerseyNumber})`
                : ""}
            </option>
          ))}
        </select>

        <div className="mt-4 flex justify-start">
          <button
            type="button"
            onClick={handleStartNextOver}
            disabled={scoring || !selectedNextBowlerId}
            className="rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {scoring ? "Starting..." : "Start Next Over"}
          </button>
        </div>
      </div>
    )}

      {/* Scoring Controls */}
      <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">

        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-lg font-semibold text-gray-800 dark:text-white/90">
              Runs
            </h2>

            <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Record runs scored by the current striker.
            </p>
          </div>
          <div className="mt-4 flex justify-start">
            <button
              type="button"
              onClick={handleUndo}
              disabled={scoring}
              className="rounded-lg bg-amber-500 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-amber-600 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Undo Last Ball
            </button>
          </div>
          {scoring && (
            <span className="text-sm font-medium text-brand-600 dark:text-brand-400">
              Recording ball...
            </span>
          )}
        </div>

        {/* Normal Runs */}
        <div className="mt-6 grid grid-cols-3 gap-3 sm:grid-cols-6">
          {[0, 1, 2, 3, 4, 6].map(
            (runs) => (
              <button
                key={runs}
                type="button"
                disabled={scoring}
                onClick={() =>
                  void handleRuns(runs)
                }
                className="flex h-14 items-center justify-center rounded-xl border border-gray-200 bg-white text-xl font-bold text-gray-800 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:bg-gray-900 dark:text-white dark:hover:bg-white/[0.05]"
              >
                {runs}
              </button>
            ),
          )}
        </div>
        <div className="mt-4">
         <div className="mt-4 flex justify-start">
          <button
            type="button"
            onClick={() => {
              setShowWicketPanel(true);
              setScoreError("");
            }}
            disabled={scoring}
            className="rounded-lg bg-red-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
          >
            Wicket
          </button>
        </div>
          {showWicketPanel && (
          <div className="mt-4 rounded-xl border border-red-200 bg-red-50 p-4 dark:border-red-900/40 dark:bg-red-950/20">
            <div className="mb-4 flex items-center justify-between">
              <div>
                <h3 className="text-sm font-semibold text-gray-800 dark:text-white">
                  Record Wicket
                </h3>
                <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                  Select the dismissal details before recording the wicket.
                </p>
              </div>

              <button
                type="button"
                onClick={() => {
                  setShowWicketPanel(false);
                  setSelectedWicketType(null);
                  setSelectedDismissedPlayerId(null);
                  setSelectedNewBatterId(null);
                  setSelectedFielderId(null);
                  setSelectedDismissalEnd("STRIKER");
                }}
                className="rounded-lg px-3 py-1.5 text-xs font-medium text-gray-600 hover:bg-white dark:text-gray-300 dark:hover:bg-gray-800"
              >
                Cancel
              </button>
            </div>

            {/* Wicket Type */}
            <div>
              <label className="mb-2 block text-xs font-medium text-gray-700 dark:text-gray-300">
                Wicket Type
              </label>

              <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
                {[
                  "BOWLED",
                  "CAUGHT",
                  "LBW",
                  "RUN_OUT",
                  "STUMPED",
                  "HIT_WICKET",
                  "RETIRED_HURT",
                  "RETIRED_OUT",
                  "OBSTRUCTING_FIELD",
                ].map((type) => (
                  <button
                    key={type}
                    type="button"
                    onClick={() =>
                      setSelectedWicketType(type as WicketType)
                    }
                    className={`rounded-lg border px-3 py-2 text-xs font-medium ${
                      selectedWicketType === type
                        ? "border-red-500 bg-red-500 text-white"
                        : "border-gray-200 bg-white text-gray-700 hover:bg-gray-50 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-200"
                    }`}
                  >
                    {type.replace(/_/g, " ")}
                  </button>
                ))}
              </div>
            </div>

            {/* Dismissed Player */}
            <div className="mt-4">
              <label className="mb-2 block text-xs font-medium text-gray-700 dark:text-gray-300">
                Dismissed Player
              </label>

              <select
                value={selectedDismissedPlayerId ?? ""}
                onChange={(event) =>
                  setSelectedDismissedPlayerId(
                    event.target.value
                      ? Number(event.target.value)
                      : null,
                  )
                }
                className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-700 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
              >
                <option value="">Select dismissed player</option>

                {inningsState && (
                  <>
                    <option value={inningsState.strikerId}>
                      {inningsState.strikerName} (Striker)
                    </option>

                    <option value={inningsState.nonStrikerId}>
                      {inningsState.nonStrikerName} (Non-Striker)
                    </option>
                  </>
                )}
              </select>
            </div>

            {/* Dismissal End */}
            {selectedWicketType === "RUN_OUT" && (
              <div className="mt-4">
                <label className="mb-2 block text-xs font-medium text-gray-700 dark:text-gray-300">
                  Dismissal End
                </label>

                <div className="grid grid-cols-2 gap-2">
                  {(["STRIKER", "NON_STRIKER"] as DismissalEnd[]).map(
                    (end) => (
                      <button
                        key={end}
                        type="button"
                        onClick={() => setSelectedDismissalEnd(end)}
                        className={`rounded-lg border px-3 py-2 text-xs font-medium ${
                          selectedDismissalEnd === end
                            ? "border-red-500 bg-red-500 text-white"
                            : "border-gray-200 bg-white text-gray-700 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-200"
                        }`}
                      >
                        {end === "STRIKER"
                          ? "Striker End"
                          : "Non-Striker End"}
                      </button>
                    ),
                  )}
                </div>
              </div>
            )}

            {/* Fielder */}
            {(selectedWicketType === "CAUGHT" ||
              selectedWicketType === "STUMPED" ||
              selectedWicketType === "RUN_OUT") && (
              <div className="mt-4">
                <label className="mb-2 block text-xs font-medium text-gray-700 dark:text-gray-300">
                  Fielder
                </label>

                <select
                  value={selectedFielderId ?? ""}
                  onChange={(event) =>
                    setSelectedFielderId(
                      event.target.value
                        ? Number(event.target.value)
                        : null,
                    )
                  }
                  className="block w-full min-w-0 rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm text-gray-700 outline-none focus:border-brand-500 focus:ring-1 focus:ring-brand-500 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
                >
                  <option value="">Select fielder</option>

                  {bowlingTeamLineup.map((player) => (
                    <option
                      key={player.playerId}
                      value={player.playerId}
                    >
                      {player.playerName}
                      {player.jerseyNumber != null
                        ? ` (#${player.jerseyNumber})`
                        : ""}
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* New Batter */}
            <div className="mt-4">
              <label className="mb-2 block text-xs font-medium text-gray-700 dark:text-gray-300">
                New Batter
              </label>

              <select
                value={selectedNewBatterId ?? ""}
                onChange={(event) =>
                  setSelectedNewBatterId(
                    event.target.value
                      ? Number(event.target.value)
                      : null,
                  )
                }
                className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-700 dark:border-gray-700 dark:bg-gray-800 dark:text-white"
              >
                <option value="">Select new batter</option>

                {nextBatterOptions.map((player) => (
                    <option
                      key={player.playerId}
                      value={player.playerId}
                    >
                      {player.playerName}
                      {player.jerseyNumber != null
                        ? ` (#${player.jerseyNumber})`
                        : ""}
                    </option>
                  ))}
              </select>
            </div>

            {/* Record */}
            <div className="mt-5 flex justify-start">
              <button
                type="button"
                onClick={handleWicket}
                disabled={scoring}
                className="rounded-lg bg-red-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {scoring ? "Recording..." : "Record Wicket"}
              </button>
            </div>
          </div>
        )}
        </div>

        {/* Extras */}
        <div className="mt-6">
            <h3 className="text-sm font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400">
                Extras
            </h3>

            <div className="mt-3 grid grid-cols-2 gap-3 sm:grid-cols-4">
                {/* Wide = fixed +1 */}
                <button
                type="button"
                disabled={scoring}
                onClick={() => void handleWide()}
                className="flex h-14 items-center justify-center rounded-xl border border-gray-200 bg-white text-base font-semibold text-gray-800 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:bg-gray-900 dark:text-white dark:hover:bg-white/[0.05]"
                >
                Wide
                <span className="ml-1 text-sm text-gray-500 dark:text-gray-400">
                    +1
                </span>
                </button>

                {/* No Ball = +1 extra + batter runs */}
                <button
                type="button"
                disabled={scoring}
                onClick={() => setSelectedExtra("NO_BALL")}
                className="flex h-14 items-center justify-center rounded-xl border border-gray-200 bg-white text-base font-semibold text-gray-800 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:bg-gray-900 dark:text-white dark:hover:bg-white/[0.05]"
                >
                No Ball
                </button>

                {/* Bye = multi-run selector */}
                <button
                type="button"
                disabled={scoring}
                onClick={() => setSelectedExtra("BYE")}
                className="flex h-14 items-center justify-center rounded-xl border border-gray-200 bg-white text-base font-semibold text-gray-800 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:bg-gray-900 dark:text-white dark:hover:bg-white/[0.05]"
                >
                Bye
                </button>

                {/* Leg Bye = multi-run selector */}
                <button
                type="button"
                disabled={scoring}
                onClick={() => setSelectedExtra("LEG_BYE")}
                className="flex h-14 items-center justify-center rounded-xl border border-gray-200 bg-white text-base font-semibold text-gray-800 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:bg-gray-900 dark:text-white dark:hover:bg-white/[0.05]"
                >
                Leg Bye
                </button>
            </div>

            {/* No Ball / Bye / Leg Bye selector */}
            {selectedExtra && (
                <div className="mt-4 rounded-xl border border-gray-200 p-4 dark:border-gray-700">
                <div className="flex items-center justify-between gap-3">
                    <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                    {selectedExtra === "NO_BALL"
                        ? "No Ball — +1 extra + batter runs"
                        : selectedExtra === "BYE"
                        ? "Bye — select extra runs"
                        : "Leg Bye — select extra runs"}
                    </p>

                    <button
                    type="button"
                    disabled={scoring}
                    onClick={() => setSelectedExtra(null)}
                    className="text-sm font-medium text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
                    >
                    Cancel
                    </button>
                </div>

                <div className="mt-3 grid grid-cols-3 gap-3 sm:grid-cols-6">
                    {(selectedExtra === "NO_BALL"
                    ? [0, 1, 2, 3, 4, 6]
                    : [1, 2, 3, 4, 5, 6]
                    ).map((runs) => (
                    <button
                        key={runs}
                        type="button"
                        disabled={scoring}
                        onClick={() =>
                        selectedExtra === "NO_BALL"
                            ? void handleNoBall(runs)
                            : void handleMultiRunExtra(runs)
                        }
                        className="flex h-12 items-center justify-center rounded-lg border border-gray-200 bg-white font-semibold text-gray-800 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-gray-700 dark:bg-gray-900 dark:text-white dark:hover:bg-white/[0.05]"
                    >
                        {runs}
                    </button>
                    ))}
                </div>
                </div>
            )}
            </div>

        {/* Scoring Error */}
        {scoreError && (
          <div className="mt-4 rounded-lg border border-error-200 bg-error-50 px-4 py-3 text-sm text-error-700 dark:border-error-900/40 dark:bg-error-900/10 dark:text-error-400">
            {scoreError}
          </div>
        )}
      </div>

      {/* Innings Information */}
      <div className="rounded-2xl border border-gray-200 bg-white p-6 dark:border-gray-800 dark:bg-white/[0.03]">
        <h2 className="text-lg font-semibold text-gray-800 dark:text-white/90">
          Innings Information
        </h2>

        <div className="mt-4 grid grid-cols-2 gap-4 sm:grid-cols-5">
          <div>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Innings
            </p>
            <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
              {innings?.inningsNumber ?? "—"}
            </p>
          </div>

          <div>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Status
            </p>
            <div className="mt-1">
              <span
                className={`inline-flex rounded-full px-3 py-1 text-xs font-semibold ${
                  innings?.status === "LIVE"
                    ? "bg-green-100 text-green-700 dark:bg-green-900/30 dark:text-green-400"
                    : innings?.status === "COMPLETED"
                      ? "bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400"
                      : "bg-gray-100 text-gray-700 dark:bg-gray-800 dark:text-gray-300"
                }`}
              >
                {innings?.status ?? "—"}
              </span>
            </div>
          </div>

          <div>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Over
            </p>
            <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
              {inningsState?.currentOver ?? "—"}
            </p>
          </div>

          <div>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Legal Balls
            </p>
            <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
              {innings?.legalBalls ?? 0}
            </p>
          </div>

          <div>
            <p className="text-sm text-gray-500 dark:text-gray-400">
              Wickets
            </p>
            <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
              {innings?.wickets ?? 0}
            </p>
          </div>
        </div>
      </div>

      {innings?.status === "COMPLETED" &&
        innings.inningsNumber === 1 && (
          <div className="mt-6 rounded-2xl border border-blue-200 bg-blue-50 p-5 dark:border-blue-900/40 dark:bg-blue-950/20">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h2 className="text-lg font-semibold text-gray-800 dark:text-white/90">
                  First Innings Completed
                </h2>

                <p className="mt-1 text-sm text-gray-600 dark:text-gray-400">
                  {innings.battingTeamShortName}{" "}
                  <span className="font-semibold text-gray-800 dark:text-white/90">
                    {innings.totalRuns}/{innings.wickets}
                  </span>{" "}
                  · {innings.legalBalls} legal balls
                </p>

                <p className="mt-2 text-sm text-gray-600 dark:text-gray-400">
                  {innings.bowlingTeamShortName} will bat next.
                </p>
              </div>

              <button
                type="button"
                onClick={handleStartSecondInnings}
                disabled={startingSecondInnings}
                className="rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {startingSecondInnings
                  ? "Starting..."
                  : "Start 2nd Innings"}
              </button>
            </div>
          </div>
        )}
        {innings?.status === "LIVE" && !inningsState && (
          <div className="mt-6 rounded-2xl border border-blue-200 bg-blue-50 p-5 dark:border-blue-900/40 dark:bg-blue-950/20">
            <h2 className="text-lg font-semibold text-gray-800 dark:text-white/90">
              {innings.inningsNumber === 1
              ? "First Innings Setup"
              : "Second Innings Setup"}
            </h2>

            <p className="mt-1 text-sm text-gray-600 dark:text-gray-400">
              Select the opening striker, non-striker and bowler before scoring.
            </p>

            <div className="mt-5 grid grid-cols-1 gap-4 md:grid-cols-3">
              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  Striker
                </label>

                <select
                  value={selectedOpeningStrikerId ?? ""}
                  onChange={(event) =>
                    setSelectedOpeningStrikerId(
                      event.target.value
                        ? Number(event.target.value)
                        : null,
                    )
                  }
                  className="mt-2 w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm dark:border-gray-700 dark:bg-gray-900"
                >
                  <option value="">Select striker</option>

                  {battingTeamLineup.map((player) => (
                    <option
                      key={player.playerId}
                      value={player.playerId}
                    >
                      {player.playerName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  Non-Striker
                </label>

                <select
                  value={selectedOpeningNonStrikerId ?? ""}
                  onChange={(event) =>
                    setSelectedOpeningNonStrikerId(
                      event.target.value
                        ? Number(event.target.value)
                        : null,
                    )
                  }
                  className="mt-2 w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm dark:border-gray-700 dark:bg-gray-900"
                >
                  <option value="">Select non-striker</option>

                  {battingTeamLineup.map((player) => (
                    <option
                      key={player.playerId}
                      value={player.playerId}
                      disabled={
                        player.playerId === selectedOpeningStrikerId
                      }
                    >
                      {player.playerName}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  Bowler
                </label>

                <select
                  value={selectedOpeningBowlerId ?? ""}
                  onChange={(event) =>
                    setSelectedOpeningBowlerId(
                      event.target.value
                        ? Number(event.target.value)
                        : null,
                    )
                  }
                  className="mt-2 w-full rounded-lg border border-gray-300 bg-white px-3 py-2.5 text-sm dark:border-gray-700 dark:bg-gray-900"
                >
                  <option value="">Select bowler</option>

                  {bowlingTeamLineup.map((player) => (
                    <option
                      key={player.playerId}
                      value={player.playerId}
                    >
                      {player.playerName}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            <div className="mt-5 flex justify-end">
              <button
                type="button"
                onClick={handleInitializeInnings}
                disabled={initializingInnings}
                className="rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {initializingInnings
                  ? "Initializing..."
                  : "Initialize Innings"}
              </button>
            </div>
          </div>
        )}
        {innings?.status === "COMPLETED" &&
          innings.inningsNumber === 2 && (
            <div className="mt-6 rounded-2xl border border-green-200 bg-green-50 p-5 dark:border-green-900/40 dark:bg-green-950/20">
              <div>
                <h2 className="text-lg font-semibold text-gray-800 dark:text-white/90">
                  Match Completed
                </h2>

                <p className="mt-1 text-sm text-gray-600 dark:text-gray-400">
                  Both innings have been completed.
                </p>
              </div>

              {/* Innings Summary */}
              <div className="mt-5 grid grid-cols-1 gap-4 md:grid-cols-2">
                {scorecard?.innings.map((scorecardInnings) => (
                  <div
                    key={scorecardInnings.inningsId}
                    className="rounded-xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]"
                  >
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-500 dark:text-gray-400">
                          {scorecardInnings.inningsNumber === 1
                            ? "1st Innings"
                            : "2nd Innings"}
                        </p>

                        <h3 className="mt-1 text-lg font-semibold text-gray-800 dark:text-white/90">
                          {scorecardInnings.battingTeamName}
                        </h3>
                      </div>

                      <span className="rounded-full bg-gray-100 px-3 py-1 text-xs font-semibold text-gray-700 dark:bg-gray-800 dark:text-gray-300">
                        COMPLETED
                      </span>
                    </div>

                    <div className="mt-5 flex items-end gap-3">
                      <span className="text-3xl font-bold text-gray-900 dark:text-white">
                        {scorecardInnings.totalRuns}/
                        {scorecardInnings.wickets}
                      </span>

                      <span className="pb-1 text-sm text-gray-500 dark:text-gray-400">
                        ({Math.floor(scorecardInnings.legalBalls / 6)}.
                        {scorecardInnings.legalBalls % 6} overs)
                      </span>
                    </div>

                    <div className="mt-4 grid grid-cols-2 gap-3">
                      <div className="rounded-lg bg-gray-50 p-3 dark:bg-gray-900/40">
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          Runs
                        </p>

                        <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
                          {scorecardInnings.totalRuns}
                        </p>
                      </div>

                      <div className="rounded-lg bg-gray-50 p-3 dark:bg-gray-900/40">
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          Wickets
                        </p>

                        <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
                          {scorecardInnings.wickets}
                        </p>
                      </div>

                      <div className="rounded-lg bg-gray-50 p-3 dark:bg-gray-900/40">
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          Legal Balls
                        </p>

                        <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
                          {scorecardInnings.legalBalls}
                        </p>
                      </div>

                      <div className="rounded-lg bg-gray-50 p-3 dark:bg-gray-900/40">
                        <p className="text-xs text-gray-500 dark:text-gray-400">
                          Overs
                        </p>

                        <p className="mt-1 font-semibold text-gray-800 dark:text-white/90">
                          {Math.floor(scorecardInnings.legalBalls / 6)}.
                          {scorecardInnings.legalBalls % 6}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>

              {/* Result */}
              {scorecard?.result && (
                <div className="mt-5 rounded-xl border border-green-200 bg-white p-5 dark:border-green-900/40 dark:bg-white/[0.03]">
                  <p className="text-sm font-medium text-gray-500 dark:text-gray-400">
                    Match Result
                  </p>

                  <p className="mt-2 text-xl font-bold text-green-700 dark:text-green-400">
                    {scorecard.result.resultText ?? "Result recorded"}
                  </p>
                </div>
              )}
              <div className="mt-5 rounded-xl border border-green-200 bg-white p-4 dark:border-green-900/40 dark:bg-white/[0.03]">
                <p className="text-sm font-medium text-gray-700 dark:text-gray-300">
                  Match completed. Scoring is now locked.
                </p>
              </div>
            </div>
          )}        

    </div>
  );
}
