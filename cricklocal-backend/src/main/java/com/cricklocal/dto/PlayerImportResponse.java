package com.cricklocal.dto;

import java.util.List;

public class PlayerImportResponse {

    private int totalRows;
    private int createdRows;
    private List<PlayerImportError> errors;
    private List<PlayerRegistrationImportResponse> registrations;

    public PlayerImportResponse() {
    }

    public PlayerImportResponse(
            int totalRows,
            int createdRows,
            List<PlayerImportError> errors) {

        this.totalRows = totalRows;
        this.createdRows = createdRows;
        this.errors = errors;
        this.registrations = List.of();
    }

    public PlayerImportResponse(
            int totalRows,
            int createdRows,
            List<PlayerImportError> errors,
            List<PlayerRegistrationImportResponse> registrations) {

        this.totalRows = totalRows;
        this.createdRows = createdRows;
        this.errors = errors;
        this.registrations = registrations;
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

    public List<PlayerRegistrationImportResponse> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(
            List<PlayerRegistrationImportResponse> registrations) {

        this.registrations = registrations;
    }
}