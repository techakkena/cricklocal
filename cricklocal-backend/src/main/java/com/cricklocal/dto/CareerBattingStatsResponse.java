package com.cricklocal.dto;

import java.math.BigDecimal;

public class CareerBattingStatsResponse {

    private Long playerId;
    private String playerName;

    private Long matches;
    private Long innings;
    private Long runs;
    private Long ballsFaced;
    private Long fours;
    private Long sixes;
    private Long dots;
    private Long extrasFaced;
    private Long dismissals;
    private Long highestScore;

    private BigDecimal average;
    private BigDecimal strikeRate;

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

    public Long getInnings() {
        return innings;
    }

    public void setInnings(Long innings) {
        this.innings = innings;
    }

    public Long getRuns() {
        return runs;
    }

    public void setRuns(Long runs) {
        this.runs = runs;
    }

    public Long getBallsFaced() {
        return ballsFaced;
    }

    public void setBallsFaced(Long ballsFaced) {
        this.ballsFaced = ballsFaced;
    }

    public Long getFours() {
        return fours;
    }

    public void setFours(Long fours) {
        this.fours = fours;
    }

    public Long getSixes() {
        return sixes;
    }

    public void setSixes(Long sixes) {
        this.sixes = sixes;
    }

    public Long getDots() {
        return dots;
    }

    public void setDots(Long dots) {
        this.dots = dots;
    }

    public Long getExtrasFaced() {
        return extrasFaced;
    }

    public void setExtrasFaced(Long extrasFaced) {
        this.extrasFaced = extrasFaced;
    }

    public Long getDismissals() {
        return dismissals;
    }

    public void setDismissals(Long dismissals) {
        this.dismissals = dismissals;
    }

    public Long getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(Long highestScore) {
        this.highestScore = highestScore;
    }

    public BigDecimal getAverage() {
        return average;
    }

    public void setAverage(BigDecimal average) {
        this.average = average;
    }

    public BigDecimal getStrikeRate() {
        return strikeRate;
    }

    public void setStrikeRate(BigDecimal strikeRate) {
        this.strikeRate = strikeRate;
    }
}