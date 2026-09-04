package com.cricklocal.dto;

public class PartnershipResponse {

    private Long id;
    private Long inningsId;

    private Integer partnershipNumber;

    private Long batterOneId;
    private String batterOneName;

    private Long batterTwoId;
    private String batterTwoName;

    private Integer runs;
    private Integer balls;

    private Boolean active;

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

    public Integer getPartnershipNumber() {
        return partnershipNumber;
    }

    public void setPartnershipNumber(Integer partnershipNumber) {
        this.partnershipNumber = partnershipNumber;
    }

    public Long getBatterOneId() {
        return batterOneId;
    }

    public void setBatterOneId(Long batterOneId) {
        this.batterOneId = batterOneId;
    }

    public String getBatterOneName() {
        return batterOneName;
    }

    public void setBatterOneName(String batterOneName) {
        this.batterOneName = batterOneName;
    }

    public Long getBatterTwoId() {
        return batterTwoId;
    }

    public void setBatterTwoId(Long batterTwoId) {
        this.batterTwoId = batterTwoId;
    }

    public String getBatterTwoName() {
        return batterTwoName;
    }

    public void setBatterTwoName(String batterTwoName) {
        this.batterTwoName = batterTwoName;
    }

    public Integer getRuns() {
        return runs;
    }

    public void setRuns(Integer runs) {
        this.runs = runs;
    }

    public Integer getBalls() {
        return balls;
    }

    public void setBalls(Integer balls) {
        this.balls = balls;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}