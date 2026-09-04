package com.cricklocal.dto;

import com.cricklocal.enums.MatchResultType;

public class MatchResultResponse {

    private Long id;

    private Long matchId;

    private MatchResultType resultType;

    private Long winningTeamId;
    private String winningTeamName;

    private Long losingTeamId;
    private String losingTeamName;

    private Integer marginRuns;

    private Integer marginWickets;

    private String resultText;

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

    public MatchResultType getResultType() {
        return resultType;
    }

    public void setResultType(MatchResultType resultType) {
        this.resultType = resultType;
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

    public Long getLosingTeamId() {
        return losingTeamId;
    }

    public void setLosingTeamId(Long losingTeamId) {
        this.losingTeamId = losingTeamId;
    }

    public String getLosingTeamName() {
        return losingTeamName;
    }

    public void setLosingTeamName(String losingTeamName) {
        this.losingTeamName = losingTeamName;
    }

    public Integer getMarginRuns() {
        return marginRuns;
    }

    public void setMarginRuns(Integer marginRuns) {
        this.marginRuns = marginRuns;
    }

    public Integer getMarginWickets() {
        return marginWickets;
    }

    public void setMarginWickets(Integer marginWickets) {
        this.marginWickets = marginWickets;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }
}