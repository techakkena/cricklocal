package com.cricklocal.dto;

import java.time.Instant;

public class InningsStateResponse {

    private Long id;

    private Long inningsId;
    private Integer inningsNumber;

    private Long strikerId;
    private String strikerName;

    private Long nonStrikerId;
    private String nonStrikerName;

    private Long currentBowlerId;
    private String currentBowlerName;

    private Integer currentOver;
    private Integer legalBallsInOver;

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

    public Long getStrikerId() {
        return strikerId;
    }

    public void setStrikerId(Long strikerId) {
        this.strikerId = strikerId;
    }

    public String getStrikerName() {
        return strikerName;
    }

    public void setStrikerName(String strikerName) {
        this.strikerName = strikerName;
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

    public Long getCurrentBowlerId() {
        return currentBowlerId;
    }

    public void setCurrentBowlerId(Long currentBowlerId) {
        this.currentBowlerId = currentBowlerId;
    }

    public String getCurrentBowlerName() {
        return currentBowlerName;
    }

    public void setCurrentBowlerName(String currentBowlerName) {
        this.currentBowlerName = currentBowlerName;
    }

    public Integer getCurrentOver() {
        return currentOver;
    }

    public void setCurrentOver(Integer currentOver) {
        this.currentOver = currentOver;
    }

    public Integer getLegalBallsInOver() {
        return legalBallsInOver;
    }

    public void setLegalBallsInOver(Integer legalBallsInOver) {
        this.legalBallsInOver = legalBallsInOver;
    }
}