package com.cricklocal.dto;

import java.time.Instant;

public class TeamRosterPlayerResponse {

    private Long teamPlayerId;
    private Long playerId;
    private String displayName;
    private Integer jerseyNumber;
    private Instant joinedAt;
    private Instant leftAt;
    private Boolean active;

    public Long getTeamPlayerId() {
        return teamPlayerId;
    }

    public void setTeamPlayerId(Long teamPlayerId) {
        this.teamPlayerId = teamPlayerId;
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getLeftAt() {
        return leftAt;
    }

    public void setLeftAt(Instant leftAt) {
        this.leftAt = leftAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}