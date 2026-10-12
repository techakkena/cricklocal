
package com.cricklocal.dto;

import java.time.Instant;

public class ShortRunAdjustmentResponse {

    private Long id;
    private Long inningsId;
    private Integer runsDeducted;
    private String reason;
    private Instant createdAt;
    private Integer updatedTotalRuns;
    private boolean reversed;
    private Integer overNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInningsId() {
        return inningsId;
    }

    public void setInningsId(Long inningsId) {
        this.inningsId = inningsId;
    }

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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUpdatedTotalRuns() {
        return updatedTotalRuns;
    }

    public void setUpdatedTotalRuns(Integer updatedTotalRuns) {
        this.updatedTotalRuns = updatedTotalRuns;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public Integer getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(Integer overNumber) {
        this.overNumber = overNumber;
    }
}