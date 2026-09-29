import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router";
import PageBreadcrumb from "../components/common/PageBreadCrumb";
import PageMeta from "../components/common/PageMeta";
import Badge from "../components/ui/badge/Badge";
import {
  addPlayerToMatch,
  finalizePlayingXI,
  getFinalizedPlayingXI,
  getMatchById,
  getMatchLineup,
} from "../api/matchesApi";
import { getPlayers } from "../api/playersApi";
import type {
  MatchLineupResponse,
  MatchResponse,
  PlayerResponse,
  PlayingXIResponse,
} from "../api/types";

type TeamState = {
  teamId: number;
  teamName: string;
  shortName: string;
};

export default function MatchPreparation() {
  const { matchId } = useParams<{ matchId: string }>();
  const numericMatchId = Number(matchId);

  const [match, setMatch] = useState<MatchResponse | null>(null);
  const [players, setPlayers] = useState<PlayerResponse[]>([]);
  const [lineup, setLineup] = useState<MatchLineupResponse[]>([]);
  const [finalized, setFinalized] = useState<
    Record<number, PlayingXIResponse | null>
  >({});

  const [captains, setCaptains] = useState<Record<number, number | "">>(
    {},
  );
  const [wicketKeepers, setWicketKeepers] = useState<
    Record<number, number | "">
  >({});

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [savingPlayer, setSavingPlayer] = useState<number | null>(null);
  const [finalizingTeam, setFinalizingTeam] = useState<number | null>(null);

  useEffect(() => {
    if (!Number.isInteger(numericMatchId) || numericMatchId <= 0) {
      setError("Invalid match ID.");
      setLoading(false);
      return;
    }

    async function loadPreparation() {
      try {
        setLoading(true);
        setError("");

        const [matchResponse, playersResponse, lineupResponse] =
          await Promise.all([
            getMatchById(numericMatchId),
            getPlayers(),
            getMatchLineup(numericMatchId),
          ]);

        setMatch(matchResponse);
        setPlayers(playersResponse);
        setLineup(lineupResponse);

        const captainState: Record<number, number | ""> = {};
        const wicketKeeperState: Record<number, number | ""> = {};
        const finalizedState: Record<
          number,
          PlayingXIResponse | null
        > = {};

        await Promise.all(
          matchResponse.teams.map(async (team) => {
            const teamLineup = lineupResponse.filter(
              (item) => item.teamId === team.teamId,
            );

            const captain = teamLineup.find(
              (item) => item.captain && item.playing,
            );

            const wicketKeeper = teamLineup.find(
              (item) => item.wicketKeeper && item.playing,
            );

            captainState[team.teamId] = captain?.playerId ?? "";
            wicketKeeperState[team.teamId] =
              wicketKeeper?.playerId ?? "";

            try {
              const finalizedXI = await getFinalizedPlayingXI(
                numericMatchId,
                team.teamId,
              );

              finalizedState[team.teamId] = finalizedXI;
            } catch {
              // A 404 means this team's Playing XI has not been locked yet.
              finalizedState[team.teamId] = null;
            }
          }),
        );

        setCaptains(captainState);
        setWicketKeepers(wicketKeeperState);
        setFinalized(finalizedState);
      } catch (err) {
        setError(
          err instanceof Error
            ? err.message
            : "Unable to load match preparation.",
        );
      } finally {
        setLoading(false);
      }
    }

    void loadPreparation();
  }, [numericMatchId]);

  const teams: TeamState[] = useMemo(
    () =>
      match?.teams.map((team) => ({
        teamId: team.teamId,
        teamName: team.teamName,
        shortName: team.shortName,
      })) ?? [],
    [match],
  );

  function getTeamLineup(teamId: number) {
    return lineup.filter((item) => item.teamId === teamId);
  }

  function getTeamPlayers(teamId: number) {
    return players.filter(
      (player) =>
        player.active &&
        player.teams.some((team) => team.teamId === teamId),
    );
  }

  function isPlaying(teamId: number, playerId: number) {
    return getTeamLineup(teamId).some(
      (item) => item.playerId === playerId && item.playing,
    );
  }

  async function savePlayer(
    teamId: number,
    playerId: number,
    playing: boolean,
  ) {
    if (finalized[teamId]) {
      return;
    }

    const currentCount = getTeamLineup(teamId).filter(
      (item) => item.playing,
    ).length;

    if (playing && currentCount >= 11) {
      setError("A Playing XI can contain only 11 players.");
      return;
    }

    try {
      setSavingPlayer(playerId);
      setError("");

      const response = await addPlayerToMatch(numericMatchId, {
        teamId,
        playerId,
        playing,
        captain: captains[teamId] === playerId,
        wicketKeeper: wicketKeepers[teamId] === playerId,
      });

      setLineup((current) => {
        const existingIndex = current.findIndex(
          (item) =>
            item.teamId === teamId &&
            item.playerId === playerId,
        );

        if (existingIndex === -1) {
          return [...current, response];
        }

        const updated = [...current];
        updated[existingIndex] = response;
        return updated;
      });

      if (!playing) {
        if (captains[teamId] === playerId) {
          setCaptains((current) => ({
            ...current,
            [teamId]: "",
          }));
        }

        if (wicketKeepers[teamId] === playerId) {
          setWicketKeepers((current) => ({
            ...current,
            [teamId]: "",
          }));
        }
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to update the Playing XI.",
      );
    } finally {
      setSavingPlayer(null);
    }
  }

  async function saveRole(
    teamId: number,
    playerId: number,
    role: "captain" | "wicketKeeper",
  ) {
    if (finalized[teamId]) {
      return;
    }

    if (!isPlaying(teamId, playerId)) {
      setError("Only a Playing XI player can be assigned this role.");
      return;
    }

    const teamLineup = getTeamLineup(teamId);

    const existingCaptain =
      role === "captain"
        ? captains[teamId]
        : teamLineup.find((item) => item.wicketKeeper)?.playerId ?? "";

    const existingWicketKeeper =
      role === "wicketKeeper"
        ? wicketKeepers[teamId]
        : teamLineup.find((item) => item.wicketKeeper)?.playerId ?? "";

    try {
      setSavingPlayer(playerId);
      setError("");

      const response = await addPlayerToMatch(numericMatchId, {
        teamId,
        playerId,
        playing: true,
        captain:
          role === "captain"
            ? playerId === Number(playerId)
            : existingCaptain === playerId,
        wicketKeeper:
          role === "wicketKeeper"
            ? playerId === Number(playerId)
            : existingWicketKeeper === playerId,
      });

      setLineup((current) => {
        const updated = current.map((item) => {
          if (item.teamId !== teamId || !item.playing) {
            return item;
          }

          if (role === "captain") {
            return {
              ...item,
              captain: item.playerId === playerId,
            };
          }

          return {
            ...item,
            wicketKeeper: item.playerId === playerId,
          };
        });

        const index = updated.findIndex(
          (item) =>
            item.teamId === teamId &&
            item.playerId === playerId,
        );

        if (index === -1) {
          return [...updated, response];
        }

        updated[index] = response;
        return updated;
      });

      if (role === "captain") {
        setCaptains((current) => ({
          ...current,
          [teamId]: playerId,
        }));
      } else {
        setWicketKeepers((current) => ({
          ...current,
          [teamId]: playerId,
        }));
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to update player role.",
      );
    } finally {
      setSavingPlayer(null);
    }
  }

  async function handleFinalize(teamId: number) {
    const teamLineup = getTeamLineup(teamId).filter(
      (item) => item.playing,
    );

    if (teamLineup.length !== 11) {
      setError("Exactly 11 players are required before finalizing.");
      return;
    }

    const captainId = captains[teamId];

    if (!captainId) {
      setError("Please select a captain before finalizing.");
      return;
    }

    if (!teamLineup.some((item) => item.playerId === captainId)) {
      setError("Captain must be one of the 11 playing players.");
      return;
    }

    const wicketKeeperId = wicketKeepers[teamId];

    if (!wicketKeeperId) {
      setError("Please select a wicket keeper before finalizing.");
      return;
    }

    if (
      !teamLineup.some(
        (item) => item.playerId === wicketKeeperId,
      )
    ) {
      setError(
        "Wicket Keeper must be one of the 11 playing players.",
      );
      return;
    }

    try {
      setFinalizingTeam(teamId);
      setError("");

      const response = await finalizePlayingXI(
        numericMatchId,
        teamId,
      );

      setFinalized((current) => ({
        ...current,
        [teamId]: response,
      }));
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Unable to finalize the Playing XI.",
      );
    } finally {
      setFinalizingTeam(null);
    }
  }

  if (loading) {
    return (
      <>
        <PageMeta
          title="Match Preparation | CricketLocal"
          description="Prepare and finalize the Playing XI."
        />
        <PageBreadcrumb pageTitle="Match Preparation" />

        <div className="rounded-2xl border border-gray-200 bg-white p-8 text-center text-sm text-gray-500 dark:border-gray-800 dark:bg-white/[0.03] dark:text-gray-400">
          Loading match preparation...
        </div>
      </>
    );
  }

  if (!match) {
    return (
      <>
        <PageMeta
          title="Match Preparation | CricketLocal"
          description="Prepare and finalize the Playing XI."
        />
        <PageBreadcrumb pageTitle="Match Preparation" />

        <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
          {error || "Match not found."}
        </div>
      </>
    );
  }

  const bothFinalized =
    teams.length === 2 &&
    teams.every((team) => Boolean(finalized[team.teamId]));

  return (
    <>
      <PageMeta
        title={`${match.name} | Match Preparation`}
        description="Prepare and finalize the Playing XI."
      />

      <PageBreadcrumb pageTitle="Match Preparation" />

      <div className="space-y-6">
        {/* Match summary */}
        <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] sm:p-6">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-brand-500">
                {match.seriesName ?? "Standalone Match"}
              </p>

              <h3 className="mt-1 text-xl font-semibold text-gray-800 dark:text-white/90">
                {match.name}
              </h3>

              <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                {match.teams[0]?.shortName ?? match.teams[0]?.teamName}{" "}
                vs{" "}
                {match.teams[1]?.shortName ?? match.teams[1]?.teamName}
                {" · "}
                {match.format}
                {" · "}
                {match.totalOvers} overs
              </p>

              {match.venue && (
                <p className="mt-1 text-xs text-gray-400 dark:text-gray-500">
                  {match.venue}
                </p>
              )}
            </div>

            <Link
              to="/matches"
              className="inline-flex h-10 items-center justify-center rounded-lg border border-gray-300 px-4 text-sm font-medium text-gray-700 transition hover:bg-gray-50 dark:border-gray-700 dark:text-gray-300 dark:hover:bg-white/[0.03]"
            >
              Back to Matches
            </Link>
          </div>
        </div>

        {error && (
          <div className="rounded-xl border border-error-200 bg-error-50 p-4 text-sm text-error-600 dark:border-error-500/20 dark:bg-error-500/10 dark:text-error-400">
            {error}
          </div>
        )}

        {/* Readiness */}
        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
          {teams.map((team) => {
            const count = getTeamLineup(team.teamId).filter(
              (item) => item.playing,
            ).length;

            const isFinalized = Boolean(finalized[team.teamId]);

            return (
              <div
                key={team.teamId}
                className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]"
              >
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-semibold text-gray-800 dark:text-white/90">
                      {team.shortName}
                    </p>

                    <p className="mt-1 text-xs text-gray-500 dark:text-gray-400">
                      {team.teamName}
                    </p>
                  </div>

                  <Badge
                    size="sm"
                    color={isFinalized ? "success" : "warning"}
                  >
                    {isFinalized
                      ? "XI Finalized"
                      : `${count} / 11`}
                  </Badge>
                </div>
              </div>
            );
          })}
        </div>

        {/* Playing XI */}
        <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
          {teams.map((team) => {
            const teamLineup = getTeamLineup(team.teamId);
            const playingCount = teamLineup.filter(
              (item) => item.playing,
            ).length;

            const isFinalized = Boolean(finalized[team.teamId]);

            return (
              <div
                key={team.teamId}
                className="rounded-2xl border border-gray-200 bg-white dark:border-gray-800 dark:bg-white/[0.03]"
              >
                <div className="border-b border-gray-100 px-5 py-5 dark:border-gray-800">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="font-semibold text-gray-800 dark:text-white/90">
                        {team.shortName}
                      </h4>

                      <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                        Select Playing XI
                      </p>
                    </div>

                    <Badge
                      size="sm"
                      color={
                        isFinalized
                          ? "success"
                          : playingCount === 11
                            ? "info"
                            : "warning"
                      }
                    >
                      {isFinalized
                        ? "Finalized"
                        : `${playingCount} / 11`}
                    </Badge>
                  </div>
                </div>

                <div className="p-5">
                  <div className="max-h-[420px] space-y-2 overflow-y-auto pr-1">
                    {getTeamPlayers(team.teamId).map((player) => {
                      const playing = isPlaying(
                        team.teamId,
                        player.id,
                      );

                      return (
                        <label
                          key={player.id}
                          className={`flex items-center justify-between rounded-lg border px-3 py-2.5 ${
                            playing
                              ? "border-brand-300 bg-brand-50 dark:border-brand-500/30 dark:bg-brand-500/10"
                              : "border-gray-200 dark:border-gray-800"
                          } ${
                            isFinalized
                              ? "cursor-default opacity-75"
                              : "cursor-pointer"
                          }`}
                        >
                          <div className="flex items-center gap-3">
                            <input
                              type="checkbox"
                              checked={playing}
                              disabled={
                                isFinalized ||
                                savingPlayer === player.id
                              }
                              onChange={() =>
                                void savePlayer(
                                  team.teamId,
                                  player.id,
                                  !playing,
                                )
                              }
                              className="h-4 w-4 rounded border-gray-300 text-brand-500 focus:ring-brand-500"
                            />

                            <div>
                              <p className="text-sm font-medium text-gray-800 dark:text-white/90">
                                {player.displayName}
                              </p>

                              <p className="text-xs text-gray-500 dark:text-gray-400">
                                {player.role}
                              </p>
                            </div>
                          </div>

                          <div className="flex gap-1">
                            {teamLineup.some(
                              (item) =>
                                item.playerId === player.id &&
                                item.captain,
                            ) && (
                              <Badge size="sm" color="info">
                                C
                              </Badge>
                            )}

                            {teamLineup.some(
                              (item) =>
                                item.playerId === player.id &&
                                item.wicketKeeper,
                            ) && (
                              <Badge size="sm" color="success">
                                WK
                              </Badge>
                            )}
                          </div>
                        </label>
                      );
                    })}
                  </div>

                  <div className="mt-5 grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <div>
                      <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                        Captain
                      </label>

                      <select
                        value={captains[team.teamId] ?? ""}
                        disabled={isFinalized}
                        onChange={(event) => {
                          const playerId = Number(event.target.value);

                          if (!playerId) {
                            return;
                          }

                          void saveRole(
                            team.teamId,
                            playerId,
                            "captain",
                          );
                        }}
                        className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-3 text-sm text-gray-800 outline-none focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                      >
                        <option value="">Select captain</option>

                        {teamLineup
                          .filter((item) => item.playing)
                          .map((item) => (
                            <option
                              key={item.playerId}
                              value={item.playerId}
                            >
                              {item.playerName}
                            </option>
                          ))}
                      </select>
                    </div>

                    <div>
                      <label className="mb-1.5 block text-sm font-medium text-gray-700 dark:text-gray-300">
                        Wicket Keeper
                      </label>

                      <select
                        value={wicketKeepers[team.teamId] ?? ""}
                        disabled={isFinalized}
                        onChange={(event) => {
                          const playerId = Number(event.target.value);

                          if (!playerId) {
                            return;
                          }

                          void saveRole(
                            team.teamId,
                            playerId,
                            "wicketKeeper",
                          );
                        }}
                        className="h-11 w-full rounded-lg border border-gray-300 bg-transparent px-3 text-sm text-gray-800 outline-none focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90"
                      >
                        <option value="">
                          Select wicket keeper
                        </option>

                        {teamLineup
                          .filter((item) => item.playing)
                          .map((item) => (
                            <option
                              key={item.playerId}
                              value={item.playerId}
                            >
                              {item.playerName}
                            </option>
                          ))}
                      </select>
                    </div>
                  </div>

                  <button
                    type="button"
                    disabled={
                      isFinalized ||
                      finalizingTeam === team.teamId ||
                      playingCount !== 11 ||
                      !captains[team.teamId] ||
                      !wicketKeepers[team.teamId]
                    }
                    onClick={() =>
                      void handleFinalize(team.teamId)
                    }
                    className="mt-5 inline-flex h-11 w-full items-center justify-center rounded-lg bg-brand-500 px-4 text-sm font-medium text-white transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-50"
                  >
                    {isFinalized
                      ? "✓ Playing XI Finalized"
                      : finalizingTeam === team.teamId
                        ? "Finalizing..."
                        : "Finalize Playing XI"}
                  </button>
                </div>
              </div>
            );
          })}
        </div>

        {/* Workflow gates */}
        <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h4 className="font-semibold text-gray-800 dark:text-white/90">
                  Support Verification
                </h4>

                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  CricketLocal support must verify both Playing XIs
                  before the toss.
                </p>
              </div>

              <Badge size="sm" color="warning">
                {bothFinalized ? "Pending" : "Locked"}
              </Badge>
            </div>

            <div className="mt-4 rounded-lg bg-gray-50 p-3 text-xs text-gray-500 dark:bg-white/[0.03] dark:text-gray-400">
              {bothFinalized
                ? "Both teams have finalized their XI. Support verification is the next step."
                : "Locked until both teams finalize their Playing XI."}
            </div>
          </div>

          <div className="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03]">
            <div className="flex items-start justify-between gap-4">
              <div>
                <h4 className="font-semibold text-gray-800 dark:text-white/90">
                  Toss
                </h4>

                <p className="mt-1 text-sm text-gray-500 dark:text-gray-400">
                  Toss becomes available after support confirmation.
                </p>
              </div>

              <Badge size="sm" color="warning">
                Locked
              </Badge>
            </div>

            <div className="mt-4 rounded-lg bg-gray-50 p-3 text-xs text-gray-500 dark:bg-white/[0.03] dark:text-gray-400">
              Toss is intentionally locked until the Support Team
              confirms both Playing XIs.
            </div>
          </div>
        </div>
      </div>
    </>
  );
}