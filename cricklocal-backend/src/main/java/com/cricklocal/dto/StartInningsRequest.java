package com.cricklocal.dto;

import jakarta.validation.constraints.NotNull;

public class StartInningsRequest {

    @NotNull(message = "Batting team is required")
    private Long battingTeamId;

    @NotNull(message = "Bowling team is required")
    private Long bowlingTeamId;

    @NotNull(message = "Innings number is required")
    private Integer inningsNumber;

    public Long getBattingTeamId() {
        return battingTeamId;
    }

    public void setBattingTeamId(Long battingTeamId) {
        this.battingTeamId = battingTeamId;
    }

    public Long getBowlingTeamId() {
        return bowlingTeamId;
    }

    public void setBowlingTeamId(Long bowlingTeamId) {
        this.bowlingTeamId = bowlingTeamId;
    }

    public Integer getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(Integer inningsNumber) {
        this.inningsNumber = inningsNumber;
    }
}