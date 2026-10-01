package com.cricklocal.controller;

import com.cricklocal.dto.CreatePlayerRequest;
import com.cricklocal.dto.PlayerImportResponse;
import com.cricklocal.dto.PlayerResponse;
import com.cricklocal.dto.PlayerRegistrationSummaryResponse;
import com.cricklocal.dto.PlayerRegistrationDetailResponse;
import com.cricklocal.dto.PlayerRegistrationRegenerateResponse;
import com.cricklocal.dto.PlayerRegistrationLinkResponse;
import com.cricklocal.entity.User;
import com.cricklocal.exception.ForbiddenException;
import com.cricklocal.service.AuthenticationService;
import com.cricklocal.service.AdminAccessService;
import com.cricklocal.dto.PlayerRegistrationManagementResponse;
import com.cricklocal.service.PlayerRegistrationManagementService;
import com.cricklocal.service.PlayerRegistrationSummaryService;
import com.cricklocal.service.PlayerRegistrationDetailService;
import com.cricklocal.service.PlayerRegistrationRegenerateService;
import com.cricklocal.service.PlayerRegistrationExcelExportService;
import com.cricklocal.service.PlayerRegistrationLinkService;
import com.cricklocal.service.PlayerRegistrationInvitationBackfillService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import com.cricklocal.service.PlayerImportService;
import com.cricklocal.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;


import java.util.List;

@RestController
@RequestMapping("/api/players")
public class PlayerController {
    
    private final PlayerService playerService;
    private final PlayerImportService playerImportService;
    private final AuthenticationService authenticationService;
    private final AdminAccessService adminAccessService;
    private final PlayerRegistrationManagementService playerRegistrationManagementService;
    private final PlayerRegistrationSummaryService playerRegistrationSummaryService;
    private final PlayerRegistrationDetailService playerRegistrationDetailService;
    private final PlayerRegistrationRegenerateService playerRegistrationRegenerateService;
    private final PlayerRegistrationExcelExportService playerRegistrationExcelExportService;
    private final PlayerRegistrationLinkService playerRegistrationLinkService;
    private final PlayerRegistrationInvitationBackfillService playerRegistrationInvitationBackfillService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public PlayerController(
            PlayerService playerService,
            PlayerImportService playerImportService,
            AuthenticationService authenticationService,
            AdminAccessService adminAccessService,
            PlayerRegistrationManagementService playerRegistrationManagementService,
            PlayerRegistrationSummaryService playerRegistrationSummaryService,
            PlayerRegistrationDetailService playerRegistrationDetailService,
            PlayerRegistrationRegenerateService playerRegistrationRegenerateService,
            PlayerRegistrationExcelExportService playerRegistrationExcelExportService,
            PlayerRegistrationLinkService playerRegistrationLinkService,
            PlayerRegistrationInvitationBackfillService playerRegistrationInvitationBackfillService,
            ClientRegistrationRepository clientRegistrationRepository) {

        this.playerService = playerService;
        this.playerImportService = playerImportService;
        this.authenticationService = authenticationService;
        this.adminAccessService = adminAccessService;
        this.playerRegistrationManagementService = playerRegistrationManagementService;
        this.playerRegistrationSummaryService = playerRegistrationSummaryService;
        this.playerRegistrationDetailService = playerRegistrationDetailService;
        this.playerRegistrationRegenerateService = playerRegistrationRegenerateService;
        this.playerRegistrationExcelExportService = playerRegistrationExcelExportService;
        this.playerRegistrationLinkService = playerRegistrationLinkService;
        this.playerRegistrationInvitationBackfillService = playerRegistrationInvitationBackfillService;
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse createPlayer(
            @Valid @RequestBody CreatePlayerRequest request) {

        return playerService.createPlayer(request);
    }

    @PostMapping("/local")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse createLocalPlayer(
                @Valid @RequestBody CreatePlayerRequest request) {

        return playerService.createLocalPlayer(request);
    }

    @GetMapping
    public List<PlayerResponse> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/global")
    public List<PlayerResponse> getGlobalPlayers() {
        return playerService.getGlobalPlayers();
    }

    @GetMapping("/local")
    public List<PlayerResponse> getLocalPlayers() {
        return playerService.getLocalPlayers();
    }

    @GetMapping("/{playerId:\\d+}")
    public PlayerResponse getPlayerById(
                @PathVariable Long playerId) {

        return playerService.getPlayerById(playerId);
    }

    @PostMapping("/import/validate")
    public PlayerImportResponse validatePlayerImport(
            @RequestParam("file") MultipartFile file) {

        return playerImportService.validateExcel(file);
    }

    @PostMapping("/import")
    public PlayerImportResponse importPlayers(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new IllegalStateException("Unsupported authentication provider");
        }

        User user =
                authenticationService.getUserForGoogleLogin(oauth2User);

        if (!adminAccessService.hasActiveAdminAccess(user)) {
            throw new ForbiddenException(
                    "Active admin access is required"
            );
        }

        return playerImportService.importExcel(
                file,
                user
        );
    }

    @GetMapping("/registrations")
    public List<PlayerRegistrationManagementResponse> getPlayerRegistrations(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }

