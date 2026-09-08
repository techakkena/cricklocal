package com.cricklocal.dto;

import com.cricklocal.enums.MatchStatus;

import java.time.Instant;

public class PlayerMatchHistoryResponse {

    private Long matchId;
    private String matchName;
    private Instant matchDate;
    private MatchStatus matchStatus;

    private Long teamId;
    private String teamName;

    private Boolean playing;

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

    public Instant getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(Instant matchDate) {
        this.matchDate = matchDate;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(MatchStatus matchStatus) {
        this.matchStatus = matchStatus;
    }

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

    public Boolean getPlaying() {
        return playing;
    }

    public void setPlaying(Boolean playing) {
        this.playing = playing;
    }
}