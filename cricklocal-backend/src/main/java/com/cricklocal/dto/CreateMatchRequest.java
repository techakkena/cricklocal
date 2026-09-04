package com.cricklocal.dto;

import com.cricklocal.enums.MatchFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class CreateMatchRequest {

    @NotBlank(message = "Match name is required")
    @Size(max = 150, message = "Match name must not exceed 150 characters")
    private String name;

    @NotNull(message = "Match format is required")
    private MatchFormat format;

    @NotNull(message = "Total overs is required")
    @Min(value = 1, message = "Total overs must be at least 1")
    private Integer totalOvers;

    @NotNull(message = "Maximum players per team is required")
    @Min(value = 1, message = "Maximum players per team must be at least 1")
    private Integer maxPlayersPerTeam;

    @NotNull(message = "Scheduled time is required")
    private Instant scheduledAt;

    @Size(max = 150, message = "Venue must not exceed 150 characters")
    private String venue;

    @NotNull(message = "Team A is required")
    private Long teamAId;

    private Long seriesId;

    private Integer matchNumber;

    @NotNull(message = "Team B is required")
    private Long teamBId;

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

    public Long getTeamAId() {
        return teamAId;
    }

    public void setTeamAId(Long teamAId) {
        this.teamAId = teamAId;
    }

    public Long getTeamBId() {
        return teamBId;
    }

    public void setTeamBId(Long teamBId) {
        this.teamBId = teamBId;
    }

    public Long getSeriesId() {
    return seriesId;
}

public void setSeriesId(Long seriesId) {
    this.seriesId = seriesId;
}

public Integer getMatchNumber() {
    return matchNumber;
}

public void setMatchNumber(Integer matchNumber) {
        this.matchNumber = matchNumber;
}
}