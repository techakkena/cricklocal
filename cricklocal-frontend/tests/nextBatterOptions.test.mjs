import test from "node:test";
import assert from "node:assert/strict";
import { getNextBatterOptions } from "../src/utils/nextBatterOptions.ts";

test("excludes players who appeared in surviving deliveries", () => {
  const lineup = Array.from({ length: 10 }, (_, index) => ({
    playerId: index + 1,
    teamId: 100,
    playing: true,
  }));

  const deliveries = [
    {
      batterId: 1,
      nonStrikerId: 2,
      dismissedPlayerId: 2,
    },
    {
      batterId: 3,
      nonStrikerId: 1,
      dismissedPlayerId: null,
    },
  ];

  const options = getNextBatterOptions(
    lineup,
    100,
    deliveries,
    4,
    5,
  );

  assert.deepEqual(
    options.map((player) => player.playerId),
    [6, 7, 8, 9, 10],
  );
});

test("excludes non-playing players, other-team players and current batters", () => {
  const lineup = [
    { playerId: 1, teamId: 100, playing: true },
    { playerId: 2, teamId: 100, playing: false },
    { playerId: 3, teamId: 200, playing: true },
    { playerId: 4, teamId: 100, playing: true },
    { playerId: 5, teamId: 100, playing: true },
    { playerId: 6, teamId: 100, playing: true },
  ];

  const options = getNextBatterOptions(
    lineup,
    100,
    [],
    4,
    5,
  );

  assert.deepEqual(
    options.map((player) => player.playerId),
    [1, 6],
  );
});
