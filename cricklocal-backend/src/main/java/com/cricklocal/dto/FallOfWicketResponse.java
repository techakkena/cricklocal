package com.cricklocal.dto;

import com.cricklocal.enums.WicketType;

public class FallOfWicketResponse {

    private Long id;
    private Long inningsId;

    private Integer wicketNumber;

    private Long dismissedPlayerId;
    private String dismissedPlayerName;

    private Integer score;

    private Integer overNumber;
    private Integer ballInOver;

    private WicketType wicketType;

    private Long deliveryId;

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

    public Integer getWicketNumber() {
        return wicketNumber;
    }

    public void setWicketNumber(Integer wicketNumber) {
        this.wicketNumber = wicketNumber;
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

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
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

    public WicketType getWicketType() {
        return wicketType;
    }

    public void setWicketType(WicketType wicketType) {
        this.wicketType = wicketType;
    }

    public Long getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(Long deliveryId) {
        this.deliveryId = deliveryId;
    }
}