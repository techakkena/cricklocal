package com.cricklocal.dto;

import java.util.List;

public class PlayerImportResponse {

    private int totalRows;
    private int createdRows;
    private List<PlayerImportError> errors;

    public PlayerImportResponse() {
    }

    public PlayerImportResponse(
            int totalRows,
            int createdRows,
            List<PlayerImportError> errors) {

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

    public List<PlayerImportError> getErrors() {
        return errors;
    }

    public void setErrors(List<PlayerImportError> errors) {
        this.errors = errors;
    }
}