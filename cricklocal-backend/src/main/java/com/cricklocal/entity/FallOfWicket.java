package com.cricklocal.entity;

import com.cricklocal.enums.WicketType;
import jakarta.persistence.*;

@Entity
@Table(
    name = "fall_of_wickets",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_innings_wicket_number",
            columnNames = {"innings_id", "wicket_number"}
        )
    }
)
public class FallOfWicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innings_id", nullable = false)
    private Innings innings;

    @Column(nullable = false)
    private Integer wicketNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dismissed_player_id", nullable = false)
    private Player dismissedPlayer;

    @Column(nullable = false)
    private Integer score;

    @Column(nullable = false)
    private Integer overNumber;

    @Column(nullable = false)
    private Integer ballInOver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WicketType wicketType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    // getters/setters

    public Long getId() {
        return id;
    }

    public Innings getInnings() {
        return innings;
    }

    public void setInnings(Innings innings) {
        this.innings = innings;
    }

    public Integer getWicketNumber() {
        return wicketNumber;
    }

    public void setWicketNumber(Integer wicketNumber) {
        this.wicketNumber = wicketNumber;
    }

    public Player getDismissedPlayer() {
        return dismissedPlayer;
    }

    public void setDismissedPlayer(Player dismissedPlayer) {
        this.dismissedPlayer = dismissedPlayer;
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

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }
}