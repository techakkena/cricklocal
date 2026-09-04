package com.cricklocal.dto;

import com.cricklocal.enums.MatchTeamSide;

public class MatchTeamResponse {

    private Long teamId;
    private String teamName;
    private String shortName;
    private MatchTeamSide side;

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public MatchTeamSide getSide() {
        return side;
    }

    public void setSide(MatchTeamSide side) {
        this.side = side;
    }
}