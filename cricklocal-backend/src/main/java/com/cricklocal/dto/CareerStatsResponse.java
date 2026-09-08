package com.cricklocal.dto;

public class CareerStatsResponse {

    private Long playerId;
    private String playerName;

    private CareerBattingStatsResponse batting;
    private CareerBowlingStatsResponse bowling;
    private CareerFieldingStatsResponse fielding;

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

    public CareerBattingStatsResponse getBatting() {
        return batting;
    }

    public void setBatting(CareerBattingStatsResponse batting) {
        this.batting = batting;
    }

    public CareerBowlingStatsResponse getBowling() {
        return bowling;
    }

    public void setBowling(CareerBowlingStatsResponse bowling) {
        this.bowling = bowling;
    }

    public CareerFieldingStatsResponse getFielding() {
        return fielding;
    }

    public void setFielding(CareerFieldingStatsResponse fielding) {
        this.fielding = fielding;
    }
}