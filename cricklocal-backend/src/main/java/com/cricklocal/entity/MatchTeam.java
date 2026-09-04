package com.cricklocal.entity;

import com.cricklocal.enums.MatchTeamSide;
import jakarta.persistence.*;

@Entity
@Table(
    name = "match_teams",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_match_team",
            columnNames = {"match_id", "team_id"}
        ),
        @UniqueConstraint(
            name = "uk_match_team_side",
            columnNames = {"match_id", "side"}
        )
    }
)
public class MatchTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MatchTeamSide side;

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

    public MatchTeamSide getSide() {
        return side;
    }

    public void setSide(MatchTeamSide side) {
        this.side = side;
    }
}