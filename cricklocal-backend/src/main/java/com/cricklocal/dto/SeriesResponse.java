package com.cricklocal.dto;

import com.cricklocal.enums.SeriesStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class SeriesResponse {

    private Long id;
    private String name;
    private Integer totalMatches;
    private SeriesStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
    private List<SeriesTeamResponse> teams;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotalMatches() {
        return totalMatches;
    }

    public void setTotalMatches(Integer totalMatches) {
        this.totalMatches = totalMatches;
    }

    public SeriesStatus getStatus() {
        return status;
    }

    public void setStatus(SeriesStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<SeriesTeamResponse> getTeams() {
        return teams;
    }

    public void setTeams(List<SeriesTeamResponse> teams) {
        this.teams = teams;
    }
}