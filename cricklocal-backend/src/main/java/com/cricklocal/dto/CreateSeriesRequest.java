package com.cricklocal.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateSeriesRequest {

    @NotBlank(message = "Series name is required")
    @Size(
        max = 150,
        message = "Series name must not exceed 150 characters"
    )
    private String name;

    @NotNull(message = "Total matches is required")
    @Min(
        value = 1,
        message = "Total matches must be at least 1"
    )
    private Integer totalMatches;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

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
}