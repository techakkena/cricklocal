package com.cricklocal.dto;

import com.cricklocal.enums.TossDecision;
import jakarta.validation.constraints.NotNull;

public class RecordTossRequest {

    @NotNull(message = "Winning team is required")
    private Long winningTeamId;

    @NotNull(message = "Toss decision is required")
    private TossDecision decision;

    public Long getWinningTeamId() {
        return winningTeamId;
    }

    public void setWinningTeamId(Long winningTeamId) {
        this.winningTeamId = winningTeamId;
    }

    public TossDecision getDecision() {
        return decision;
    }

    public void setDecision(TossDecision decision) {
        this.decision = decision;
    }
}