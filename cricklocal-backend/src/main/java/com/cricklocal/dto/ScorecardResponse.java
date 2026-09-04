package com.cricklocal.dto;

import java.util.List;

public class ScorecardResponse {

    private Long matchId;

    private String matchName;

    private String status;

    private List<InningsScorecardResponse> innings;

    private MatchResultResponse result;

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<InningsScorecardResponse> getInnings() {
        return innings;
    }

    public void setInnings(List<InningsScorecardResponse> innings) {
        this.innings = innings;
    }

    public MatchResultResponse getResult() {
        return result;
    }

    public void setResult(MatchResultResponse result) {
        this.result = result;
    }
}