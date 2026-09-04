package com.cricklocal.dto;

public class BattingInningsResponse {

    private Long id;
    private Long inningsId;

    private Long playerId;
    private String playerName;

    private Integer battingPosition;
    private Integer runs;
    private Integer ballsFaced;
    private Integer fours;
    private Integer sixes;
    private Integer dots;
    private Integer extrasFaced;

    private Boolean dismissed;
    private String dismissalType;

    private Long dismissedByPlayerId;
    private String dismissedByPlayerName;

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

    public Integer getBattingPosition() {
        return battingPosition;
    }

    public void setBattingPosition(Integer battingPosition) {
        this.battingPosition = battingPosition;
    }

    public Integer getRuns() {
        return runs;
    }

    public void setRuns(Integer runs) {
        this.runs = runs;
    }

    public Integer getBallsFaced() {
        return ballsFaced;
    }

    public void setBallsFaced(Integer ballsFaced) {
        this.ballsFaced = ballsFaced;
    }

    public Integer getFours() {
        return fours;
    }

    public void setFours(Integer fours) {
        this.fours = fours;
    }

    public Integer getSixes() {
        return sixes;
    }

    public void setSixes(Integer sixes) {
        this.sixes = sixes;
    }

    public Integer getDots() {
        return dots;
    }

    public void setDots(Integer dots) {
        this.dots = dots;
    }

    public Integer getExtrasFaced() {
        return extrasFaced;
    }

    public void setExtrasFaced(Integer extrasFaced) {
        this.extrasFaced = extrasFaced;
    }

    public Boolean getDismissed() {
        return dismissed;
    }

    public void setDismissed(Boolean dismissed) {
        this.dismissed = dismissed;
    }

    public String getDismissalType() {
        return dismissalType;
    }

    public void setDismissalType(String dismissalType) {
        this.dismissalType = dismissalType;
    }

    public Long getDismissedByPlayerId() {
        return dismissedByPlayerId;
    }

    public void setDismissedByPlayerId(Long dismissedByPlayerId) {
        this.dismissedByPlayerId = dismissedByPlayerId;
    }

    public String getDismissedByPlayerName() {
        return dismissedByPlayerName;
    }

    public void setDismissedByPlayerName(String dismissedByPlayerName) {
        this.dismissedByPlayerName = dismissedByPlayerName;
    }
}