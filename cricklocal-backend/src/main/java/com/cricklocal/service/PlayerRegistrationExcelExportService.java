package com.cricklocal.service;

import com.cricklocal.entity.Player;
import com.cricklocal.entity.PlayerRegistrationInvitation;
import com.cricklocal.repository.PlayerRegistrationInvitationRepository;
import com.cricklocal.repository.PlayerRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@Service
public class PlayerRegistrationExcelExportService {

    private static final String REGISTRATION_BASE_URL =
            "http://localhost:5173/player-registration/";

    private final PlayerRepository playerRepository;
    private final PlayerRegistrationInvitationRepository invitationRepository;

    public PlayerRegistrationExcelExportService(
            PlayerRepository playerRepository,
            PlayerRegistrationInvitationRepository invitationRepository) {

        this.playerRepository = playerRepository;
        this.invitationRepository = invitationRepository;
    }

    @Transactional(readOnly = true)
    public byte[] exportRegistrations() {

        List<Player> players = playerRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Player Registrations");

            String[] headers = {
                    "Player Name",
                    "Jersey Number",
                    "Team",
                    "Batting Style",
                    "Bowling Style",
                    "Role",
                    "Registration Status",
                    "Invitation Created Date",
                    "Invitation Expiry Date",
                    "Registration Link"
            };

            Row headerRow = sheet.createRow(0);

            for (int column = 0; column < headers.length; column++) {
                Cell cell = headerRow.createCell(column);
                cell.setCellValue(headers[column]);
            }

            int rowNumber = 1;

            for (Player player : players) {

                Row row = sheet.createRow(rowNumber++);

                PlayerRegistrationInvitation invitation =
                        invitationRepository.findByPlayer(player)
                                .stream()
                                .max(
                                        Comparator
                                                .comparing(
                                                        PlayerRegistrationInvitation::getCreatedAt,
                                                        Comparator.nullsFirst(
                                                                Comparator.naturalOrder()))
                                                .thenComparing(
                                                        PlayerRegistrationInvitation::getId,
                                                        Comparator.nullsFirst(
                                                                Comparator.naturalOrder()))
                                )
                                .orElse(null);

                row.createCell(0).setCellValue(
                        player.getDisplayName() != null
                                ? player.getDisplayName()
                                : ""
                );

                row.createCell(1).setCellValue("");

                row.createCell(2).setCellValue("");

                row.createCell(3).setCellValue(
                        player.getBattingStyle() != null
                                ? player.getBattingStyle().name()
                                : ""
                );

                row.createCell(4).setCellValue(
                        player.getBowlingStyle() != null
                                ? player.getBowlingStyle().name()
                                : ""
                );

                row.createCell(5).setCellValue(
                        player.getRole() != null
                                ? player.getRole().name()
                                : ""
                );

                row.createCell(6).setCellValue(
                        player.getRegistrationStatus() != null
                                ? player.getRegistrationStatus().name()
                                : ""
                );

                row.createCell(7).setCellValue(
                        invitation != null && invitation.getCreatedAt() != null
                                ? invitation.getCreatedAt().toString()
                                : ""
                );

                row.createCell(8).setCellValue(
                        invitation != null && invitation.getExpiresAt() != null
                                ? invitation.getExpiresAt().toString()
                                : ""
                );

                row.createCell(9).setCellValue(
                        invitation != null && invitation.getToken() != null
                                ? REGISTRATION_BASE_URL + invitation.getToken()
                                : ""
                );
            }

            for (int column = 0; column < headers.length; column++) {
                sheet.autoSizeColumn(column);
            }

            workbook.write(outputStream);

            return outputStream.toByteArray();

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to generate player registration Excel",
                    exception
            );
        }
    }
}