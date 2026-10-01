package com.cricklocal.dto;

import jakarta.validation.constraints.NotNull;

public class AddPlayerToSeriesRequest {

    @NotNull
    private Long playerId;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
}