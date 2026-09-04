package com.cricklocal.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "partnerships",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_innings_partnership_number",
            columnNames = {"innings_id", "partnership_number"}
        )
    }
)
public class Partnership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innings_id", nullable = false)
    private Innings innings;

    @Column(nullable = false)
    private Integer partnershipNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batter_one_id", nullable = false)
    private Player batterOne;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batter_two_id", nullable = false)
    private Player batterTwo;

    @Column(nullable = false)
    private Integer runs = 0;

    @Column(nullable = false)
    private Integer balls = 0;

    @Column(nullable = false)
    private Boolean active = true;

    public Long getId() {
        return id;
    }

    public Innings getInnings() {
        return innings;
    }

    public void setInnings(Innings innings) {
        this.innings = innings;
    }

    public Integer getPartnershipNumber() {
        return partnershipNumber;
    }

    public void setPartnershipNumber(Integer partnershipNumber) {
        this.partnershipNumber = partnershipNumber;
    }

    public Player getBatterOne() {
        return batterOne;
    }

    public void setBatterOne(Player batterOne) {
        this.batterOne = batterOne;
    }

    public Player getBatterTwo() {
        return batterTwo;
    }

    public void setBatterTwo(Player batterTwo) {
        this.batterTwo = batterTwo;
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