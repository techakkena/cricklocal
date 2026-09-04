package com.cricklocal.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "batting_innings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_innings_batter",
            columnNames = {"innings_id", "player_id"}
        )
    }
)
public class BattingInnings {

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
    private Integer battingPosition;

    @Column(nullable = false)
    private Integer runs = 0;

    @Column(nullable = false)
    private Integer ballsFaced = 0;

    @Column(nullable = false)
    private Integer fours = 0;

    @Column(nullable = false)
    private Integer sixes = 0;

    @Column(nullable = false)
    private Integer dots = 0;

    @Column(nullable = false)
    private Integer extrasFaced = 0;

    @Column(nullable = false)
    private Boolean dismissed = false;

    @Column(length = 30)
    private String dismissalType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dismissed_by_player_id")
    private Player dismissedByPlayer;

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

    public Integer getBattingPosition() {
        return battingPosition;
    }

    public void setBattingPosition(Integer battingPosition) {
        this.battingPosition = battingPosition;
    }

    public Integer getRuns() {
        return runs;
    }

    public void setRuns(Integer runs) {
        this.runs = runs;
    }

    public Integer getBallsFaced() {
        return ballsFaced;
    }

    public void setBallsFaced(Integer ballsFaced) {
        this.ballsFaced = ballsFaced;
    }

    public Integer getFours() {
        return fours;
    }

    public void setFours(Integer fours) {
        this.fours = fours;
    }

    public Integer getSixes() {
        return sixes;
    }

    public void setSixes(Integer sixes) {
        this.sixes = sixes;
    }

    public Integer getDots() {
        return dots;
    }

    public void setDots(Integer dots) {
        this.dots = dots;
    }

    public Integer getExtrasFaced() {
        return extrasFaced;
    }

    public void setExtrasFaced(Integer extrasFaced) {
        this.extrasFaced = extrasFaced;
    }

    public Boolean getDismissed() {
        return dismissed;
    }

    public void setDismissed(Boolean dismissed) {
        this.dismissed = dismissed;
    }

    public String getDismissalType() {
        return dismissalType;
    }

    public void setDismissalType(String dismissalType) {
        this.dismissalType = dismissalType;
    }

    public Player getDismissedByPlayer() {
        return dismissedByPlayer;
    }

    public void setDismissedByPlayer(Player dismissedByPlayer) {
        this.dismissedByPlayer = dismissedByPlayer;
    }
}