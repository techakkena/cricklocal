package com.cricklocal.dto;

import com.cricklocal.enums.DeliveryResult;
import com.cricklocal.enums.ExtraType;
import com.cricklocal.enums.WicketType;
import com.cricklocal.enums.DismissalEnd;

import java.time.Instant;

public class DeliveryResponse {

    private Long id;

    private Long inningsId;
    private Integer inningsNumber;

    private Integer deliveryNumber;
    private Integer overNumber;
    private Integer ballInOver;

    private Long batterId;
    private String batterName;

    private Long nonStrikerId;
    private String nonStrikerName;

    private Long bowlerId;
    private String bowlerName;

    private Boolean legalDelivery;

    private Integer runsOffBat;
    private Integer extraRuns;
    private Integer totalRuns;

    private ExtraType extraType;
    private DeliveryResult result;

    private Boolean wicket;
    private WicketType wicketType;
    private DismissalEnd dismissalEnd;
    private Long dismissedPlayerId;
    private String dismissedPlayerName;

    private Long fielderId;
    private String fielderName;

    private Integer inningsTotalRuns;
    private Integer inningsWickets;
    private Integer inningsLegalBalls;

    private Instant createdAt;

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

    public Integer getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(Integer inningsNumber) {
        this.inningsNumber = inningsNumber;
    }

    public Integer getDeliveryNumber() {
        return deliveryNumber;
    }

    public void setDeliveryNumber(Integer deliveryNumber) {
        this.deliveryNumber = deliveryNumber;
    }

    public Integer getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(Integer overNumber) {
        this.overNumber = overNumber;
    }

    public Integer getBallInOver() {
        return ballInOver;
    }

    public void setBallInOver(Integer ballInOver) {
        this.ballInOver = ballInOver;
    }

    public Long getBatterId() {
        return batterId;
    }

    public void setBatterId(Long batterId) {
        this.batterId = batterId;
    }

    public String getBatterName() {
        return batterName;
    }

    public void setBatterName(String batterName) {
        this.batterName = batterName;
    }

    public Long getNonStrikerId() {
        return nonStrikerId;
    }

    public void setNonStrikerId(Long nonStrikerId) {
        this.nonStrikerId = nonStrikerId;
    }

    public String getNonStrikerName() {
        return nonStrikerName;
    }

    public void setNonStrikerName(String nonStrikerName) {
        this.nonStrikerName = nonStrikerName;
    }

    public Long getBowlerId() {
        return bowlerId;
    }

    public void setBowlerId(Long bowlerId) {
        this.bowlerId = bowlerId;
    }

    public String getBowlerName() {
        return bowlerName;
    }

    public void setBowlerName(String bowlerName) {
        this.bowlerName = bowlerName;
    }

    public Boolean getLegalDelivery() {
        return legalDelivery;
    }

    public void setLegalDelivery(Boolean legalDelivery) {
        this.legalDelivery = legalDelivery;
    }

    public Integer getRunsOffBat() {
        return runsOffBat;
    }

    public void setRunsOffBat(Integer runsOffBat) {
        this.runsOffBat = runsOffBat;
    }

    public Integer getExtraRuns() {
        return extraRuns;
    }

    public void setExtraRuns(Integer extraRuns) {
        this.extraRuns = extraRuns;
    }

    public Integer getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(Integer totalRuns) {
        this.totalRuns = totalRuns;
    }

    public ExtraType getExtraType() {
        return extraType;
    }

    public void setExtraType(ExtraType extraType) {
        this.extraType = extraType;
    }

    public DeliveryResult getResult() {
        return result;
    }

    public void setResult(DeliveryResult result) {
        this.result = result;
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

    public DismissalEnd getDismissalEnd() {
        return dismissalEnd;
    }

    public void setDismissalEnd(DismissalEnd dismissalEnd) {
        this.dismissalEnd = dismissalEnd;
    }

    public Long getDismissedPlayerId() {
        return dismissedPlayerId;
    }

    public void setDismissedPlayerId(Long dismissedPlayerId) {
        this.dismissedPlayerId = dismissedPlayerId;
    }

    public String getDismissedPlayerName() {
        return dismissedPlayerName;
    }

    public void setDismissedPlayerName(String dismissedPlayerName) {
        this.dismissedPlayerName = dismissedPlayerName;
    }

    public Long getFielderId() {
        return fielderId;
    }

    public void setFielderId(Long fielderId) {
        this.fielderId = fielderId;
    }

    public String getFielderName() {
        return fielderName;
    }

    public void setFielderName(String fielderName) {
        this.fielderName = fielderName;
    }

    public Integer getInningsTotalRuns() {
        return inningsTotalRuns;
    }

    public void setInningsTotalRuns(Integer inningsTotalRuns) {
        this.inningsTotalRuns = inningsTotalRuns;
    }

    public Integer getInningsWickets() {
        return inningsWickets;
    }

    public void setInningsWickets(Integer inningsWickets) {
        this.inningsWickets = inningsWickets;
    }

    public Integer getInningsLegalBalls() {
        return inningsLegalBalls;
    }

    public void setInningsLegalBalls(Integer inningsLegalBalls) {
        this.inningsLegalBalls = inningsLegalBalls;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}