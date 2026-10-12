
package com.cricklocal.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "short_run_adjustments")
public class ShortRunAdjustment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "innings_id", nullable = false)
    private Innings innings;

    // The delivery against which this adjustment was applied.
    // Nullable for adjustments made before any delivery.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;

    @Column(name = "over_number")
    private Integer overNumber;

    @Column(nullable = false)
    private Integer runsDeducted;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private boolean reversed = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }

    public Innings getInnings() { return innings; }
    public void setInnings(Innings innings) {
        this.innings = innings;
    }

    public Delivery getDelivery() { return delivery; }
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }

    public Integer getRunsDeducted() { return runsDeducted; }
    public void setRunsDeducted(Integer runsDeducted) {
        this.runsDeducted = runsDeducted;
    }

    public String getReason() { return reason; }
    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isReversed() { return reversed; }
    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public Instant getCreatedAt() { return createdAt; }

    public Integer getOverNumber() {
        return overNumber;
    }

    public void setOverNumber(Integer overNumber) {
        this.overNumber = overNumber;
    }
}
