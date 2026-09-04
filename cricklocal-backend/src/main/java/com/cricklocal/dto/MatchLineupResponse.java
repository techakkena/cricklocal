package com.cricklocal.dto;

public class MatchLineupResponse {

    private Long id;

    private Long matchId;

    private Long teamId;
    private String teamName;
    private String teamShortName;

    private Long playerId;
    private String playerName;

    private Integer jerseyNumber;

    private Boolean playing;
    private Boolean captain;
    private Boolean wicketKeeper;

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

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public Boolean getPlaying() {
        return playing;
    }

    public void setPlaying(Boolean playing) {
        this.playing = playing;
    }

    public Boolean getCaptain() {
        return captain;
    }

    public void setCaptain(Boolean captain) {
        this.captain = captain;
    }

    public Boolean getWicketKeeper() {
        return wicketKeeper;
    }

    public void setWicketKeeper(Boolean wicketKeeper) {
        this.wicketKeeper = wicketKeeper;
    }
}