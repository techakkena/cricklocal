package com.cricklocal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "role_permissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_role_permission_module", columnNames = {"role", "module_id"}))
public class RolePermission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private PermissionModule module;
    @Column(nullable = false) private Boolean canView = false;
    @Column(nullable = false) private Boolean canCreate = false;
    @Column(nullable = false) private Boolean canEdit = false;
    @Column(nullable = false) private Boolean canDelete = false;
    @Column(nullable = false) private Boolean canApprove = false;

    public Long getId() { return id; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
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
