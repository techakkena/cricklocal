package com.cricklocal.dto;

import com.cricklocal.enums.MatchFormat;
import com.cricklocal.enums.MatchStatus;

import java.time.Instant;
import java.util.List;

public class MatchResponse {

    private Long id;
    private String name;
    private MatchFormat format;
    private Integer totalOvers;
    private Integer maxPlayersPerTeam;
    private Long seriesId;
    private String seriesName;
    private Integer matchNumber;
    private Instant scheduledAt;
    private String venue;
    private MatchStatus status;
    private Instant createdAt;
    private List<MatchTeamResponse> teams;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public void setFormat(MatchFormat format) {
        this.format = format;
    }

    public Integer getTotalOvers() {
        return totalOvers;
    }

    public void setTotalOvers(Integer totalOvers) {
        this.totalOvers = totalOvers;
    }

    public Integer getMaxPlayersPerTeam() {
        return maxPlayersPerTeam;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public String getSeriesName() {
        return seriesName;
    }

    public void setSeriesName(String seriesName) {
        this.seriesName = seriesName;
    }

    public Integer getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(Integer matchNumber) {
        this.matchNumber = matchNumber;
    }

    public void setMaxPlayersPerTeam(Integer maxPlayersPerTeam) {
        this.maxPlayersPerTeam = maxPlayersPerTeam;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    public List<MatchTeamResponse> getTeams() {
    return teams;
    }
    public void setTeams(List<MatchTeamResponse> teams) {
        this.teams = teams;
    }
}