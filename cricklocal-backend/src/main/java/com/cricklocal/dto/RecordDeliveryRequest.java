package com.cricklocal.dto;

import com.cricklocal.enums.ExtraType;
import com.cricklocal.enums.WicketType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import com.cricklocal.enums.DismissalEnd;

public class RecordDeliveryRequest {

    @NotNull(message = "Batter is required")
    private Long batterId;

    private Long nonStrikerId;

    @NotNull(message = "Bowler is required")
    private Long bowlerId;

    @NotNull(message = "Runs off bat is required")
    @Min(value = 0, message = "Runs off bat cannot be negative")
    private Integer runsOffBat;

    @NotNull(message = "Extra type is required")
    private ExtraType extraType;

    @NotNull(message = "Extra runs are required")
    @Min(value = 0, message = "Extra runs cannot be negative")
    private Integer extraRuns;

    @NotNull(message = "Wicket status is required")
    private Boolean wicket;

    private WicketType wicketType;

    private Long dismissedPlayerId;

    private Long newBatterId;
    
    private DismissalEnd dismissalEnd;

    private Long fielderId;


    public Long getBatterId() {
        return batterId;
    }

    public void setBatterId(Long batterId) {
        this.batterId = batterId;
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

    public Integer getRunsOffBat() {
        return runsOffBat;
    }

    public void setRunsOffBat(Integer runsOffBat) {
        this.runsOffBat = runsOffBat;
    }

    public ExtraType getExtraType() {
        return extraType;
    }

    public void setExtraType(ExtraType extraType) {
        this.extraType = extraType;
    }

    public Integer getExtraRuns() {
        return extraRuns;
    }

    public void setExtraRuns(Integer extraRuns) {
        this.extraRuns = extraRuns;
    }

    public Boolean getWicket() {
        return wicket;
    }

    public void setWicket(Boolean wicket) {
        this.wicket = wicket;
    }

    public WicketType getWicketType() {
        return wicketType;
    }

    public void setWicketType(WicketType wicketType) {
        this.wicketType = wicketType;
    }

    public Long getDismissedPlayerId() {
        return dismissedPlayerId;
    }

    public void setDismissedPlayerId(Long dismissedPlayerId) {
        this.dismissedPlayerId = dismissedPlayerId;
    }

    public Long getNewBatterId() {
        return newBatterId;
    }

    public void setNewBatterId(Long newBatterId) {
        this.newBatterId = newBatterId;
    }

    public DismissalEnd getDismissalEnd() {
        return dismissalEnd;
    }

    public void setDismissalEnd(DismissalEnd dismissalEnd) {
        this.dismissalEnd = dismissalEnd;
    }

    public Long getFielderId() {
        return fielderId;
    }

    public void setFielderId(Long fielderId) {
        this.fielderId = fielderId;
    }
}