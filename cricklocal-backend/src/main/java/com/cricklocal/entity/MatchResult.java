package com.cricklocal.entity;

import com.cricklocal.enums.MatchResultType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
    name = "match_results",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_match_result_match",
            columnNames = {"match_id"}
        )
    }
)
public class MatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchResultType resultType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winning_team_id")
    private Team winningTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "losing_team_id")
    private Team losingTeam;

    @Column
    private Integer marginRuns;

    @Column
    private Integer marginWickets;

    @Column(length = 500)
    private String resultText;

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

    public MatchResultType getResultType() {
        return resultType;
    }

    public void setResultType(MatchResultType resultType) {
        this.resultType = resultType;
    }

    public Team getWinningTeam() {
        return winningTeam;
    }

    public void setWinningTeam(Team winningTeam) {
        this.winningTeam = winningTeam;
    }

    public Team getLosingTeam() {
        return losingTeam;
    }

    public void setLosingTeam(Team losingTeam) {
        this.losingTeam = losingTeam;
    }

    public Integer getMarginRuns() {
        return marginRuns;
    }

    public void setMarginRuns(Integer marginRuns) {
        this.marginRuns = marginRuns;
    }

    public Integer getMarginWickets() {
        return marginWickets;
    }

    public void setMarginWickets(Integer marginWickets) {
        this.marginWickets = marginWickets;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}