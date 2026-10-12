package com.cricklocal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ShortRunAdjustmentRequest {

    @NotNull(message = "Runs deducted is required")
    @Min(value = 1, message = "At least 1 run must be deducted")
    @Max(value = 5, message = "A maximum of 5 runs can be deducted at once")
    private Integer runsDeducted;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    public Integer getRunsDeducted() {
        return runsDeducted;
    }

    public void setRunsDeducted(Integer runsDeducted) {
        this.runsDeducted = runsDeducted;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}