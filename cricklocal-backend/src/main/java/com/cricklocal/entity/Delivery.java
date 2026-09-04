package com.cricklocal.entity;

import com.cricklocal.enums.DeliveryResult;
import com.cricklocal.enums.ExtraType;
import com.cricklocal.enums.WicketType;
import jakarta.persistence.*;
import com.cricklocal.enums.DismissalEnd;

import java.time.Instant;

@Entity
@Table(
    name = "deliveries",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_innings_delivery_number",
            columnNames = {"innings_id", "delivery_number"}
        )
    }
)
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innings_id", nullable = false)
    private Innings innings;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batter_id", nullable = false)
    private Player batter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bowler_id", nullable = false)
    private Player bowler;

    @Column(nullable = false)
    private Integer deliveryNumber;

    @Column(nullable = false)
    private Integer overNumber;

    @Column(nullable = false)
    private Integer ballInOver;

    @Column(nullable = false)
    private Boolean legalDelivery;

    @Column(nullable = false)
    private Integer runsOffBat = 0;

    @Column(nullable = false)
    private Integer extraRuns = 0;

    @Column(nullable = false)
    private Integer totalRuns = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExtraType extraType = ExtraType.NONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliveryResult result;

    @Column(nullable = false)
    private Boolean wicket = false;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private WicketType wicketType;

    @Enumerated(EnumType.STRING)
    @Column(length=20)
    private DismissalEnd dismissalEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dismissed_player_id")
    private Player dismissedPlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fielder_id")
    private Player fielder;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Innings getInnings() {
        return innings;
    }

    public void setInnings(Innings innings) {
        this.innings = innings;
    }

    public Player getBatter() {
        return batter;
    }

    public void setBatter(Player batter) {
        this.batter = batter;
    }

    public Player getBowler() {
        return bowler;
    }

    public void setBowler(Player bowler) {
        this.bowler = bowler;
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

    public Player getDismissedPlayer() {
        return dismissedPlayer;
    }

    public void setDismissedPlayer(Player dismissedPlayer) {
        this.dismissedPlayer = dismissedPlayer;
    }

    public Player getFielder() {
        return fielder;
    }

    public void setFielder(Player fielder) {
        this.fielder = fielder;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}