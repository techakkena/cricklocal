package com.cricklocal.entity;

import com.cricklocal.enums.TossDecision;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tosses")
public class Toss {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "winning_team_id", nullable = false)
    private Team winningTeam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TossDecision decision;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Team getWinningTeam() {
        return winningTeam;
    }

    public void setWinningTeam(Team winningTeam) {
        this.winningTeam = winningTeam;
    }

    public TossDecision getDecision() {
        return decision;
    }

    public void setDecision(TossDecision decision) {
        this.decision = decision;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}