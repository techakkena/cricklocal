package com.cricklocal.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "user_permission_overrides",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_permission_override",
            columnNames = {"user_id", "module_id", "action"}
        )
    }
)
public class UserPermissionOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private PermissionModule module;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private PermissionAction action;

    /*
     * null  = use role default
     * true  = explicitly allow
     * false = explicitly deny
     */
    @Column(name = "allowed")
    private Boolean allowed;

    public UserPermissionOverride() {
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PermissionModule getModule() {
        return module;
    }

    public void setModule(PermissionModule module) {
        this.module = module;
    }

    public PermissionAction getAction() {
        return action;
    }

    public void setAction(PermissionAction action) {
        this.action = action;
    }

    public Boolean getAllowed() {
        return allowed;
    }

    public void setAllowed(Boolean allowed) {
        this.allowed = allowed;
    }
}