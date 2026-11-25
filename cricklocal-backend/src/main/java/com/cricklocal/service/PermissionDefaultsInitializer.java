package com.cricklocal.service;

import com.cricklocal.entity.PermissionModule;
import com.cricklocal.entity.RolePermission;
import com.cricklocal.entity.UserRole;
import com.cricklocal.repository.PermissionModuleRepository;
import com.cricklocal.repository.RolePermissionRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class PermissionDefaultsInitializer {
    private final PermissionModuleRepository moduleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public PermissionDefaultsInitializer(
            PermissionModuleRepository moduleRepository,
            RolePermissionRepository rolePermissionRepository) {
        this.moduleRepository = moduleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDefaults() {
        List<ModuleDefinition> definitions = List.of(
                new ModuleDefinition("DASHBOARD", "Dashboard"),
                new ModuleDefinition("TEAMS", "Teams"),
                new ModuleDefinition("PLAYERS", "Players"),
                new ModuleDefinition("GLOBAL_PLAYERS", "Global Player Directory"),
                new ModuleDefinition("LOCAL_PLAYERS", "Local Players"),
                new ModuleDefinition("PLAYER_REGISTRATIONS", "Player Registrations"),
                new ModuleDefinition("SERIES", "Series"),
                new ModuleDefinition("MATCHES", "Matches"),
                new ModuleDefinition("MATCH_PREPARATION", "Match Preparation"),
                new ModuleDefinition("MATCH_CENTER", "Match Center"),
                new ModuleDefinition("LIVE_SCORING", "Live Scoring"),
                new ModuleDefinition("SCORECARDS", "Scorecards"),
                new ModuleDefinition("STATISTICS", "Statistics"),
                new ModuleDefinition("LEADERBOARDS", "Leaderboards"),
                new ModuleDefinition("HISTORY", "History"),
                new ModuleDefinition("SETTINGS", "Settings")
        );

        for (ModuleDefinition definition : definitions) {
            PermissionModule module = moduleRepository
                    .findByModuleKey(definition.key())
                    .orElseGet(PermissionModule::new);
            module.setModuleKey(definition.key());
            module.setModuleName(definition.name());
            module.setActive(true);
            module = moduleRepository.save(module);

            ensureRolePermission(UserRole.PLAYER, module, definition.player());
            ensureRolePermission(UserRole.ADMIN, module, definition.admin());
        }
    }

    private void ensureRolePermission(
            UserRole role, PermissionModule module, PermissionSet set) {
        RolePermission permission = rolePermissionRepository
                .findByRoleAndModuleId(role, module.getId())
                .orElseGet(RolePermission::new);
        permission.setRole(role);
        permission.setModule(module);
        permission.setCanView(set.view());
        permission.setCanCreate(set.create());
        permission.setCanEdit(set.edit());
        permission.setCanDelete(set.delete());
        permission.setCanApprove(set.approve());
        rolePermissionRepository.save(permission);
    }

    private record ModuleDefinition(
            String key, String name, PermissionSet player, PermissionSet admin) {
        ModuleDefinition(String key, String name) {
            this(key, name, playerDefaults(key), adminDefaults(key));
        }

        private static PermissionSet playerDefaults(String key) {
            return switch (key) {
                case "DASHBOARD", "TEAMS", "PLAYERS", "GLOBAL_PLAYERS",
                        "LOCAL_PLAYERS", "SERIES", "MATCHES", "MATCH_PREPARATION",
                        "MATCH_CENTER", "LIVE_SCORING", "SCORECARDS", "STATISTICS",
                        "LEADERBOARDS", "HISTORY", "SETTINGS" -> PermissionSet.viewOnly();
                default -> PermissionSet.none();
            };
        }

        private static PermissionSet adminDefaults(String key) {
            return switch (key) {
                case "TEAMS", "PLAYERS", "GLOBAL_PLAYERS", "LOCAL_PLAYERS",
                        "SERIES", "MATCHES", "MATCH_PREPARATION", "MATCH_CENTER",
                        "LIVE_SCORING", "SCORECARDS" -> PermissionSet.full();
                case "DASHBOARD", "STATISTICS", "LEADERBOARDS", "HISTORY", "SETTINGS"
                        -> PermissionSet.viewOnly();
                case "PLAYER_REGISTRATIONS"
                        -> new PermissionSet(true, false, false, false, true);
                default -> PermissionSet.none();
            };
        }
    }

    private record PermissionSet(
            boolean view, boolean create, boolean edit,
            boolean delete, boolean approve) {
        static PermissionSet none() {
            return new PermissionSet(false, false, false, false, false);
        }
        static PermissionSet viewOnly() {
            return new PermissionSet(true, false, false, false, false);
        }
        static PermissionSet full() {
            return new PermissionSet(true, true, true, true, true);
        }
    }
}
