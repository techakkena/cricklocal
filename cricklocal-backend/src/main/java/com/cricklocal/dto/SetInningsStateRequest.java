package com.cricklocal.dto;

import jakarta.validation.constraints.NotNull;

public class SetInningsStateRequest {

    @NotNull(message = "Striker ID is required")
    private Long strikerId;

    @NotNull(message = "Non-striker ID is required")
    private Long nonStrikerId;

    @NotNull(message = "Bowler ID is required")
    private Long bowlerId;

    public Long getStrikerId() {
        return strikerId;
    }

    public void setStrikerId(Long strikerId) {
        this.strikerId = strikerId;
    }

    public Long getNonStrikerId() {
        return nonStrikerId;
    }

    public void setNonStrikerId(Long nonStrikerId) {
        this.nonStrikerId = nonStrikerId;
    }

    public Long getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(Long bowlerId) {
        this.bowlerId = bowlerId;
    }
}