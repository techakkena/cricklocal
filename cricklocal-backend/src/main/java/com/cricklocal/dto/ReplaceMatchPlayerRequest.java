package com.cricklocal.dto;

import jakarta.validation.constraints.NotNull;

public class ReplaceMatchPlayerRequest {

    @NotNull(message = "Outgoing player is required")
    private Long outPlayerId;

    @NotNull(message = "Incoming player is required")
    private Long inPlayerId;

    public Long getOutPlayerId() {
        return outPlayerId;
    }

    public void setOutPlayerId(Long outPlayerId) {
        this.outPlayerId = outPlayerId;
    }

    public Long getInPlayerId() {
        return inPlayerId;
    }

    public void setInPlayerId(Long inPlayerId) {
        this.inPlayerId = inPlayerId;
    }
}
