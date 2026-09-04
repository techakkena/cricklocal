package com.cricklocal.dto;

import java.util.List;

public class InningsScorecardResponse {

    private Long inningsId;

    private Integer inningsNumber;

    private Long battingTeamId;

    private String battingTeamName;

    private Long bowlingTeamId;

    private String bowlingTeamName;

    private Integer totalRuns;

    private Integer wickets;

    private Integer legalBalls;

    private String status;

    private List<BattingInningsResponse> batting;

    private List<BowlingInningsResponse> bowling;

    private List<PartnershipResponse> partnerships;

    private List<FallOfWicketResponse> fallOfWickets;

    private List<FieldingEventResponse> fieldingEvents;

    public Long getInningsId() {
        return inningsId;
    }

    public void setInningsId(Long inningsId) {
        this.inningsId = inningsId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BattingInningsResponse> getBatting() {
        return batting;
    }

    public void setBatting(List<BattingInningsResponse> batting) {
        this.batting = batting;
    }

    public List<BowlingInningsResponse> getBowling() {
        return bowling;
    }

    public void setBowling(List<BowlingInningsResponse> bowling) {
        this.bowling = bowling;
    }

    public List<PartnershipResponse> getPartnerships() {
        return partnerships;
    }

    public void setPartnerships(List<PartnershipResponse> partnerships) {
        this.partnerships = partnerships;
    }

    public List<FallOfWicketResponse> getFallOfWickets() {
        return fallOfWickets;
    }

    public void setFallOfWickets(
            List<FallOfWicketResponse> fallOfWickets) {
        this.fallOfWickets = fallOfWickets;
    }

    public List<FieldingEventResponse> getFieldingEvents() {
        return fieldingEvents;
    }

    public void setFieldingEvents(
            List<FieldingEventResponse> fieldingEvents) {
        this.fieldingEvents = fieldingEvents;
    }
}