package com.cricklocal.dto;

import jakarta.validation.constraints.NotNull;

public class AddPlayerToMatchRequest {

    @NotNull(message = "Team ID is required")
    private Long teamId;

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotNull(message = "Playing status is required")
    private Boolean playing;

    private Boolean captain = false;

    private Boolean wicketKeeper = false;

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public Boolean getPlaying() {
        return playing;
    }

    public void setPlaying(Boolean playing) {
        this.playing = playing;
    }

    public Boolean getCaptain() {
        return captain;
    }

    public void setCaptain(Boolean captain) {
        this.captain = captain;
    }

    public Boolean getWicketKeeper() {
        return wicketKeeper;
    }

    public void setWicketKeeper(Boolean wicketKeeper) {
        this.wicketKeeper = wicketKeeper;
    }
}