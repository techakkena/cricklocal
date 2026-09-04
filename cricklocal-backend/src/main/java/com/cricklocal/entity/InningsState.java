package com.cricklocal.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "innings_states",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_innings_state_innings",
            columnNames = {"innings_id"}
        )
    }
)
public class InningsState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innings_id", nullable = false)
    private Innings innings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "striker_id")
    private Player striker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "non_striker_id")
    private Player nonStriker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_bowler_id")
    private Player currentBowler;

    @Column(nullable = false)
    private Integer currentOver = 1;

    @Column(nullable = false)
    private Integer legalBallsInOver = 0;

    public Long getId() {
        return id;
    }

    public Innings getInnings() {
        return innings;
    }

    public void setInnings(Innings innings) {
        this.innings = innings;
    }

    public Player getStriker() {
        return striker;
    }

    public void setStriker(Player striker) {
        this.striker = striker;
    }

    public Player getNonStriker() {
        return nonStriker;
    }

    public void setNonStriker(Player nonStriker) {
        this.nonStriker = nonStriker;
    }

    public Player getCurrentBowler() {
        return currentBowler;
    }

    public void setCurrentBowler(Player currentBowler) {
        this.currentBowler = currentBowler;
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