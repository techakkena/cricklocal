package com.cricklocal.dto;

public class FieldingStatsResponse {

    private Long playerId;
    private String playerName;

    private Long catches;
    private Long runOuts;
    private Long stumpings;

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

    public Long getCatches() {
        return catches;
    }

    public void setCatches(Long catches) {
        this.catches = catches;
    }

    public Long getRunOuts() {
        return runOuts;
    }

    public void setRunOuts(Long runOuts) {
        this.runOuts = runOuts;
    }

    public Long getStumpings() {
        return stumpings;
    }

    public void setStumpings(Long stumpings) {
        this.stumpings = stumpings;
    }
}