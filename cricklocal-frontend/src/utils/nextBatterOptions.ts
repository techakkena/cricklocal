
export interface BatterLineupOption {
  playerId: number;
  teamId: number;
  playing: boolean;
}

export interface BatterDeliveryParticipation {
  batterId: number;
  nonStrikerId: number;
  dismissedPlayerId: number | null;
  wicketType?: string | null;
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
  const retiredHurtPlayerIds = new Set<number>();
  const dismissedPlayerIds = new Set<number>();

  for (const delivery of deliveries) {
    appearedPlayerIds.add(delivery.batterId);
    appearedPlayerIds.add(delivery.nonStrikerId);

    const playerId = delivery.dismissedPlayerId;
    if (playerId == null) continue;

    if (delivery.wicketType === "RETIRED_HURT") {
      retiredHurtPlayerIds.add(playerId);
    } else if (delivery.wicketType != null) {
      dismissedPlayerIds.add(playerId);
      retiredHurtPlayerIds.delete(playerId);
    }
  }

  return lineup.filter((player) => {
    const isEligibleTeamPlayer =
      player.teamId === battingTeamId && player.playing;

    const isCurrentlyAtCrease =
      player.playerId === strikerId ||
      player.playerId === nonStrikerId;

    const canReturnAfterRetiredHurt =
      retiredHurtPlayerIds.has(player.playerId) &&
      !dismissedPlayerIds.has(player.playerId);

    return (
      isEligibleTeamPlayer &&
      !isCurrentlyAtCrease &&
      (
        !appearedPlayerIds.has(player.playerId) ||
        canReturnAfterRetiredHurt
      )
    );
  });
}
