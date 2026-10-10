export interface BatterLineupOption {
  playerId: number;
  teamId: number;
  playing: boolean;
}

export interface BatterDeliveryParticipation {
  batterId: number;
  nonStrikerId: number;
  dismissedPlayerId: number | null;
}

export function getNextBatterOptions<
  T extends BatterLineupOption,
>(
  lineup: T[],
  battingTeamId: number | undefined,
  deliveries: BatterDeliveryParticipation[],
  strikerId: number | null | undefined,
  nonStrikerId: number | null | undefined,
): T[] {
  const appearedPlayerIds = new Set<number>();

  for (const delivery of deliveries) {
    appearedPlayerIds.add(delivery.batterId);
    appearedPlayerIds.add(delivery.nonStrikerId);

    if (delivery.dismissedPlayerId != null) {
      appearedPlayerIds.add(delivery.dismissedPlayerId);
    }
  }

  return lineup.filter(
    (player) =>
      player.teamId === battingTeamId &&
      player.playing &&
      !appearedPlayerIds.has(player.playerId) &&
      player.playerId !== strikerId &&
      player.playerId !== nonStrikerId,
  );
}
