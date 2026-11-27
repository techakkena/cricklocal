package com.cricklocal.config;

import com.cricklocal.entity.PermissionAction;
import com.cricklocal.entity.PermissionModule;
import com.cricklocal.entity.RolePermission;
import com.cricklocal.entity.UserRole;
import com.cricklocal.repository.PermissionModuleRepository;
import com.cricklocal.repository.RolePermissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class PermissionDefaultsInitializer implements CommandLineRunner {

    private final PermissionModuleRepository permissionModuleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public PermissionDefaultsInitializer(
            PermissionModuleRepository permissionModuleRepository,
            RolePermissionRepository rolePermissionRepository) {

        this.permissionModuleRepository = permissionModuleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initializeModules();
        initializeRolePermissions();
    }

    private void initializeModules() {

        List<ModuleDefinition> modules = List.of(
                new ModuleDefinition("DASHBOARD", "Dashboard"),
                new ModuleDefinition("TEAMS", "Teams"),
                new ModuleDefinition("PLAYERS", "Players"),
                new ModuleDefinition("GLOBAL_PLAYERS", "Global Players"),
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

        for (ModuleDefinition definition : modules) {

            permissionModuleRepository
                    .findByModuleKey(definition.key())
                    .orElseGet(() ->
                            permissionModuleRepository.save(
                                    new PermissionModule(
                                            definition.key(),
                                            definition.name()
                                    )
                            )
                    );
        }
    }

    private void initializeRolePermissions() {

        Map<String, Set<PermissionAction>> adminPermissions =
                new LinkedHashMap<>();

        adminPermissions.put(
                "DASHBOARD",
                actions(PermissionAction.VIEW)
        );

        adminPermissions.put(
                "TEAMS",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE
                )
        );

        adminPermissions.put(
                "PLAYERS",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE
                )
        );

        adminPermissions.put(
                "GLOBAL_PLAYERS",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE
                )
        );

        adminPermissions.put(
                "LOCAL_PLAYERS",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE
                )
        );

        adminPermissions.put(
                "PLAYER_REGISTRATIONS",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE,
                        PermissionAction.APPROVE
                )
        );

        adminPermissions.put(
                "SERIES",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE
                )
        );

        adminPermissions.put(
                "MATCHES",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE
                )
        );

        adminPermissions.put(
                "MATCH_PREPARATION",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE
                )
        );

        adminPermissions.put(
                "MATCH_CENTER",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE
                )
        );

        adminPermissions.put(
                "LIVE_SCORING",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE
                )
        );

        adminPermissions.put(
                "SCORECARDS",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.CREATE,
                        PermissionAction.EDIT,
                        PermissionAction.DELETE
                )
        );

        adminPermissions.put(
                "STATISTICS",
                actions(PermissionAction.VIEW)
        );

        adminPermissions.put(
                "LEADERBOARDS",
                actions(PermissionAction.VIEW)
        );

        adminPermissions.put(
                "HISTORY",
                actions(PermissionAction.VIEW)
        );

        adminPermissions.put(
                "SETTINGS",
                actions(
                        PermissionAction.VIEW,
                        PermissionAction.EDIT
                )
        );

        Map<String, Set<PermissionAction>> playerPermissions =
                new LinkedHashMap<>();

        playerPermissions.put(
                "DASHBOARD",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "TEAMS",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "PLAYERS",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "GLOBAL_PLAYERS",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "LOCAL_PLAYERS",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "PLAYER_REGISTRATIONS",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "SERIES",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "MATCHES",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "MATCH_PREPARATION",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "MATCH_CENTER",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "LIVE_SCORING",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "SCORECARDS",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "STATISTICS",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "LEADERBOARDS",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "HISTORY",
                actions(PermissionAction.VIEW)
        );

        playerPermissions.put(
                "SETTINGS",
                actions(PermissionAction.VIEW)
        );

        initializeRole(
                UserRole.ADMIN,
                adminPermissions
        );

        initializeRole(
                UserRole.PLAYER,
                playerPermissions
        );
    }

    private void initializeRole(
            UserRole role,
            Map<String, Set<PermissionAction>> permissions) {

        for (Map.Entry<String, Set<PermissionAction>> entry :
                permissions.entrySet()) {

            String moduleKey = entry.getKey();

            PermissionModule module =
                    permissionModuleRepository
                            .findByModuleKey(moduleKey)
                            .orElseThrow(() ->
                                    new IllegalStateException(
                                            "Permission module not found: "
                                                    + moduleKey
                                    )
                            );

            /*
             * Create a row for every action.
             *
             * allowed = true  -> default permission enabled
             * allowed = false -> default permission disabled
             */
            for (PermissionAction action :
                    EnumSet.allOf(PermissionAction.class)) {

                boolean allowed =
                        entry.getValue().contains(action);

                RolePermission rolePermission =
                        rolePermissionRepository
                                .findByRoleAndModule_ModuleKeyAndAction(
                                        role,
                                        moduleKey,
                                        action
                                )
                                .orElse(null);

                if (rolePermission == null) {

                    rolePermission = new RolePermission();

                    rolePermission.setRole(role);
                    rolePermission.setModule(module);
                    rolePermission.setAction(action);
                    rolePermission.setAllowed(allowed);

                    rolePermissionRepository.save(rolePermission);
                }
            }
        }
    }

    private Set<PermissionAction> actions(
            PermissionAction... actions) {

        return EnumSet.of(actions[0], actions);
    }

    private record ModuleDefinition(
            String key,
            String name
    ) {
    }
}