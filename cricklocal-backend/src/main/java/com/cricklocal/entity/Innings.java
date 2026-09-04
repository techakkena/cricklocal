package com.cricklocal.entity;

import com.cricklocal.enums.InningsStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "innings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_match_innings_number",
            columnNames = {"match_id", "innings_number"}
        )
    }
)
public class Innings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batting_team_id", nullable = false)
    private Team battingTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bowling_team_id", nullable = false)
    private Team bowlingTeam;

    @Column(nullable = false)
    private Integer inningsNumber;

    @Column(nullable = false)
    private Integer totalRuns = 0;

    @Column(nullable = false)
    private Integer wickets = 0;

    @Column(nullable = false)
    private Integer legalBalls = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InningsStatus status = InningsStatus.NOT_STARTED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant startedAt;

    private Instant completedAt;

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

    public Team getBattingTeam() {
        return battingTeam;
    }

    public void setBattingTeam(Team battingTeam) {
        this.battingTeam = battingTeam;
    }

    public Team getBowlingTeam() {
        return bowlingTeam;
    }

    public void setBowlingTeam(Team bowlingTeam) {
        this.bowlingTeam = bowlingTeam;
    }

    public Integer getInningsNumber() {
        return inningsNumber;
    }

    public void setInningsNumber(Integer inningsNumber) {
        this.inningsNumber = inningsNumber;
    }

    public Integer getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(Integer totalRuns) {
        this.totalRuns = totalRuns;
    }

    public Integer getWickets() {
        return wickets;
    }

    public void setWickets(Integer wickets) {
        this.wickets = wickets;
    }

    public Integer getLegalBalls() {
        return legalBalls;
    }

    public void setLegalBalls(Integer legalBalls) {
        this.legalBalls = legalBalls;
    }

    public InningsStatus getStatus() {
        return status;
    }

    public void setStatus(InningsStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}