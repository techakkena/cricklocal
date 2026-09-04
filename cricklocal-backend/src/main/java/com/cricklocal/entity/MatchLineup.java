package com.cricklocal.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "match_lineups",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_match_lineup_player",
            columnNames = {"match_id", "player_id"}
        ),
        @UniqueConstraint(
            name = "uk_match_lineup_jersey",
            columnNames = {"match_id", "team_id", "jersey_number"}
        )
    }
)
public class MatchLineup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private Integer jerseyNumber;

    @Column(nullable = false)
    private Boolean playing = true;

    @Column(nullable = false)
    private Boolean captain = false;

    @Column(nullable = false)
    private Boolean wicketKeeper = false;

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

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Integer getJerseyNumber() {
        return jerseyNumber;
    }

    public void setJerseyNumber(Integer jerseyNumber) {
        this.jerseyNumber = jerseyNumber;
    }

    public Boolean getPlaying() {
        return playing;
    }

    public void setPlaying(Boolean playing) {
        this.playing = playing;
    }

    public Boolean getCaptain() {
        return captain;
    }

    public void setCaptain(Boolean captain) {
        this.captain = captain;
    }

    public Boolean getWicketKeeper() {
        return wicketKeeper;
    }

    public void setWicketKeeper(Boolean wicketKeeper) {
        this.wicketKeeper = wicketKeeper;
    }
}