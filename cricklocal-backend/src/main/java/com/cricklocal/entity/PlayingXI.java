package com.cricklocal.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "playing_xis",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_playing_xi_match_team",
            columnNames = {"match_id", "team_id"}
        )
    }
)
public class PlayingXI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

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

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}