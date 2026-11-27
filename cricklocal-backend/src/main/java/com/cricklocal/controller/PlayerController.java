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
import org.springframework.security.access.prepost.PreAuthorize;


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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYERS', T(com.cricklocal.entity.PermissionAction).CREATE)")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerResponse createPlayer(
            @Valid @RequestBody CreatePlayerRequest request) {
                    
                    return playerService.createPlayer(request);
                }

    @PostMapping("/local")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYERS', T(com.cricklocal.entity.PermissionAction).CREATE)")
    public PlayerResponse createLocalPlayer(
                @Valid @RequestBody CreatePlayerRequest request) {

        return playerService.createLocalPlayer(request);
    }

    @GetMapping
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYERS', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<PlayerResponse> getAllPlayers() {
        return playerService.getAllPlayers();
    }

    @GetMapping("/global")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYERS', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<PlayerResponse> getGlobalPlayers() {
        return playerService.getGlobalPlayers();
    }

    @GetMapping("/local")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYERS', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public List<PlayerResponse> getLocalPlayers() {
        return playerService.getLocalPlayers();
    }

    @GetMapping("/{playerId:\\d+}")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYERS', T(com.cricklocal.entity.PermissionAction).VIEW)")
    public PlayerResponse getPlayerById(
                @PathVariable Long playerId) {

        return playerService.getPlayerById(playerId);
    }

    @PostMapping("/import/validate")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYERS', T(com.cricklocal.entity.PermissionAction).CREATE)")
    public PlayerImportResponse validatePlayerImport(
            @RequestParam("file") MultipartFile file) {

        return playerImportService.validateExcel(file);
    }

    @PostMapping("/import")
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYERS', T(com.cricklocal.entity.PermissionAction).CREATE)")
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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYER_REGISTRATIONS', T(com.cricklocal.entity.PermissionAction).VIEW)")
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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYER_REGISTRATIONS', T(com.cricklocal.entity.PermissionAction).VIEW)")
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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYER_REGISTRATIONS', T(com.cricklocal.entity.PermissionAction).VIEW)")
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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYER_REGISTRATIONS', T(com.cricklocal.entity.PermissionAction).EDIT)")
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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYER_REGISTRATIONS', T(com.cricklocal.entity.PermissionAction).EDIT)")
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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYER_REGISTRATIONS', T(com.cricklocal.entity.PermissionAction).VIEW)")
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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYER_REGISTRATIONS', T(com.cricklocal.entity.PermissionAction).VIEW)")
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
    @PreAuthorize("@permissionAuthorizationService.isAllowed(authentication, 'PLAYER_REGISTRATIONS', T(com.cricklocal.entity.PermissionAction).EDIT")
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