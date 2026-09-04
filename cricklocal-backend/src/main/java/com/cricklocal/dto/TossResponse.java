package com.cricklocal.dto;

import com.cricklocal.enums.TossDecision;

import java.time.Instant;

public class TossResponse {

    private Long id;
    private Long matchId;
    private Long winningTeamId;
    private String winningTeamName;
    private String winningTeamShortName;
    private TossDecision decision;
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getWinningTeamId() {
        return winningTeamId;
    }

    public void setWinningTeamId(Long winningTeamId) {
        this.winningTeamId = winningTeamId;
    }

    public String getWinningTeamName() {
        return winningTeamName;
    }

    public void setWinningTeamName(String winningTeamName) {
        this.winningTeamName = winningTeamName;
    }

    public String getWinningTeamShortName() {
        return winningTeamShortName;
    }

    public void setWinningTeamShortName(String winningTeamShortName) {
        this.winningTeamShortName = winningTeamShortName;
    }

    public TossDecision getDecision() {
        return decision;
    }

    public void setDecision(TossDecision decision) {
        this.decision = decision;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}