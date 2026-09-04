package com.cricklocal.dto;

import com.cricklocal.enums.MatchResultType;
import jakarta.validation.constraints.NotNull;

public class RecordMatchResultRequest {

    @NotNull(message = "Result type is required")
    private MatchResultType resultType;

    private Long winningTeamId;

    private Long losingTeamId;

    private Integer marginRuns;

    private Integer marginWickets;

    private String resultText;

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

    public Long getLosingTeamId() {
        return losingTeamId;
    }

    public void setLosingTeamId(Long losingTeamId) {
        this.losingTeamId = losingTeamId;
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