package com.cricklocal.dto;

import java.util.List;

public class LeaderboardResponse {

    private String category;
    private List<LeaderboardEntryResponse> entries;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<LeaderboardEntryResponse> getEntries() {
        return entries;
    }

    public void setEntries(List<LeaderboardEntryResponse> entries) {
        this.entries = entries;
    }
}