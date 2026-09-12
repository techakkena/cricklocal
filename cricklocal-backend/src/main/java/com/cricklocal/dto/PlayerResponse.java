package com.cricklocal.dto;

import com.cricklocal.enums.BattingStyle;
import com.cricklocal.enums.BowlingStyle;
import com.cricklocal.enums.PlayerRole;

import java.time.Instant;
import java.util.List;

public class PlayerResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String displayName;
    private String phone;
    private BattingStyle battingStyle;
    private BowlingStyle bowlingStyle;
    private PlayerRole role;
    private Boolean active;
    private Instant createdAt;
    private List<PlayerTeamResponse> teams;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BattingStyle getBattingStyle() {
        return battingStyle;
    }

    public void setBattingStyle(BattingStyle battingStyle) {
        this.battingStyle = battingStyle;
    }

    public BowlingStyle getBowlingStyle() {
        return bowlingStyle;
    }

    public void setBowlingStyle(BowlingStyle bowlingStyle) {
        this.bowlingStyle = bowlingStyle;
    }

    public PlayerRole getRole() {
        return role;
    }

    public void setRole(PlayerRole role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<PlayerTeamResponse> getTeams() {
        return teams;
    }

    public void setTeams(List<PlayerTeamResponse> teams) {
        this.teams = teams;
    }
}