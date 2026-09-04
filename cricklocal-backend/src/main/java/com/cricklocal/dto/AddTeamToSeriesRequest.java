package com.cricklocal.dto;

import jakarta.validation.constraints.NotNull;

public class AddTeamToSeriesRequest {

    @NotNull(message = "Team ID is required")
    private Long teamId;

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }
}