package com.cricklocal.dto;

import java.math.BigDecimal;

public class CareerBowlingStatsResponse {

    private Long playerId;
    private String playerName;

    private Long matches;
    private Long innings;
    private Long ballsBowled;
    private Long runsConceded;
    private Long wickets;
    private Long maidens;
    private Long foursConceded;
    private Long sixesConceded;
    private Long wides;
    private Long noBalls;

    private BigDecimal economy;
    private BigDecimal average;

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

    public Long getBallsBowled() {
        return ballsBowled;
    }

    public void setBallsBowled(Long ballsBowled) {
        this.ballsBowled = ballsBowled;
    }

    public Long getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(Long runsConceded) {
        this.runsConceded = runsConceded;
    }

    public Long getWickets() {
        return wickets;
    }

    public void setWickets(Long wickets) {
        this.wickets = wickets;
    }

    public Long getMaidens() {
        return maidens;
    }

    public void setMaidens(Long maidens) {
        this.maidens = maidens;
    }

    public Long getFoursConceded() {
        return foursConceded;
    }

    public void setFoursConceded(Long foursConceded) {
        this.foursConceded = foursConceded;
    }

    public Long getSixesConceded() {
        return sixesConceded;
    }

    public void setSixesConceded(Long sixesConceded) {
        this.sixesConceded = sixesConceded;
    }

    public Long getWides() {
        return wides;
    }

    public void setWides(Long wides) {
        this.wides = wides;
    }

    public Long getNoBalls() {
        return noBalls;
    }

    public void setNoBalls(Long noBalls) {
        this.noBalls = noBalls;
    }

    public BigDecimal getEconomy() {
        return economy;
    }

    public void setEconomy(BigDecimal economy) {
        this.economy = economy;
    }

    public BigDecimal getAverage() {
        return average;
    }

    public void setAverage(BigDecimal average) {
        this.average = average;
    }
}