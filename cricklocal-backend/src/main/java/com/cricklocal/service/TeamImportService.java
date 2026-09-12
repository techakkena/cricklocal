package com.cricklocal.service;

import com.cricklocal.dto.TeamImportError;
import com.cricklocal.dto.TeamImportResponse;
import com.cricklocal.entity.Team;
import com.cricklocal.repository.TeamRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TeamImportService {

    private static final String HEADER_NAME = "Team Name";
    private static final String HEADER_SHORT_NAME = "Short Name";
    private static final String HEADER_CITY = "City";

    private final TeamRepository teamRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    public TeamImportService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Transactional(readOnly = true)
    public TeamImportResponse validateExcel(MultipartFile file) {
        ParsedTeamImport parsed = parseAndValidate(file);

        return new TeamImportResponse(
                parsed.totalRows(),
                0,
                parsed.errors());
    }

    @Transactional
    public TeamImportResponse importExcel(MultipartFile file) {
        ParsedTeamImport parsed = parseAndValidate(file);

        if (!parsed.errors().isEmpty()) {
            return new TeamImportResponse(
                    parsed.totalRows(),
                    0,
                    parsed.errors());
        }

        List<Team> teams = new ArrayList<>();

        for (TeamRow row : parsed.rows()) {
            Team team = new Team();

            team.setName(row.name());
            team.setShortName(row.shortName());
            team.setCity(row.city());

            teams.add(team);
        }

        teamRepository.saveAll(teams);

        return new TeamImportResponse(
                parsed.totalRows(),
                teams.size(),
                List.of());
    }

    private ParsedTeamImport parseAndValidate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel file is required");
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !filename.toLowerCase().endsWith(".xlsx")) {

            throw new IllegalArgumentException(
                    "Only .xlsx Excel files are supported");
        }

        List<TeamImportError> errors = new ArrayList<>();
        List<TeamRow> rows = new ArrayList<>();
        int totalRows = 0;

        Set<String> namesInWorkbook = new HashSet<>();
        Set<String> shortNamesInWorkbook = new HashSet<>();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Excel workbook must contain a worksheet");
            }

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            validateHeaders(headerRow);

            for (int rowIndex = 1;
                 rowIndex <= sheet.getLastRowNum();
                 rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null || isBlankRow(row)) {
                    continue;
                }

                totalRows++;

                int rowNumber = rowIndex + 1;

                String name = getCellValue(row.getCell(0));
                String shortName = getCellValue(row.getCell(1));
                String city = getCellValue(row.getCell(2));

                int errorsBefore = errors.size();

                validateRow(
                        rowNumber,
                        name,
                        shortName,
                        city,
                        errors,
                        namesInWorkbook,
                        shortNamesInWorkbook);

                if (errors.size() == errorsBefore) {
                    rows.add(new TeamRow(name, shortName, city));
                }
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Unable to read the Excel file");
        }

        return new ParsedTeamImport(totalRows, rows, errors);
    }

    private void validateHeaders(Row headerRow) {

        if (headerRow == null) {
            throw new IllegalArgumentException(
                    "Excel header row is required");
        }

        String name = getCellValue(headerRow.getCell(0));
        String shortName = getCellValue(headerRow.getCell(1));
        String city = getCellValue(headerRow.getCell(2));

        if (!HEADER_NAME.equals(name)
                || !HEADER_SHORT_NAME.equals(shortName)
                || !HEADER_CITY.equals(city)) {

            throw new IllegalArgumentException(
                    "Invalid Excel headers. Expected: Team Name, Short Name, City");
        }
    }

    private void validateRow(
            int rowNumber,
            String name,
            String shortName,
            String city,
            List<TeamImportError> errors,
            Set<String> namesInWorkbook,
            Set<String> shortNamesInWorkbook) {

        boolean validName = true;
        boolean validShortName = true;

        if (name.isBlank()) {
            errors.add(new TeamImportError(
                    rowNumber,
                    "Team name is required"));
            validName = false;
        } else if (name.length() > 100) {
            errors.add(new TeamImportError(
                    rowNumber,
                    "Team name must not exceed 100 characters"));
            validName = false;
        }

        if (shortName.isBlank()) {
            errors.add(new TeamImportError(
                    rowNumber,
                    "Short name is required"));
            validShortName = false;
        } else if (shortName.length() > 20) {
            errors.add(new TeamImportError(
                    rowNumber,
                    "Short name must not exceed 20 characters"));
            validShortName = false;
        }

        if (city.length() > 100) {
            errors.add(new TeamImportError(
                    rowNumber,
                    "City must not exceed 100 characters"));
        }

        if (validName) {
            if (teamRepository.existsByName(name)) {
                errors.add(new TeamImportError(
                        rowNumber,
                        "Team name already exists"));
            }

            if (!namesInWorkbook.add(name)) {
                errors.add(new TeamImportError(
                        rowNumber,
                        "Duplicate team name in Excel file"));
            }
        }

        if (validShortName) {
            if (teamRepository.existsByShortName(shortName)) {
                errors.add(new TeamImportError(
                        rowNumber,
                        "Short name already exists"));
            }

            if (!shortNamesInWorkbook.add(shortName)) {
                errors.add(new TeamImportError(
                        rowNumber,
                        "Duplicate short name in Excel file"));
            }
        }
    }

    private boolean isBlankRow(Row row) {
        return getCellValue(row.getCell(0)).isBlank()
                && getCellValue(row.getCell(1)).isBlank()
                && getCellValue(row.getCell(2)).isBlank();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        return dataFormatter.formatCellValue(cell).trim();
    }

    private record TeamRow(
            String name,
            String shortName,
            String city) {
    }

    private record ParsedTeamImport(
            int totalRows,
            List<TeamRow> rows,
            List<TeamImportError> errors) {
    }
}







