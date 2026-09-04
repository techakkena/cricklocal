package com.cricklocal.dto;

import com.cricklocal.enums.InningsStatus;

import java.time.Instant;

public class InningsResponse {

    private Long id;

    private Long matchId;

    private Integer inningsNumber;

    private Long battingTeamId;
    private String battingTeamName;
    private String battingTeamShortName;

    private Long bowlingTeamId;
    private String bowlingTeamName;
    private String bowlingTeamShortName;

    private Integer totalRuns;
    private Integer wickets;
    private Integer legalBalls;

    private InningsStatus status;

    private Instant createdAt;
    private Instant startedAt;
    private Instant completedAt;

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

    public Integer getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(Integer inningsNumber) {
        this.inningsNumber = inningsNumber;
    }

    public Long getBattingTeamId() {
        return battingTeamId;
    }

    public void setBattingTeamId(Long battingTeamId) {
        this.battingTeamId = battingTeamId;
    }

    public String getBattingTeamName() {
        return battingTeamName;
    }

    public void setBattingTeamName(String battingTeamName) {
        this.battingTeamName = battingTeamName;
    }

    public String getBattingTeamShortName() {
        return battingTeamShortName;
    }

    public void setBattingTeamShortName(String battingTeamShortName) {
        this.battingTeamShortName = battingTeamShortName;
    }

    public Long getBowlingTeamId() {
        return bowlingTeamId;
    }

    public void setBowlingTeamId(Long bowlingTeamId) {
        this.bowlingTeamId = bowlingTeamId;
    }

    public String getBowlingTeamName() {
        return bowlingTeamName;
    }

    public void setBowlingTeamName(String bowlingTeamName) {
        this.bowlingTeamName = bowlingTeamName;
    }

    public String getBowlingTeamShortName() {
        return bowlingTeamShortName;
    }

    public void setBowlingTeamShortName(String bowlingTeamShortName) {
        this.bowlingTeamShortName = bowlingTeamShortName;
    }

    public Integer getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(Integer totalRuns) {
        this.totalRuns = totalRuns;
    }

    public Integer getWickets() {
        return wickets;
    }

    public void setWickets(Integer wickets) {
        this.wickets = wickets;
    }

    public Integer getLegalBalls() {
        return legalBalls;
    }

    public void setLegalBalls(Integer legalBalls) {
        this.legalBalls = legalBalls;
    }

    public InningsStatus getStatus() {
        return status;
    }

    public void setStatus(InningsStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}