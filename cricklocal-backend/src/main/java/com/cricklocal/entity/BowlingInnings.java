package com.cricklocal.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "bowling_innings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_innings_bowler",
            columnNames = {"innings_id", "player_id"}
        )
    }
)
public class BowlingInnings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innings_id", nullable = false)
    private Innings innings;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private Integer overs = 0;

    @Column(nullable = false)
    private Integer ballsBowled = 0;

    @Column(nullable = false)
    private Integer runsConceded = 0;

    @Column(nullable = false)
    private Integer wickets = 0;

    @Column(nullable = false)
    private Integer maidens = 0;

    @Column(nullable = false)
    private Integer foursConceded = 0;

    @Column(nullable = false)
    private Integer sixesConceded = 0;

    @Column(nullable = false)
    private Integer wides = 0;

    @Column(nullable = false)
    private Integer noBalls = 0;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public Innings getInnings() {
        return innings;
    }

    public void setInnings(Innings innings) {
        this.innings = innings;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getOvers() {
        return overs;
    }

    public void setOvers(Integer overs) {
        this.overs = overs;
    }

    public Integer getBallsBowled() {
        return ballsBowled;
    }

    public void setBallsBowled(Integer ballsBowled) {
        this.ballsBowled = ballsBowled;
    }

    public Integer getRunsConceded() {
        return runsConceded;
    }

    public void setRunsConceded(Integer runsConceded) {
        this.runsConceded = runsConceded;
    }

    public Integer getWickets() {
        return wickets;
    }

    public void setWickets(Integer wickets) {
        this.wickets = wickets;
    }

    public Integer getMaidens() {
        return maidens;
    }

    public void setMaidens(Integer maidens) {
        this.maidens = maidens;
    }

    public Integer getFoursConceded() {
        return foursConceded;
    }

    public void setFoursConceded(Integer foursConceded) {
        this.foursConceded = foursConceded;
    }

    public Integer getSixesConceded() {
        return sixesConceded;
    }

    public void setSixesConceded(Integer sixesConceded) {
        this.sixesConceded = sixesConceded;
    }

    public Integer getWides() {
        return wides;
    }

    public void setWides(Integer wides) {
        this.wides = wides;
    }

    public Integer getNoBalls() {
        return noBalls;
    }

    public void setNoBalls(Integer noBalls) {
        this.noBalls = noBalls;
    }
}