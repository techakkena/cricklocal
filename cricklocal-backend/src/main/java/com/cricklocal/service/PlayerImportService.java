package com.cricklocal.service;

import com.cricklocal.dto.PlayerImportError;
import com.cricklocal.dto.PlayerImportResponse;
import com.cricklocal.entity.Player;
import com.cricklocal.enums.BattingStyle;
import com.cricklocal.enums.BowlingStyle;
import com.cricklocal.enums.PlayerRole;
import com.cricklocal.repository.PlayerRepository;
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
public class PlayerImportService {

    private static final String HEADER_FIRST_NAME = "First Name";
    private static final String HEADER_LAST_NAME = "Last Name";
    private static final String HEADER_DISPLAY_NAME = "Display Name";
    private static final String HEADER_PHONE = "Phone";
    private static final String HEADER_BATTING_STYLE = "Batting Style";
    private static final String HEADER_BOWLING_STYLE = "Bowling Style";
    private static final String HEADER_ROLE = "Role";

    private final PlayerRepository playerRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    public PlayerImportService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional(readOnly = true)
    public PlayerImportResponse validateExcel(MultipartFile file) {
        ParsedPlayerImport parsed = parseAndValidate(file);

        return new PlayerImportResponse(
                parsed.totalRows(),
                0,
                parsed.errors());
    }

    @Transactional
    public PlayerImportResponse importExcel(MultipartFile file) {
        ParsedPlayerImport parsed = parseAndValidate(file);

        if (!parsed.errors().isEmpty()) {
            return new PlayerImportResponse(
                    parsed.totalRows(),
                    0,
                    parsed.errors());
        }

        List<Player> players = new ArrayList<>();

        for (PlayerRow row : parsed.rows()) {
            Player player = new Player();

            player.setFirstName(row.firstName());
            player.setLastName(row.lastName());
            player.setDisplayName(row.displayName());
            player.setPhone(row.phone());
            player.setBattingStyle(row.battingStyle());
            player.setBowlingStyle(row.bowlingStyle());
            player.setRole(row.role());

            players.add(player);
        }

        playerRepository.saveAll(players);

        return new PlayerImportResponse(
                parsed.totalRows(),
                players.size(),
                List.of());
    }

    private ParsedPlayerImport parseAndValidate(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel file is required");
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !filename.toLowerCase().endsWith(".xlsx")) {

            throw new IllegalArgumentException(
                    "Only .xlsx Excel files are supported");
        }

        List<PlayerImportError> errors = new ArrayList<>();
        List<PlayerRow> rows = new ArrayList<>();

        int totalRows = 0;

        Set<String> displayNamesInWorkbook = new HashSet<>();

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

                String firstName = getCellValue(row.getCell(0));
                String lastName = getCellValue(row.getCell(1));
                String displayName = getCellValue(row.getCell(2));
                String phone = getCellValue(row.getCell(3));
                String battingStyleValue = getCellValue(row.getCell(4));
                String bowlingStyleValue = getCellValue(row.getCell(5));
                String roleValue = getCellValue(row.getCell(6));

                int errorsBefore = errors.size();

                PlayerRole role = parseRole(
                        rowNumber,
                        roleValue,
                        errors);

                BattingStyle battingStyle = parseBattingStyle(
                        rowNumber,
                        battingStyleValue,
                        errors);

                BowlingStyle bowlingStyle = parseBowlingStyle(
                        rowNumber,
                        bowlingStyleValue,
                        errors);

                validateRow(
                        rowNumber,
                        firstName,
                        lastName,
                        displayName,
                        phone,
                        errors,
                        displayNamesInWorkbook);

