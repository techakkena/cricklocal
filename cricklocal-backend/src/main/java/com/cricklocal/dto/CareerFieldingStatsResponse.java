package com.cricklocal.dto;

public class CareerFieldingStatsResponse {

    private Long playerId;
    private String playerName;

    private Long matches;
    private Long fieldingDismissals;
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

    public Long getMatches() {
        return matches;
    }

    public void setMatches(Long matches) {
        this.matches = matches;
    }

    public Long getFieldingDismissals() {
        return fieldingDismissals;
    }

    public void setFieldingDismissals(Long fieldingDismissals) {
        this.fieldingDismissals = fieldingDismissals;
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