package com.cricklocal.dto;

public class LeaderboardEntryResponse {

    private Long rank;
    private Long playerId;
    private String playerName;
    private Long primaryValue;
    private Long secondaryValue;

    public Long getRank() {
        return rank;
    }

    public void setRank(Long rank) {
        this.rank = rank;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Long getPrimaryValue() {
        return primaryValue;
    }

    public void setPrimaryValue(Long primaryValue) {
        this.primaryValue = primaryValue;
    }

    public Long getSecondaryValue() {
        return secondaryValue;
    }

    public void setSecondaryValue(Long secondaryValue) {
        this.secondaryValue = secondaryValue;
    }
}