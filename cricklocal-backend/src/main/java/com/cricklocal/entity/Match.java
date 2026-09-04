package com.cricklocal.entity;

import com.cricklocal.enums.MatchFormat;
import com.cricklocal.enums.MatchStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id")
    private Series series;

    @Column
    private Integer matchNumber;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchFormat format;

    @Column(nullable = false)
    private Integer totalOvers;

    @Column(nullable = false)
    private Integer maxPlayersPerTeam;

    @Column(nullable = false)
    private Instant scheduledAt;

    @Column(length = 150)
    private String venue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Integer getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(Integer matchNumber) {
        this.matchNumber = matchNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MatchFormat getFormat() {
        return format;
    }

    public void setFormat(MatchFormat format) {
        this.format = format;
    }

    public Integer getTotalOvers() {
        return totalOvers;
    }

    public void setTotalOvers(Integer totalOvers) {
        this.totalOvers = totalOvers;
    }

    public Integer getMaxPlayersPerTeam() {
        return maxPlayersPerTeam;
    }

    public void setMaxPlayersPerTeam(Integer maxPlayersPerTeam) {
        this.maxPlayersPerTeam = maxPlayersPerTeam;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}