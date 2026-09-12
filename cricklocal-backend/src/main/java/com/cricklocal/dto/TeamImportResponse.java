package com.cricklocal.dto;

import java.util.List;

public class TeamImportResponse {

    private int totalRows;
    private int createdRows;
    private List<TeamImportError> errors;

    public TeamImportResponse() {
    }

    public TeamImportResponse(
            int totalRows,
            int createdRows,
            List<TeamImportError> errors) {

        this.totalRows = totalRows;
        this.createdRows = createdRows;
        this.errors = errors;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getCreatedRows() {
        return createdRows;
    }

    public void setCreatedRows(int createdRows) {
        this.createdRows = createdRows;
    }

    public List<TeamImportError> getErrors() {
        return errors;
    }

    public void setErrors(List<TeamImportError> errors) {
        this.errors = errors;
    }
}