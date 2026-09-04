package com.cricklocal.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "series_teams",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_series_team",
            columnNames = {"series_id", "team_id"}
        )
    }
)
public class SeriesTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    public Long getId() {
        return id;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}