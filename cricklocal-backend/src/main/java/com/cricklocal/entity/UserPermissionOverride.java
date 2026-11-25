package com.cricklocal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_permission_overrides",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_permission_override_module", columnNames = {"user_id", "module_id"}))
public class UserPermissionOverride {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private PermissionModule module;
    private Boolean canView;
    private Boolean canCreate;
    private Boolean canEdit;
    private Boolean canDelete;
    private Boolean canApprove;

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public PermissionModule getModule() { return module; }
    public void setModule(PermissionModule module) { this.module = module; }
    public Boolean getCanView() { return canView; }
    public void setCanView(Boolean value) { canView = value; }
    public Boolean getCanCreate() { return canCreate; }
    public void setCanCreate(Boolean value) { canCreate = value; }
    public Boolean getCanEdit() { return canEdit; }
    public void setCanEdit(Boolean value) { canEdit = value; }
    public Boolean getCanDelete() { return canDelete; }
    public void setCanDelete(Boolean value) { canDelete = value; }
    public Boolean getCanApprove() { return canApprove; }
    public void setCanApprove(Boolean value) { canApprove = value; }
}
