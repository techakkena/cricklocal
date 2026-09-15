package com.cricklocal.dto;

import java.time.Instant;

public class PlayingXIResponse {

    private Long playingXIId;
    private Long matchId;
    private Long teamId;
    private String teamName;
    private String teamShortName;
    private Instant finalizedAt;
    private Integer playerCount;
    private Long captainPlayerId;
    private String captainName;

    public Long getPlayingXIId() {
        return playingXIId;
    }

    public void setPlayingXIId(Long playingXIId) {
        this.playingXIId = playingXIId;
    }

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
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

    public String getTeamShortName() {
        return teamShortName;
    }

    public void setTeamShortName(String teamShortName) {
        this.teamShortName = teamShortName;
    }

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public Integer getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(Integer playerCount) {
        this.playerCount = playerCount;
    }

    public Long getCaptainPlayerId() {
        return captainPlayerId;
    }

    public void setCaptainPlayerId(Long captainPlayerId) {
        this.captainPlayerId = captainPlayerId;
    }

    public String getCaptainName() {
        return captainName;
    }

    public void setCaptainName(String captainName) {
        this.captainName = captainName;
    }
}