package com.cricklocal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "permission_modules",
        uniqueConstraints = @UniqueConstraint(name = "uk_permission_module_key", columnNames = "module_key"))
public class PermissionModule {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "module_key", nullable = false, length = 80)
    private String moduleKey;
    @Column(name = "module_name", nullable = false, length = 120)
    private String moduleName;
    @Column(nullable = false)
    private Boolean active = true;

    public Long getId() { return id; }
    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(String moduleKey) { this.moduleKey = moduleKey; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