                if (errors.size() == errorsBefore) {
                    rows.add(new PlayerRow(
                            firstName,
                            lastName.isBlank() ? null : lastName,
                            displayName,
                            phone.isBlank() ? null : phone,
                            battingStyle,
                            bowlingStyle,
                            role));
                }
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "Unable to read the Excel file");
        }

        return new ParsedPlayerImport(
                totalRows,
                rows,
                errors);
    }

    private void validateHeaders(Row headerRow) {

        if (headerRow == null) {
            throw new IllegalArgumentException(
                    "Excel header row is required");
        }

        String firstName = getCellValue(headerRow.getCell(0));
        String lastName = getCellValue(headerRow.getCell(1));
        String displayName = getCellValue(headerRow.getCell(2));
        String phone = getCellValue(headerRow.getCell(3));
        String battingStyle = getCellValue(headerRow.getCell(4));
        String bowlingStyle = getCellValue(headerRow.getCell(5));
        String role = getCellValue(headerRow.getCell(6));

        if (!HEADER_FIRST_NAME.equals(firstName)
                || !HEADER_LAST_NAME.equals(lastName)
                || !HEADER_DISPLAY_NAME.equals(displayName)
                || !HEADER_PHONE.equals(phone)
                || !HEADER_BATTING_STYLE.equals(battingStyle)
                || !HEADER_BOWLING_STYLE.equals(bowlingStyle)
                || !HEADER_ROLE.equals(role)) {

            throw new IllegalArgumentException(
                    "Invalid Excel headers. Expected: First Name, Last Name, Display Name, Phone, Batting Style, Bowling Style, Role");
        }
    }

    private void validateRow(
            int rowNumber,
            String firstName,
            String lastName,
            String displayName,
            String phone,
            List<PlayerImportError> errors,
            Set<String> displayNamesInWorkbook) {

        boolean validDisplayName = true;

        if (firstName.isBlank()) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "First name is required"));
        } else if (firstName.length() > 50) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "First name must not exceed 50 characters"));
        }

        if (lastName.length() > 50) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "Last name must not exceed 50 characters"));
        }

        if (displayName.isBlank()) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "Display name is required"));
            validDisplayName = false;
        } else if (displayName.length() > 100) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "Display name must not exceed 100 characters"));
            validDisplayName = false;
        }

        if (phone.length() > 20) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "Phone must not exceed 20 characters"));
        }

        if (validDisplayName) {
            if (playerRepository.existsByDisplayName(displayName)) {
                errors.add(new PlayerImportError(
                        rowNumber,
                        "Display name already exists"));
            }

            if (!displayNamesInWorkbook.add(displayName)) {
                errors.add(new PlayerImportError(
                        rowNumber,
                        "Duplicate display name in Excel file"));
            }
        }
    }

    private PlayerRole parseRole(
            int rowNumber,
            String value,
            List<PlayerImportError> errors) {

        if (value.isBlank()) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "Role is required"));
            return null;
        }

        try {
            return PlayerRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "Invalid role. Expected: BATTER, BOWLER, ALL_ROUNDER, WICKET_KEEPER"));
            return null;
        }
    }

    private BattingStyle parseBattingStyle(
            int rowNumber,
            String value,
            List<PlayerImportError> errors) {

        if (value.isBlank()) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "Batting style is required"));
            return null;
        }

        try {
            return BattingStyle.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "Invalid batting style. Expected: RIGHT_HAND, LEFT_HAND"));
            return null;
        }
    }

    private BowlingStyle parseBowlingStyle(
            int rowNumber,
            String value,
            List<PlayerImportError> errors) {

        if (value.isBlank()) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "Bowling style is required"));
            return null;
        }

        try {
            return BowlingStyle.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            errors.add(new PlayerImportError(
                    rowNumber,
                    "Invalid bowling style"));
            return null;
        }
    }

    private boolean isBlankRow(Row row) {
        for (int column = 0; column < 7; column++) {
            if (!getCellValue(row.getCell(column)).isBlank()) {
                return false;
            }
        }

        return true;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        return dataFormatter.formatCellValue(cell).trim();
    }

    private record PlayerRow(
            String firstName,
            String lastName,
            String displayName,
            String phone,
            BattingStyle battingStyle,
            BowlingStyle bowlingStyle,
            PlayerRole role) {
    }

    private record ParsedPlayerImport(
            int totalRows,
            List<PlayerRow> rows,
            List<PlayerImportError> errors) {
    }
}