        if (!(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            throw new IllegalStateException(
                    "Unsupported authentication provider");
        }

        User user =
                authenticationService.getUserForGoogleLogin(oauth2User);

        if (!adminAccessService.hasActiveAdminAccess(user)) {
            throw new ForbiddenException(
                    "Active admin access is required"
            );
        }

        return playerRegistrationManagementService.getRegistrations();
    }

    @GetMapping("/registrations/summary")
    public PlayerRegistrationSummaryResponse getPlayerRegistrationSummary(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "User is not authenticated");
        }

        if (!(authentication.getPrincipal()
                instanceof OAuth2User oauth2User)) {

            throw new IllegalStateException(
                    "Unsupported authentication provider");
        }

        User user =
                authenticationService
                        .getUserForGoogleLogin(oauth2User);

        if (!adminAccessService.hasActiveAdminAccess(user)) {
            throw new ForbiddenException(
                    "Active admin access is required");
        }

        return playerRegistrationSummaryService.getSummary();
    }

    @GetMapping("/registrations/{playerId}")
    public PlayerRegistrationDetailResponse getPlayerRegistrationDetail(
            @PathVariable Long playerId,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "User is not authenticated");
        }

        if (!(authentication.getPrincipal()
                instanceof OAuth2User oauth2User)) {

            throw new IllegalStateException(
                    "Unsupported authentication provider");
        }

        User user =
                authenticationService
                        .getUserForGoogleLogin(oauth2User);

        if (!adminAccessService.hasActiveAdminAccess(user)) {
            throw new ForbiddenException(
                    "Active admin access is required");
        }

        return playerRegistrationDetailService
                .getRegistrationDetail(playerId);
    }

    @PostMapping("/registrations/{playerId}/regenerate")
    public PlayerRegistrationRegenerateResponse regeneratePlayerRegistration(
            @PathVariable Long playerId,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated");
        }

        if (!(authentication.getPrincipal()
                instanceof OAuth2User oauth2User)) {

            throw new IllegalStateException(
                    "Unsupported authentication provider");
        }

        User user =
                authenticationService
                        .getUserForGoogleLogin(oauth2User);

        if (!adminAccessService.hasActiveAdminAccess(user)) {
            throw new ForbiddenException(
                    "Active admin access is required");
        }

        return playerRegistrationRegenerateService
                .regenerateInvitation(playerId, user);
    }

    @PostMapping("/registrations/{playerId}/invite")
    public PlayerRegistrationRegenerateResponse invitePlayerRegistration(
                @PathVariable Long playerId,
                Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

                throw new IllegalStateException(
                        "User is not authenticated");
        }

        if (!(authentication.getPrincipal()
                instanceof OAuth2User oauth2User)) {

                throw new IllegalStateException(
                        "Unsupported authentication provider");
        }

        User user =
                authenticationService
                        .getUserForGoogleLogin(oauth2User);

        if (!adminAccessService.hasActiveAdminAccess(user)) {
                throw new ForbiddenException(
                        "Active admin access is required");
        }

        return playerRegistrationRegenerateService
                .createInvitation(playerId, user);
    }

    @GetMapping(
            value = "/registrations/export",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    )
    public ResponseEntity<byte[]> exportPlayerRegistrations(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated");
        }

        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        User user =
                authenticationService.getUserForGoogleLogin(
                        oauth2User
                );

        if (user == null) {
            throw new IllegalStateException(
                    "Authenticated user not found");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalStateException(
                    "User is inactive");
        }

        if (!adminAccessService.hasActiveAdminAccess(user)) {
            throw new ForbiddenException(
                    "Active admin access is required");
        }

        byte[] excel =
                playerRegistrationExcelExportService
                        .exportRegistrations();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"player-registrations.xlsx\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .body(excel);
    }

    @GetMapping("/registrations/{playerId}/link")
    public PlayerRegistrationLinkResponse getPlayerRegistrationLink(
            @PathVariable Long playerId,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new IllegalStateException(
                    "User is not authenticated");
        }

        OAuth2User oauth2User =
                (OAuth2User) authentication.getPrincipal();

        User user =
                authenticationService.getUserForGoogleLogin(
                        oauth2User
                );

        if (user == null) {
            throw new IllegalStateException(
                    "Authenticated user not found");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new IllegalStateException(
                    "User is inactive");
        }

        if (!adminAccessService.hasActiveAdminAccess(user)) {
            throw new ForbiddenException(
                    "Active admin access is required");
        }

        return playerRegistrationLinkService
                .getRegistrationLink(playerId);
    }

    @PostMapping("/registrations/backfill-invitations")
    public PlayerRegistrationInvitationBackfillService
                .PlayerRegistrationInvitationBackfillResponse
                backfillPlayerRegistrationInvitations(
                        Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
                throw new IllegalStateException(
                        "User is not authenticated");
        }

        if (!(authentication.getPrincipal()
                instanceof OAuth2User oauth2User)) {
                throw new IllegalStateException(
                        "Unsupported authentication provider");
        }

        User user =
                authenticationService
                        .getUserForGoogleLogin(oauth2User);

        if (!adminAccessService.hasActiveAdminAccess(user)) {
                throw new ForbiddenException(
                        "Active admin access is required");
        }

        return playerRegistrationInvitationBackfillService
                .backfill(user);
    }
}