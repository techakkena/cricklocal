package com.cricklocal.dto;

public class BowlingInningsResponse {

    private Long id;
    private Long inningsId;

    private Long playerId;
    private String playerName;

    private String overs;
    private Integer ballsBowled;
    private Integer runsConceded;
    private Integer wickets;
    private Integer maidens;
    private Integer foursConceded;
    private Integer sixesConceded;
    private Integer wides;
    private Integer noBalls;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInningsId() {
        return inningsId;
    }

    public void setInningsId(Long inningsId) {
        this.inningsId = inningsId;
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

    public String getOvers() {
        return overs;
    }

    public void setOvers(String overs) {
        this.overs = overs;
    }

    public Integer getBallsBowled() {
        return ballsBowled;
    }

    public void setBallsBowled(Integer ballsBowled) {
        this.ballsBowled = ballsBowled;
    }

    public Integer getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(Integer runsConceded) {
        this.runsConceded = runsConceded;
    }

    public Integer getWickets() {
        return wickets;
    }

    public void setWickets(Integer wickets) {
        this.wickets = wickets;
    }

    public Integer getMaidens() {
        return maidens;
    }

    public void setMaidens(Integer maidens) {
        this.maidens = maidens;
    }

    public Integer getFoursConceded() {
        return foursConceded;
    }

    public void setFoursConceded(Integer foursConceded) {
        this.foursConceded = foursConceded;
    }

    public Integer getSixesConceded() {
        return sixesConceded;
    }

    public void setSixesConceded(Integer sixesConceded) {
        this.sixesConceded = sixesConceded;
    }

    public Integer getWides() {
        return wides;
    }

    public void setWides(Integer wides) {
        this.wides = wides;
    }

    public Integer getNoBalls() {
        return noBalls;
    }

    public void setNoBalls(Integer noBalls) {
        this.noBalls = noBalls;
    }
}